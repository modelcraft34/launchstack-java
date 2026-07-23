# Getting Started

LaunchStack Java now supports two backend run modes:

- default profile: PostgreSQL-first, aligned with Docker Compose and production-like development
- `local` profile: in-memory H2 for local backend work without Docker or PostgreSQL

## Run with Docker Compose (default PostgreSQL profile)

1. Copy the root environment template:

   ```bash
   cp .env.example .env
   ```

2. Start the local stack:

   ```bash
   docker compose up --build
   ```

This keeps `backend/src/main/resources/application.yml` as the default datasource configuration.

## Run backend locally without Docker

### IntelliJ

Create a Spring Boot run configuration with:

- Main class: `com.launchstack.LaunchStackApplication`
- Active profile: `local`
- Java SDK: `17`

### Command line

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Local profile URLs

- Backend: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console`
- Readiness: `http://localhost:8080/actuator/health/readiness`

## H2 console connection details

- JDBC URL: `jdbc:h2:mem:launchstack`
- Username: `sa`
- Password: leave blank

The `local` profile uses H2 in PostgreSQL compatibility mode so the shared Flyway migrations can run without changing the default PostgreSQL profile behavior.

## Database configuration

- Default profile PostgreSQL values still come from environment variables.
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, and `SPRING_DATASOURCE_DRIVER_CLASS_NAME` can override the default PostgreSQL settings when needed.
- The `local` profile overrides those datasource settings with H2 only for local developer convenience.

## Flyway behavior

- Flyway remains enabled for the default PostgreSQL profile.
- Flyway also runs in the `local` profile against H2.
- Existing migrations `V1__baseline.sql` and `V2__auth_core.sql` are shared across both profiles; H2 runs them via PostgreSQL compatibility mode.

## Seed admin user

- Seed roles/admin support still uses `SEED_ADMIN_EMAIL` and `SEED_ADMIN_PASSWORD`.
- The seed runner is idempotent and works in both the default PostgreSQL profile and the `local` H2 profile.

## Test auth endpoints locally

Example curl flow against the `local` profile:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"local-user@launchstack.dev","password":"Password123!","firstName":"Local","lastName":"Tester"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"local-user@launchstack.dev","password":"Password123!"}'

curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'

curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'

curl http://localhost:8080/api/auth/me \
  -H "Authorization: ******"
```

## What remains unchanged

- `backend/src/main/resources/application.yml` stays PostgreSQL / Docker oriented
- the `local` profile exists only for local developer convenience
- no Sprint 3 work is started here
- no new product features are introduced beyond local developer runtime support
