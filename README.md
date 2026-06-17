# Trade Journal Monorepo

Welcome to the **Trade Journal Monorepo**. This unified repository combines the Java Spring Boot backend and the Angular standalone client frontend into a single, clean workspace while retaining their complete Git version control histories.

---

## 📂 Repository Layout

```
trade-journal-monorepo/
├── .github/
│   └── workflows/
│       └── ci.yml             # Unified CI/CD automated workflow
├── backend/                   # Spring Boot REST API Service
│   ├── src/
│   ├── pom.xml
│   └── README.md
├── frontend/                  # Angular standalone application
│   ├── src/
│   ├── package.json
│   └── README.md
├── docker-compose.yml         # Global multi-container docker orchestration
├── .gitignore                 # Workspace-wide global ignore rules
└── README.md                  # Unified developers reference guide (this file)
```

---

## 🛠️ Technology Stack & Context

| Ecosystem | Technology / Tools | Details |
| :--- | :--- | :--- |
| **Backend** | Java 17, Spring Boot 3.x, Maven, MySQL 8.0, Liquibase, Spring Security + JWT | Context path: `/api/v1`, Default Port: `8085` |
| **Frontend**| Angular 21.x (Standalone Components), Tailwind CSS, Node.js 24.x, npm 11.x | Local Port: `4200` |
| **Database**| MySQL 8.0 | Local Port: `3306` |

---

## 🚀 Quick Start (Docker Compose)

The easiest way to bootstrap the entire development stack is by using the root `docker-compose.yml`. This fires up three networked containers in an internal bridge network:
1. `mysql-db`: Persisted MySQL database
2. `trade-journal-backend`: Java REST service built from `./backend`
3. `trade-journal-frontend`: Nginx web server serving compiled Angular code built from `./frontend`

### 1. Build and Run
From the root directory of this monorepo, run:
```bash
docker compose up --build
```

### 2. Verify Access
- **Frontend App**: Open [http://localhost:4200](http://localhost:4200) in your web browser.
- **Backend API**: Verify health/endpoints at [http://localhost:8085/api/v1](http://localhost:8085/api/v1).
- **MySQL Database**: Exposed locally on port `3306` (Credentials: `admin` / `Admin@!123`, DB Name: `tradebook_db`).

### 3. Stop Environment
```bash
docker compose down -v
```

---

## 💻 Local Manual Development

For interactive active debugging, you can run individual services locally on your host machine.

### Prerequisites
- Java Development Kit (JDK) 17+ installed
- Node.js 24.x and npm 11.x installed
- Running MySQL server on port `3306` with credentials matching `application.yml` or overridden via env vars.

---

### Step 1: Start MySQL Database
You can spin up just the database container:
```bash
docker compose up -d mysql-db
```

---

### Step 2: Running the Backend locally
Go into the `backend/` directory and run the Maven Spring Boot plugin:
```bash
cd backend
mvn spring-boot:run
```
> **Note:** The backend automatically runs Liquibase database migrations upon boot. It will listen on port `8085`.

---

### Step 3: Running the Frontend locally
Go into the `frontend/` directory, install packages, and launch Angular development server:
```bash
cd frontend
npm install
npm run start
```
The application will launch locally at [http://localhost:4200](http://localhost:4200) with hot reload and live reload enabled.

---

## 📈 Git History Retention Details

To create this monorepo and merge the original repositories (`trade-journals` and `trade-journal-ui`) while preserving their complete commit history, the following sequence was performed:

```bash
# 1. Initialize and perform root commit
git init
git checkout -b main
git add README.md
git commit -m "Initial commit: Initialize monorepo"

# 2. Merge Backend (trade-journals)
git remote add backend-origin https://github.com/yashtank09/trade-journals.git
git fetch backend-origin
git checkout -b temp-backend backend-origin/master
mkdir backend
git mv <list-of-tracked-files> backend/
git commit -m "Prepare backend repository structure for monorepo merge"
git checkout main
git merge temp-backend --allow-unrelated-histories -m "Merge backend repository history into backend/ subdirectory"
git branch -D temp-backend

# 3. Merge Frontend (trade-journal-ui)
git remote add frontend-origin https://github.com/yashtank09/trade-journal-ui.git
git fetch frontend-origin
git checkout -b temp-frontend frontend-origin/master
mkdir frontend
git mv <list-of-tracked-files> frontend/
git commit -m "Prepare frontend repository structure for monorepo merge"
git checkout main
git merge temp-frontend --allow-unrelated-histories -m "Merge frontend repository history into frontend/ subdirectory"
git branch -D temp-frontend
```
This architecture keeps file renaming trackable (using `git log --follow`) and preserves developer history completely.
