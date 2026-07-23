# Getting Started

Sprint 1 prepares the backend technical foundation for later auth and user-management work.

## Local backend setup

1. Copy the root environment template:

   ```bash
   cp .env.example .env
   ```

2. Start the local stack:

   ```bash
   docker compose up --build
   ```

3. If you only want to run the backend manually, make sure PostgreSQL is running first and then start Spring Boot:

   ```bash
   cd backend
   mvn spring-boot:run
   ```

## Database configuration

- The backend reads PostgreSQL connection values from environment variables.
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, and `SPRING_DATASOURCE_DRIVER_CLASS_NAME` can override the composed defaults when needed.
- The default local Docker Compose flow uses the `postgres` service defined in the repository root.

## Flyway behavior

- Flyway is enabled for local development.
- Sprint 1 includes a minimal baseline migration under `backend/src/main/resources/db/migration`.
- The baseline intentionally does **not** create auth/user tables yet; it only establishes Flyway history so Sprint 2 can add real schema changes cleanly.

## Swagger / OpenAPI

- Swagger UI is available at:

  `http://localhost:8080/swagger-ui.html`

## What Sprint 1 implemented

- PostgreSQL datasource configuration via environment variables
- Flyway wiring with a minimal baseline migration
- Common API response wrapper
- Global exception handling and validation error formatting
- Audit base entity with `createdAt` / `updatedAt`
- OpenAPI metadata and Swagger UI access
- Basic backend tests for context load, readiness, Swagger UI, and exception handling

## Intentionally not implemented yet

- authentication, JWT, refresh tokens
- user/role entities and repositories
- register/login/logout endpoints
- frontend feature work
- payment, multi-tenancy, notifications, file upload, and other advanced modules
