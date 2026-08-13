# Architecture

ACME Salary is a modular monolith: a Spring Boot API and a React SPA. One process, one SQLite file, ~10k employees. That is enough scale for this product and keeps operations trivial for the assessment.

```
Browser (React + MUI)
        |
        | HTTPS / JSON + Bearer JWT
        v
Spring Boot 3 (Java 21)
  Security  -> Auth/RBAC
  Web       -> REST controllers
  Services  -> salary rules, insights
  JPA       -> Hibernate
        |
        v
SQLite (Flyway migrations)
```

## Why this shape

- **Modular monolith, not microservices.** One HR workspace, one database. Splitting auth/employees/insights into services would add latency and ops with no product benefit.
- **Server-side pagination.** The directory never ships 10k rows to the browser. Filters and sort run in SQL with indexes on email, last name, country+status, department+status.
- **Insights in SQL.** `GROUP BY` country, department, and currency. Mixed currencies are never summed. USD totals use a seeded FX table and are labeled approximate.
- **JWT + method security.** The SPA stores a token and sends `Authorization: Bearer`. `@PreAuthorize` on controllers is the real gate; the UI only hides buttons.
- **SQLite in a volume.** Fine for 10k rows and a single instance. Postgres would be the next step for concurrent writers or Azure.

## Modules (backend)

| Package | Responsibility |
|---|---|
| `auth` | Login, JWT, current user |
| `security` | Filters, `UserDetails`, CORS |
| `employee` | Directory, salary patch, create/deactivate, CSV |
| `insight` | Aggregations and pay bands |
| `catalog` | Country/currency pairing |
| `seed` | Idempotent 10k employees + demo users + FX |

## Frontend

Vite + React + TypeScript. TanStack Query for server state. MUI DataGrid in **server pagination** mode. Routes: `/login`, `/directory`, `/employees/:id`, `/insights`. A small `can(role, action)` helper drives button visibility; a 403 page handles deep links.

## Deployment

Docker Compose: `api` (JDK 21) + `web` (nginx static build). SQLite file on a named volume. Health: Spring Actuator `/actuator/health`.
