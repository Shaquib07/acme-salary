# How AI was used

I used Cursor as an assistant for scaffolding and mechanical edits. Product scope, architecture, and the rules that matter for this domain (RBAC and currency) were mine. I did not accept generated code I could not explain or that failed review.

## Decisions I owned

- MVP in/out of scope: directory, authorized salary edits, currency-safe insights, CSV export; no SSO, Kafka, or multi-tenant.
- Modular monolith (Spring Boot + React) instead of microservices.
- MySQL at runtime (shared JDBC contract for local Compose and hosting); H2 only in tests.
- Server-side pagination so the browser never loads 10,000 rows.
- Three roles with API enforcement (`@PreAuthorize`); the UI only hides actions.
- Never sum payroll across currencies; USD totals are approximate from seeded FX, not a live API.
- JWT for a split SPA/API demo; HTTP-only cookies would be the production follow-up.

These are also recorded in `REQUIREMENTS.md`, `ARCHITECTURE.md`, and `TRADEOFFS.md`.

## Where AI helped

- Boilerplate: controllers, JPA mappings, React pages, Docker/Vite wiring.
- Tests that lock RBAC and currency rules (I specified the cases; I kept only tests that would fail if those rules regressed).
- Incremental commit messages and deploy config (Railway `PORT`/CORS, Vercel `VITE_API_BASE`).
- First drafts of docs that I then rewrote so they match the code.

## What I did not outsource

- Whether a number is money (`BigDecimal`) or a display string.
- Whether a role may export or mutate.
- Whether an insight total is valid across INR and USD.

## Review loop

After each change I read the diff, ran `mvn test` and `npm test` where relevant, and discarded suggestions that added scope or hid the salary rules. AI sped up typing; it did not replace design or sign-off.
