# Ingestion Module

## 📥 Overview
The Ingestion module handles the bulk import of trade data from external sources (Broker reports, CSV files). It is designed to be asynchronous and robust, allowing users to backfill their journals without manual entry.

## 📦 Components

### Entities
*   **`FileProcessor`**: Tracks the status of an uploaded file (`PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`). Stores path and metadata.
*   **`JobStatus`** (Enum): State machine for the file processing lifecycle.

### Services
*   **`LocalFileProcessService`**: Handles the physical storage of uploaded `MultipartFile`s to the local disk and creates the tracking record in the DB.
*   **`TradeDataIngestionService`** (planned): Will handle parsing of specific CSV formats (e.g., Zerodha, Interactive Brokers) and converting them into `Trade` entities.

### API Endpoints
*   `POST /file/upload`: Upload a trade report file (CSV/Excel). Returns a Job ID for tracking.

## ⚙️ Workflow
1.  **Upload**: User uploads a file via API.
2.  **Validation**: System checks file type, size, and extension.
3.  **Storage**: File is saved securely to the `uploads/` directory with a unique UUID.
4.  **Processing** (Async): A background job picks up `PENDING` files, parses rows, and inserts them into the `Journal` module.
