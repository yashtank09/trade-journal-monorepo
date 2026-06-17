# Trade Journal Application

A professional-grade backend application for traders to log, track, and analyze their trading performance. Built using a **Modular Monolith** architecture with **Spring Boot**.

## 🏗 Architecture

The application follows a **Modular Monolith** architectural style. This approach keeps the simple deployment model of a monolith while enforcing strong boundaries between different business domains (features). This prepares the system for potential future splitting into microservices if scaling requirements demand it.

### High-Level Structure

```text
src/main/java/org/tradebook/journal
├── common          # Shared Kernel (DTOs, Utils, Exceptions, Base Entities)
├── config          # Global Configuration (Security, OpenAPI, CORS)
├── features        # Business Domains (The Core Modules)
│   ├── auth        # Identity & Access Management (JWT, Users)
│   ├── journal     # Core Domain (Trades, Plans, Instruments)
│   ├── ingestion   # Data Integration (File Uploads, CSV Processing)
│   └── analytics   # (Planned) Dashboard & Performance Metrics
└── TradeJournalApplication.java
```

### Key Design Decisions

*   **API-First Design**: strict REST contracts defined before implementation.
*   **Layered Architecture within Modules**: Each feature module has its own `Controller` -> `Service` -> `Repository` stack.
*   **Security**: Stateless generic authentication using **JWT (JSON Web Tokens)** via Spring Security.
*   **Data Integrity**: Using `BigDecimal` for all financial calculations to prevent floating-point errors.
*   **Database**: **MySQL** with a normalized schema for Trades and Users.

## 🛠 Tech Stack

*   **Language**: Java 17
*   **Framework**: Spring Boot 3.x
*   **Build Tool**: Maven
*   **Database**: MySQL 8.0
*   **Security**: Spring Security + JWT (jjwt)
*   **Documentation**: OpenAPI / Swagger UI
*   **Utilities**: Lombok, Apache Commons CSV

## 🚀 Getting Started

### Prerequisites

*   JDK 17+ installed
*   Maven installed
*   MySQL Server running locally

### Installation

1.  **Clone the repository**
    ```bash
    git clone <repo-url>
    cd trade-journals
    ```

2.  **Configure Database**
    Update `src/main/resources/application.yml` with your database credentials.
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/tradebook_db
        username: your_user
        password: your_password
    ```

3.  **Build the Project**
    ```bash
    mvn clean install
    ```

4.  **Run the Application**
    ```bash
    mvn spring-boot:run
    ```

### 📚 API Documentation

Once the application is running, access the interactive API documentation at:

**[http://localhost:8085/api/v1/swagger-ui/index.html](http://localhost:8085/api/v1/swagger-ui/index.html)**

### Features Implemented (Phase 1)

*   **User Authentication**: Register & Login (JWT).
*   **Trade Management**: Create, Read, Update Trades.
*   **Trade Planning**: Attach entry reasons and planned logic to trades.
*   **File Ingestion**: Upload raw trade files (CSV/Excel) for processing.
*   **PnL Calculation**: Automated Gross/Net PnL computation on trade closure.

## 🤝 Contributing

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request
