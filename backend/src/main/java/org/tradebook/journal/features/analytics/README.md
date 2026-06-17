# Analytics Module

## 📊 Overview
The Analytics module is responsible for aggregating processed trades to surface actionable insights and performance tracking data. It feeds the application dashboards with vital metrics calculated dynamically based on past trade executions.

## 📦 Components

### Controllers
*   **`AnalyticsController`**: Exposes secure API endpoints for generating various trade analytics, charts, and breakdowns. Ensure users only view their isolated data via Principal verification.

### Services
*   **`AnalyticsService`**: Orchestrates the heavy computation tasks formatting large datasets into usable DTOs for the frontend charts. Data queries rely heavily on date-ranges and user scoping.

### DTOs
*   **`DashboardSummaryDTO`**: Key metrics (total trades, net PnL, win rate).
*   **`ChartPointDTO`**: Data structures for time-series progressions, primarily used for the Equity Curve.
*   **`BreakdownDTO`**: Handles multi-dimensional grouping.
*   **`RiskMetricsDTO`**: Used to return deep analytical risk metrics (e.g. drawdown).

### API Endpoints
*   `GET /analytics/summary`: Aggregate metrics across selected timeframes.
*   `GET /analytics/equity-curve`: Data points to reconstruct the PnL equity curve over time.
*   `GET /analytics/breakdown/asset`: Performance breakdown grouped by trading instrument/asset.
*   `GET /analytics/breakdown/strategy`: Performance breakdown highlighting the most profitable setups.
*   `GET /analytics/breakdown/time`: Performance grouped by execution time blocks.
*   `GET /analytics/risk-metrics`: Quantifiable risk management figures.
*   `GET /analytics/mistakes`: Frequency count and impact analysis of categorized mistakes.
