# Restaurant App

A mock restaurant website built as a full-stack skills project using current
(2026) enterprise standards: menu browsing, table reservations, online ordering
with mock payment, and a role-protected admin dashboard.

## Stack

| Layer | Tech |
|---|---|
| Backend | Java 25 · Spring Boot 4.1 · Spring Security 7 (JWT resource server) · Spring Data JPA · Flyway · springdoc-openapi |
| Frontend | React 19 · TypeScript · Vite 8 · React Router 8 · TanStack Query 5 · Zustand · react-hook-form + zod 4 · Tailwind CSS 4 |
| Database | PostgreSQL 17 (local: Docker · prod: Cloud SQL) |
| Infra | Docker · GitHub Actions (WIF keyless deploys) · GCP Cloud Run · Artifact Registry · Secret Manager · Firebase Hosting |

## Local development

```bash
docker compose up -d postgres          # database
cd backend && ./mvnw spring-boot:run   # API on :8080 (local profile is default)
cd frontend && npm run dev             # SPA on :5173, /api proxied to :8080
```

- API docs: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

## Repository layout

- `backend/` — Spring Boot API, package-by-feature (`menu`, `reservations`, `orders`, `auth`)
- `frontend/` — Vite React SPA, feature folders; `src/api/generated/` is produced
  from the backend's OpenAPI spec (never hand-edited)
- `docker-compose.yml` — local Postgres (full-stack profile added later)
