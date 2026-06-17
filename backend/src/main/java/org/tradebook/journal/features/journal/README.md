# Journal Module (Core)

## 📖 Overview
The Journal module is the heart of the application. It manages the lifecycle of manual trade entries, planning, and execution tracking. It is responsible for recording what the trader *did* and *why* they did it.

## 📦 Components

### Entities
*   **`Trade`**: The central aggregate. Stores execution details (Entry/Exit Price, Quantity, Time) and Financial results (Gross PnL, Net PnL).
*   **`TradePlan`**: A 1:1 extension of a Trade. Stores the *intent* (Target, Stop Loss, Risk-Reward) and *psychology* (Setup reason, Notes).
*   **`Instrument`**: Normalized reference for tradeable assets (Symbol + Exchange + Type).

### Services
*   **`TradeService`**: 
    *   Handles trade creation and linking to `User`.
    *   Automatically calculates **Realized PnL** when a trade is closed (`OPEN` -> `CLOSED`).
    *   Ensures users can only access their own data (Authorization check).

### API Endpoints
*   `POST /api/v1/trades`: Log a new trade (with optional Plan).
*   `PATCH /api/v1/trades/{id}`: Update trade status (e.g., Close trade) or modify details.
*   `GET /api/v1/trades`: Retrieve trade history with date filtering.

## 🧠 Business Logic
*   **PnL Calculation**: `(Exit Price - Entry Price) * Qty` (adjusted for Direction).
*   **Instrument Resolution**: Automatically finds existing Instruments or creates new ones on the fly to avoid duplication.
