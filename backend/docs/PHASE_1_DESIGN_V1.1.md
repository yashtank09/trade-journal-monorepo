# Phase 1 (Revised) Design: Trade Journal Application

## Part 1: Architecture Overview

### 1. Core Principles
* **Modular Monolith**: Strict separation between `features/auth`, `features/journal`, and `features/ingestion`.
* **API-First**: All interaction via REST API. Base path: `/api/v1`.
* **Data Integrity**:
    * **Raw Data (`TradeBookMaster`)**: Immutable "Receipts" of what happened.
    * **Journal Data (`Trade`)**: The "Inventory" of positions, derived via FIFO logic.

### 2. High-Level Workflow (The "Upload-First" Pipeline)
1.  **Ingest**: User uploads Broker CSV $\rightarrow$ Saved to `TradeBookData` (Audit Trail).
2.  **Clean**: System aggregates partial fills $\rightarrow$ `TradeBookMaster`.
3.  **Match**: Async Job runs FIFO Logic $\rightarrow$ Creates/Updates `Trade`.
4.  **Journal**: User sees the final `Trade` entities (Open/Closed PnL).

---

## Part 2: API Definitions

### 1. Ingestion (Data Source)
* **Endpoint**: `POST /file/upload`
* **Purpose**: Upload raw broker files.
* **Request**: `MultipartFile` + JSON Metadata (Broker Name, Type).
* **Response**: `Job ID` for tracking status.

### 2. Trade Management (The Journal)
* **Endpoint**: `GET /trades`
* **Purpose**: Fetch the "Living Journal". Returns mixed data (Manual + Ingested).
* **Response**:
    ```json
    [
      {
        "id": 105,
        "symbol": "TATASTEEL",
        "status": "OPEN",
        "quantity": 100,
        "remainingQuantity": 100,
        "avgEntryPrice": 150.00,
        "netPnl": 0.00
      }
    ]
    ```

* **Endpoint**: `POST /trades` (Manual Entry)
* **Purpose**: Log a trade manually (bypassing ingestion).

### 3. Analytics
* **Endpoint**: `GET /analytics/daily-pnl`
* **Purpose**: Returns aggregated PnL from the `Trade` table.

---

## Part 3: Database Schema (Revised)

### 1. `users`
*Standard Identity Table*
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | BIGINT PK | |
| `email` | VARCHAR | Unique Login |
| `password_hash` | VARCHAR | |
| `currency` | VARCHAR | Preferred currency (USD/INR) |

### 2. `trade_book_data` (Raw Storage)
*The "Dump" table for CSV rows.*
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | BIGINT PK | |
| `user_id` | BIGINT FK | **(Added)** Multi-tenancy |
| `original_row` | JSON/TEXT | Full raw row for debugging |
| `order_id` | VARCHAR | Broker Order ID |
| `symbol` | VARCHAR | |
| `processed` | BOOLEAN | Flag for next step |

### 3. `trade_book_master` (Cleaned Events)
*Aggregated "Receipts" (1 Row per Order).*
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | BIGINT PK | |
| `user_id` | BIGINT FK | **(Added)** |
| `order_id` | VARCHAR | Unique Business Key |
| `symbol` | VARCHAR | |
| `trade_type` | VARCHAR | BUY / SELL |
| `total_quantity` | DECIMAL | Aggregated from partial fills |
| `avg_price` | DECIMAL | Weighted Avg Price |
| `is_journaled` | BOOLEAN | Has this been turned into a Position? |

### 4. `trades` (The Journal)
*The Core Business Entity (Inventory).*
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | BIGINT PK | |
| `user_id` | BIGINT FK | Owner |
| `symbol` | VARCHAR | **(Direct)** No longer strictly FK |
| `exchange` | VARCHAR | NSE/BSE |
| `status` | VARCHAR | `OPEN`, `PARTIAL`, `CLOSED`, `ORPHAN` |
| `direction` | VARCHAR | `LONG` / `SHORT` |
| **Quantity Logic** | | |
| `quantity` | DECIMAL | Initial Size (e.g., 100) |
| `remaining_quantity`| DECIMAL | **(New)** For FIFO (e.g., 50 left) |
| **Financials** | | |
| `entry_price` | DECIMAL | Avg Entry |
| `exit_price` | DECIMAL | Avg Exit (Weighted) |
| `gross_pnl` | DECIMAL | Realized PnL so far |
| `net_pnl` | DECIMAL | Gross - Fees |

---

## Part 4: Automation Logic (The Bridge)

### The "Position Discovery" Job
This background process runs every minute to sync `TradeBookMaster` $\to$ `Trades`.

**Logic Flow:**
1.  **Fetch**: Get all `TradeBookMaster` rows where `is_journaled = false`.
2.  **Identify**:
    * If `BUY`: Look for existing `SHORT` trades to cover.
    * If `SELL`: Look for existing `LONG` trades to close.
3.  **FIFO Match**:
    * Sort open trades by `entry_time ASC`.
    * Subtract quantity from `remaining_quantity`.
    * Calculate Realized PnL for the portion closed.
4.  **Handle Leftovers**:
    * If quantity remains after matching, create a **New Position**.
    * *Edge Case*: If `SELL` remains but no `LONG` exists $\to$ Create `Trade` with status **`ORPHAN`** (Flags user to fix history).
5.  **Commit**: Save `Trade`, mark `TradeBookMaster` as `journaled`.

---

## Part 5: Tech Stack & config

* **Language**: Java 17 (Spring Boot 3)
* **Database**: MySQL 8.0
* **Frontend**: React (Chosen for Charting capabilities)
* **Context Path**: `/api/v1` (Fixed in `application.yml`)