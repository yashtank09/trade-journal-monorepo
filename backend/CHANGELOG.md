# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

### Refactor
- **Architecture**: Initiated restructuring project from `org.tradebook.tradeprocessor` to `org.tradebook.journal`.
- **Package Structure**: Adopting a modular monolith structure with feature-based packages:
  - `features/ingestion`: Legacy file processing logic.
  - `features/auth`: Security and User management.
  - `features/journal`: Core trade logic and matching.
  - `features/analytics`: Dashboard and performance metrics.
  - `common`: Shared DTOs, utilities, and configurations.

### Added
- Created `CHANGELOG.md` to track architectural changes.
- **Testing**: Added comprehensive unit tests for `AuthController`, `TradeController`, and `FileController`.
- **Documentation**: Added Postman collection `docs/postman/TradeJournal_API.postman_collection.json` for API testing.

### Fixed
- **API**: Resolved `FileController` path mapping issue (moved to `/api/v1/file`).
- **Configuration**: Fixed `server.servlet.context-path` duplication issue in `application.yml`.
- **Security**: Whitelisted Swagger UI resources in `SecurityConfig` to fix 403 Forbidden errors.
- **Tests**: Resolved compilation and runtime errors in unit tests.
