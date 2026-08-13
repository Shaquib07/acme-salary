# Scope clearance email

**Subject:** ACME Salary Management — updated scope (RBAC in MVP)

Hi [Name],

Login and role-based access are in scope for the first delivery. SSO / OAuth / Azure AD remain out.

**Roles**

- **HR_ADMIN** — full access, including create/deactivate employees
- **HR_MANAGER** — view, edit salaries, insights, CSV export
- **FINANCE_VIEWER** — view directory and pay insights only; cannot change or export salary data

Authorization is enforced on the API, not only in the UI. Demo users are seeded (no self-registration, no in-app user admin).

**Still out of scope:** SSO/IdP, payroll/tax/benefits, employee self-service, approval workflows, Excel bulk import, notifications, multi-tenant.

Please confirm this split, or flag anything to pull into MVP.

Thanks,
[Your name]
