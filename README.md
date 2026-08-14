# ACME Salary Management

Web app for ACME HR to manage salaries for ~10,000 employees and answer how the org pays people. Replaces Excel.

## Demo accounts

Password for all: `Password123!`

| Email | Role |
|---|---|
| admin@acme.test | HR_ADMIN (create / deactivate / edit / export) |
| hr@acme.test | HR_MANAGER (edit salary / export) |
| finance@acme.test | FINANCE_VIEWER (read only) |

## Run with Docker

```bash
docker compose up --build
```

UI: http://localhost:8081  
API: http://localhost:8080  
Health: http://localhost:8080/actuator/health  

First boot seeds 10,000 employees (about 30–60 seconds).

## Local development

Needs Java 21, Maven, Node 20, and MySQL 8 (Docker below is enough).

```bash
docker compose up mysql -d
cd backend
mvn test
mvn spring-boot:run
```

Default DB: `acme_salary` / user `acme` / password `acme` on `localhost:3306`. If MySQL is already installed (Windows service `MySQL80`), skip the Compose MySQL container and create the schema once as root:

```sql
CREATE DATABASE IF NOT EXISTS acme_salary CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'acme'@'localhost' IDENTIFIED BY 'acme';
GRANT ALL PRIVILEGES ON acme_salary.* TO 'acme'@'localhost';
FLUSH PRIVILEGES;
```

Override with `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`. Same variables are what you set on Railway/Render.

```bash
cd frontend
npm install
npm test
npm run dev
```

UI at http://localhost:5173 proxies `/api` to the backend.

## Incremental Git history

The first commit is `docs/REQUIREMENTS.md`. To slice the rest of the tree into assessment-style commits:

```powershell
powershell -File scripts/make-incremental-commits.ps1
```

## Layout

- `docs/` — requirements, architecture, trade-offs, AI usage, demo script
- `backend/` — Spring Boot 3, JPA, Spring Security, Flyway, MySQL
- `frontend/` — React + TypeScript + MUI

## Product notes

Payroll is **never summed across currencies**. USD totals on insights are approximate, from seeded FX rates.
