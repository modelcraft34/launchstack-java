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
- Docker and Docker Compose
- PostgreSQL 16 (or Dockerized PostgreSQL)

## Quick Start

1. Copy `.env.example` to `.env` and adjust values for your environment.
2. Start PostgreSQL and the app stack with Docker Compose.
3. Flyway runs automatically on backend startup.
4. Open Swagger UI locally once the backend is running.

See `docs/getting-started.md` for the backend setup details.

## Commands

### Local development

```bash
cp .env.example .env
docker compose up --build
```

### Backend

```bash
cd backend
mvn spring-boot:run
mvn test
```

Backend defaults:

- datasource values come from `.env` / environment variables
- Flyway is enabled by default for local development
- auth token settings come from `JWT_SECRET`, `JWT_ACCESS_TOKEN_EXPIRATION`, `JWT_REFRESH_TOKEN_EXPIRATION`
- optional local admin seed uses `SEED_ADMIN_EMAIL` and `SEED_ADMIN_PASSWORD`
- Swagger UI is exposed at `http://localhost:8080/swagger-ui.html`
- readiness is exposed at `http://localhost:8080/actuator/health/readiness`

### Frontend

```bash
cd frontend
npm install
npm run start
npm run build
```

## Status

Sprint 2 adds backend auth core:

- user, role, and refresh token persistence
- register, login, refresh, logout, and `/api/auth/me` endpoints
- BCrypt password hashing
- JWT access token generation and validation
- stateless Spring Security with JWT filter
- idempotent seed roles/admin support via environment variables

Intentionally not implemented yet:

- email verification
- forgot/reset password
- user management admin APIs beyond auth core
- frontend auth screens and feature UI
- payment, multi-tenancy, notifications, file upload, and other advanced product features
