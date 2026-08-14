# Trade-offs

## Spring Boot vs Micronaut

Chose Spring Boot. The job description lists both; Spring Security, Data JPA, and MockMvc are the fastest path to production-shaped RBAC and tests. Micronaut would shave startup time that this app does not need.

## MySQL vs SQLite

MySQL is the runtime database so local Compose and hosted deploys share one JDBC URL contract. SQLite was simpler for a single file, but it fights Hibernate validation and does not ship cleanly to Vercel-style splits. Queries stay on JPQL so tests can keep using H2.

## JWT in localStorage vs HTTP-only cookie

JWT in `Authorization` is simple for a Vite SPA and Docker split (API on :8080, UI on :80). XSS risk is real; a follow-up would use HTTP-only cookies + CSRF. Not using OAuth/SSO: that is identity operations, not salary management.

## Three roles, not a permission matrix

`HR_ADMIN` / `HR_MANAGER` / `FINANCE_VIEWER` match the personas. A generic ACL would be more flexible and harder to demo. Export is denied to finance so salary files do not leave the building without HR intent.

## Approximate USD via static FX

Live FX APIs add flakiness and secrets. Seeded rates make insights deterministic and testable. The UI labels USD as approximate.

## No Excel import

Export is the escape hatch from the old workflow. Bulk import is a data-migration project (validation, duplicates, currency). Out of scope on purpose.

## MUI DataGrid vs a custom table

HR expects a dense, spreadsheet-like grid. Community DataGrid gives server pagination, sorting, and a familiar look without paying for the Pro license.

## Tests: H2 vs MySQL

Unit/API tests use H2 in-memory with Flyway off and Hibernate `create-drop` so they stay fast and isolated. The running app uses MySQL + Flyway. Queries stay on JPQL / ANSI `CASE` so both engines agree.
