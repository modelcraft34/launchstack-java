# LaunchStack Java

Stop rebuilding authentication, user management, and admin panels from scratch.

## Overview

LaunchStack Java is a production-oriented Spring Boot + Angular SaaS/Admin Starter Kit for teams that want a clean starting point for commercial and internal products.

## Repository Layout

- `/backend` - Spring Boot 3 / Java 17 / Maven backend foundation
- `/frontend` - Angular placeholder application structure
- `/docs` - product and implementation guides to expand in later sprints
- `/docker-compose.yml` - local development orchestration skeleton
- `/.env.example` - environment variable template for local setup

## Requirements

- Java 17
- Maven 3.9+
- Node.js 20+
- npm 10+
- Docker and Docker Compose for the default PostgreSQL-based stack
- PostgreSQL 16 if you want to run the default profile outside Docker

## Quick Start

1. Default profile: copy `.env.example` to `.env` and start the PostgreSQL-backed stack with Docker Compose.
2. Local profile: run the backend directly with the `local` profile to use in-memory H2 without Docker or PostgreSQL.
3. Flyway runs automatically in both profiles.
4. Open Swagger UI locally once the backend is running.

See `docs/getting-started.md` for the backend setup details.

## Commands

### Local development

```bash
cp .env.example .env
docker compose up --build
```

### Backend without Docker or PostgreSQL

IntelliJ run configuration:

- Main class: `com.launchstack.LaunchStackApplication`
- Active profile: `local`
- Java SDK: `17`

Command line:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Backend

```bash
cd backend
mvn spring-boot:run
mvn test
```

Backend defaults:

- default profile datasource values come from `.env` / environment variables for PostgreSQL / Docker
- `local` profile switches to in-memory H2 for Docker-free local development
- Flyway is enabled in both the default and `local` profiles
- auth token settings come from `JWT_SECRET`, `JWT_ACCESS_TOKEN_EXPIRATION`, `JWT_REFRESH_TOKEN_EXPIRATION`
- account lifecycle token settings come from `EMAIL_VERIFICATION_TOKEN_EXPIRATION` and `PASSWORD_RESET_TOKEN_EXPIRATION`
- frontend/app link generation uses `APP_BASE_URL`
- optional local admin seed uses `SEED_ADMIN_EMAIL` and `SEED_ADMIN_PASSWORD`
- Swagger UI is exposed at `http://localhost:8080/swagger-ui.html`
- H2 Console is exposed at `http://localhost:8080/h2-console` when the `local` profile is active
- readiness is exposed at `http://localhost:8080/actuator/health/readiness`

H2 console connection details for the `local` profile:

- JDBC URL: `jdbc:h2:mem:launchstack`
- Username: `sa`
- Password: leave blank

You can manually test auth locally in the `local` profile with curl:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"local-user@launchstack.dev","password":"Password123!","firstName":"Local","lastName":"Tester"}'

curl -X POST http://localhost:8080/api/auth/resend-verification \
  -H "Content-Type: application/json" \
  -d '{"email":"local-user@launchstack.dev"}'

# copy token from backend logs (LoggingEmailService) and verify
curl -X POST http://localhost:8080/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{"token":"<verification-token>"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"local-user@launchstack.dev","password":"Password123!"}'

curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'

curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'

curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"local-user@launchstack.dev"}'

# copy token from backend logs (LoggingEmailService) and reset password
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"token":"<password-reset-token>","newPassword":"NewPassword123!"}'

curl http://localhost:8080/api/auth/me \
  -H "Authorization: ******"
```

### Frontend

```bash
cd frontend
npm install
npm run start
npm run build
```

## Status

Sprint 4 extends backend authorization and user management:

- user, role, and refresh token persistence
- register, login, refresh, logout, and `/api/auth/me` endpoints
- `/api/auth/verify-email`, `/api/auth/resend-verification`, `/api/auth/forgot-password`, `/api/auth/reset-password`
- admin-only user management APIs: `/api/users`, `/api/users/{id}`, `/api/users/{id}/status`
- admin-only role listing API: `GET /api/roles`
- authenticated profile APIs: `GET /api/me`, `PUT /api/me`
- BCrypt password hashing
- JWT access token generation and validation
- stateless Spring Security with JWT filter
- idempotent seed roles/admin support via environment variables
- local/dev email delivery abstraction with log-based email output (verification + reset links/tokens)

Intentionally not implemented yet:

- frontend auth screens and feature UI
- payment, multi-tenancy, notifications, file upload, and other advanced product features
