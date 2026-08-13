# ACME Salary Management — Requirements

**Product:** Web software for ACME HR to manage salary data for ~10,000 employees across countries, replacing Excel as the system of record.

**Primary persona:** HR Manager. Secondary: HR Admin (workforce changes), Finance Viewer (pay analysis without mutation).

**Goal:** Look up people, change compensation when authorized, and answer “how does this org pay people?” with currency-safe insights.

## In scope (MVP)

- Login with seeded users (email/password). No self-registration.
- Role-based access enforced on **API and UI**.
- Employee directory: search, filter (country, department, status), sort, **server-side pagination** (do not load 10k rows in the browser).
- Salary edit with validation (amount > 0; currency consistent with country policy).
- Create employee and deactivate (no hard delete).
- Pay insights: headcount; payroll **by currency** (never sum mixed currencies); averages by country/department; pay-band distribution; optional USD equivalent via a seeded FX table, labeled approximate.
- CSV export of the current filter (HR’s Excel exit path).
- Seed of 10,000 employees plus three demo users.

## Roles

| Role | Directory + insights | Edit salary / profile | Create / deactivate | CSV export |
|---|---|---|---|---|
| `HR_ADMIN` | Yes | Yes | Yes | Yes |
| `HR_MANAGER` | Yes | Yes | No | Yes |
| `FINANCE_VIEWER` | Yes | No | No | No |

Unauthenticated → `401`. Forbidden → `403`. UI hides actions the role cannot use; the API is authoritative.

## Out of scope (and why)

- **SSO / OAuth / Azure AD** — local Spring Security is enough to prove RBAC; IdP is an ops project.
- **User-admin UI** — seed three demo users; role assignment stays in seed/DB.
- **Payroll, tax, benefits, bonuses, equity** — compensation operations, not salary data management.
- **Employee self-service** — this product is for HR/finance, not employees.
- **Salary history and approval workflows** — keep `updatedAt` + last-editor note only.
- **Excel/CSV bulk import** — export covers migration-out; import is its own project.
- **Notifications, real-time collab, multi-tenant** — one ACME org, one HR workspace.

## Success

- HR can find an employee among 10k in a few seconds and update salary (if authorized).
- Finance can view pay insights and cannot change or export salary data.
- Mixed-currency payroll is never shown as a single naive total.
- App runs via Docker Compose; tests cover core domain and authorization.

## Non-functional

- Java 21 / Spring Boot 3 backend; React + TypeScript UI; SQLite; money as `BigDecimal`.
- Fast, deterministic unit tests. Incremental Git history.
