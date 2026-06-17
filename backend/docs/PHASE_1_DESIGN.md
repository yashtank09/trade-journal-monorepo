# Phase 1 (MVP) Design: Trade Journal Application

## Part 1: API-First Design

### 1. API Principles
*   **Base Path**: `/api/v1`
*   **Format**: JSON for requests and responses.
*   **Dates**: ISO-8601 (`YYYY-MM-DD` and `YYYY-MM-DDTHH:mm:ssZ`).
*   **Monetary Values**: Strings in JSON to preserve precision (e.g., `"150.25"`), mapped to `BigDecimal` in backend.

### 2. API Definitions

#### A. Trade Entry & Management

**1. Create Trade Log (Plan + Initial Entry)**
*   **Endpoint**: `POST /trades`
*   **Purpose**: Log a new trade entry with its initial plan.
*   **Request**:
    ```json
    {
      "symbol": "NIFTY",
      "exchange": "NSE",
      "type": "BUY", 
      "quantity": 50,
      "entryPrice": "21500.00",
      "entryTime": "2025-12-31T09:15:00Z",
      "plan": {
        "targetPrice": "21600.00",
        "stopLossPrice": "21450.00",
        "riskRewardRatio": 2.0,
        "setupNote": "Gap up opening support"
      }
    }
    ```
*   **Response**: `201 Created`
    ```json
    {
      "id": "101",
      "status": "OPEN",
      "createdAt": "2025-12-31T09:15:00Z"
    }
    ```

**2. Update Trade (Exit / Outcome)**
*   **Endpoint**: `PATCH /trades/{id}`
*   **Purpose**: Close a trade or update its running status. Using PATCH allows partial updates.
*   **Request**:
    ```json
    {
      "exitPrice": "21580.00",
      "exitTime": "2025-12-31T10:30:00Z",
      "fees": "20.00",
      "reviewNote": "Exited early due to resistance",
      "status": "CLOSED" 
    }
    ```
*   **Response**: `200 OK`
    ```json
    {
      "id": "101",
      "grossPnl": "4000.00",
      "netPnl": "3980.00",
      "status": "CLOSED"
    }
    ```

#### B. Trade Retrieval

**3. Fetch Trades (History)**
*   **Endpoint**: `GET /trades`
*   **Query Params**:
    *   `startDate`: `2025-12-01`
    *   `endDate`: `2025-12-31`
    *   `status`: `CLOSED` (optional)
    *   `page`: `0`
    *   `size`: `20`
*   **Response**:
    ```json
    {
      "content": [
        {
          "id": "101",
          "date": "2025-12-31",
          "symbol": "NIFTY",
          "type": "BUY",
          "netPnl": "3980.00",
          "outcome": "WIN"
        }
      ],
      "totalElements": 1,
      "totalPages": 1
    }
    ```

#### C. Dashboard & Reporting

**4. Daily P/L Summary**
*   **Endpoint**: `GET /analytics/daily-pnl`
*   **Purpose**: Bar chart data for daily performance.
*   **Request Params**: `month=2025-12`
*   **Response**:
    ```json
    {
      "summary": [
        { "date": "2025-12-30", "totalTrades": 3, "netPnl": "-1500.00", "winRate": 33.3 },
        { "date": "2025-12-31", "totalTrades": 1, "netPnl": "3980.00", "winRate": 100.0 }
      ],
      "monthTotalPnl": "2480.00"
    }
    ```

**5. Dashboard Overview**
*   **Endpoint**: `GET /dashboard/stats`
*   **Purpose**: High-level tiles for the dashboard.
*   **Response**:
    ```json
    {
      "currentStreak": 2,
      "winRateMonth": 65.5,
      "avgWinner": "4000.00",
      "avgLoser": "1500.00",
      "bestSetup": "Gap Fill"
    }
    ```

---

## Part 2: Database Schema Draft

### 1. Database Choice
**Recommendation: PostgreSQL**
*   **Reasoning**: Better support for `DECIMAL` math precision, JSONB columns (useful for storing flexible trade "strategies" or "tags" metadata without altering schema), and advanced time-series extensions (`TimescaleDB`) if analytics grow complex.
*   *Note: MySQL is acceptable if strict structured relations are preferred and JSON flexibility is not a priority.*

### 2. Tables & Schema

#### `users`
*Minimal Core Identity*
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | BIGINT PK | Auto-increment ID |
| `email` | VARCHAR(100) | Unique Login |
| `password_hash` | VARCHAR(255) | BCrypt hash |
| `currency` | CHAR(3) | Account base currency (USD/INR) |

#### `instruments`
*Normalization for consistency*
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | INT PK | |
| `symbol` | VARCHAR(50) | e.g., "NIFTY", "AAPL" |
| `exchange` | VARCHAR(20) | NSE, NASDAQ |
| `type` | VARCHAR(20) | EQUITY, OPTION, FUTURE |

#### `trades`
*The Core Aggregate - Combines Execution & Result*
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | BIGINT PK | |
| `user_id` | BIGINT FK | Owner |
| `instrument_id` | INT FK | Link to symbol |
| `trade_date` | DATE | Reporting date |
| `direction` | VARCHAR(10) | LONG / SHORT |
| `quantity` | DECIMAL(15,2) | Support fractional shares |
| `status` | VARCHAR(15) | OPEN, CLOSED, CANCELLED |
| **P/L Data** | | |
| `entry_price` | DECIMAL(19,4) | Avg entry price |
| `exit_price` | DECIMAL(19,4) | Avg exit price |
| `gross_pnl` | DECIMAL(19,2) | (Exit - Entry) * Qty |
| `fees` | DECIMAL(10,2) | Brokerage + Tax |
| `net_pnl` | DECIMAL(19,2) | Gross - Fees |
| **Timing** | | |
| `entry_time` | TIMESTAMP | |
| `exit_time` | TIMESTAMP | |

#### `trade_plans`
*1:1 Relation with Trades (Optional separation for cleanliness)*
| Column | Type | Description |
| :--- | :--- | :--- |
| `trade_id` | BIGINT PK | FK to trades.id |
| `target_price` | DECIMAL(19,4) | Planned Target |
| `stop_loss` | DECIMAL(19,4) | Planned Stop |
| `risk_amount` | DECIMAL(19,2) | Calculated risk value |
| `setup_reason` | TEXT | "Why did I take this trade?" |
| `img_url` | VARCHAR(255) | Chart screenshot URL |

### 3. Relationships & Indexes

**Relationships**
*   `trades.user_id` -> `users.id` (One User, Many Trades)
*   `trades.instrument_id` -> `instruments.id` (Many Trades, One Instrument)
*   `trade_plans.trade_id` -> `trades.id` (One Trade, One Plan)

**Indexing Strategy**
1.  **Dashboard Speed**: `CREATE INDEX idx_trade_date_user ON trades(user_id, trade_date);` (Filtering by date range for a user)
2.  **Symbol Analytics**: `CREATE INDEX idx_trade_app_user_symbol ON trades(user_id, instrument_id);`
3.  **Status Check**: `CREATE INDEX idx_trade_status ON trades(status) WHERE status = 'OPEN';` (Quickly find active trades)

### 4. Financial Precision
*   **Logic**: NEVER use `FLOAT` or `DOUBLE`.
*   **Java**: `java.math.BigDecimal`
*   **DB**: `DECIMAL(19, 4)` for prices (allows high-value crypto or small forex pips). `DECIMAL(19, 2)` for PnL (cents/paisa).
