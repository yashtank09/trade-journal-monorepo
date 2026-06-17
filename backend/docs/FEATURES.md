# Trade Journal Application Features

The Trade Journal application is broken down into four core, domain-driven modules. These modules work together across the Angular (PrimeNG) frontend and the Java Spring Boot backend to help traders securely log, manage, insert, and analyze their trading activities.

## 1. Authentication (Auth Module)
Handles Identity and Access Management (IAM) for the platform.
* **Overview**: Provides stateless, secure authentication for user identity.
* **Key Capabilties**:
  * Secure `register` and `login` endpoints.
  * Validation rules for credentials.
  * Stateless JWT (JSON Web Tokens) handling for protected routes.
  * Passwords are irreversibly hashed using BCrypt.
* **Frontend**: Provides authentication guards that intercept unauthenticated access and redirect users to the login space.

## 2. Ingestion Module
Handles the bulk import and processing of trade data from external sources such as broker-generated CSV or Excel reports.
* **Overview**: Designed to be asynchronous and robust, allowing users to seamlessly backfill their journals.
* **Key Capabilties**:
  * Upload tracking using state-machine job statuses (`PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`).
  * File-type and size validations.
  * Secure storage of incoming files into the `uploads/` directory tied with unique UUID tracking.
* **Frontend**: Features drag-and-drop file upload interfaces bolstered by PrimeNG Toast notifications for real-time success or failure feedback.

## 3. Core Journal Module
The heart of the application. It acts as the system of record for manual trade entries, planning, and execution tracking.
* **Overview**: Dedicated to recording what the trader *did* and *why* they did it.
* **Key Capabilities**: 
  * Comprehensive entities capturing trade execution details (Entry/Exit Price, Quantity, Time, Instrument).
  * **Trade Plans**: Captures the psychological and strategic intent behind a trade (Target, Stop Loss, Risk-Reward, Setup reason, Mistakes, Notes).
  * Automatic **Realized PnL Calculation** upon trade closure.
  * Instrument resolution to naturally prevent duplicate asset entries.
* **Frontend**: Provides an interactive, unified table view (Trade Summary) allowing for ascending/descending sorts, quick filtering, and an integrated PrimeNG DatePicker for custom date ranges.

## 4. Analytics Module
Responsible for aggregating processed trades to surface actionable algorithmic insights and performance tracking.
* **Overview**: Feeds the application dashboards with vital metrics calculated dynamically based on past trade executions and the trade summaries context. 
* **Key Capabilities**:
  * **Dashboard Summary**: Aggregate stats (total trades, net PnL, win rate).
  * **Equity Curve**: Time-series progression of realized cumulative profit.
  * **Performance Breakdowns**: Slice-and-dice data dimensionally across:
    * *Assets*
    * *Strategies*
    * *Time / Sessions*
  * **Behavioral Tracking**: Quantifiable metric extractions on user *Mistakes* and *Risk Management* rules logic.
* **Frontend**: Fully modern dashboard displaying PrimeNG drill-down charts, KPI metrics on rich visual cards, and responsive graphical layouts.

---
*Architecture Note: All endpoints intrinsically ensure authorization boundaries (Tenant Isolation), ensuring that each user interacts implicitly and strictly with their isolated dataset.*
