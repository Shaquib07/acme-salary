# Recreates the assessment's incremental Git history from the current tree.
# Run from the repo root in PowerShell after the requirements commit exists.
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot\..

function Commit-Slice($message, $paths) {
  git add -- $paths
  $staged = git diff --cached --name-only
  if (-not $staged) {
    Write-Host "skip: $message"
    return
  }
  git commit -m $message
}

Commit-Slice "docs: architecture, trade-offs, and scope clearance" @(
  "docs/ARCHITECTURE.md", "docs/TRADEOFFS.md", "docs/SCOPE_CLEARANCE_EMAIL.md", ".gitignore"
)

Commit-Slice "chore: Spring Boot skeleton with Flyway and health" @(
  "backend/pom.xml",
  "backend/src/main/java/com/acme/salary/SalaryApplication.java",
  "backend/src/main/java/com/acme/salary/config",
  "backend/src/main/resources/application.yml",
  "backend/src/main/resources/db/migration",
  "backend/data/.gitkeep",
  "backend/src/test/resources/application.yml"
)

Commit-Slice "feat: JWT login and three-role Spring Security" @(
  "backend/src/main/java/com/acme/salary/auth",
  "backend/src/main/java/com/acme/salary/security",
  "backend/src/main/java/com/acme/salary/web"
)

Commit-Slice "feat: employee directory, salary patch, and CSV export" @(
  "backend/src/main/java/com/acme/salary/employee",
  "backend/src/main/java/com/acme/salary/catalog"
)

Commit-Slice "feat: pay insights, FX table, and 10k employee seed" @(
  "backend/src/main/java/com/acme/salary/insight",
  "backend/src/main/java/com/acme/salary/seed"
)

Commit-Slice "test: RBAC, salary rules, and currency-safe insights" @(
  "backend/src/test"
)

Commit-Slice "feat: React HR workspace with role-aware UI" @(
  "frontend"
)

Commit-Slice "chore: Docker Compose, CI, README, and demo script" @(
  "docker-compose.yml",
  "backend/Dockerfile",
  "frontend/Dockerfile",
  "frontend/nginx.conf",
  ".github",
  "README.md",
  "docs/DEMO.md",
  "docs/AI_USAGE.md",
  "docs/COMMIT_GUIDE.md",
  "scripts"
)

git status
