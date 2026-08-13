# How AI was used

This assessment was built with Cursor (agentic coding) against a written plan. The human owned product framing; the agent wrote boilerplate and tests.

## What the agent was asked to do

- Turn the assessment brief into a one-page requirements doc (in/out of scope, RBAC matrix).
- Propose Spring Boot + React + SQLite rather than microservices.
- Implement JWT RBAC, employee directory, salary patch, insights, 10k seed, Docker, tests.
- Keep commits incremental (requirements → architecture → API → UI → ship).

## What we accepted

- Modular monolith, server-side pagination, SQL `GROUP BY` for insights.
- Three roles with `@PreAuthorize` as the real control, UI as a courtesy.
- Seeded FX instead of a live FX API.
- H2 for tests, SQLite for the running app.

## What we rejected

- SSO / OAuth / Redis / Kafka / multi-tenant — out of scope and would hide judgment.
- Loading 10k rows into the browser.
- Summing INR + USD into one “total payroll” number.
- Excel bulk import in MVP.

## Quality loop

After each feature: read the diff, keep money on `BigDecimal`, add a test that would fail if RBAC or currency rules regressed.
