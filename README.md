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
2. Start the local stack with Docker Compose.
3. Run backend and frontend commands listed below when working outside containers.

Detailed setup documentation will be expanded in later sprints.

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

### Frontend

```bash
cd frontend
npm install
npm run start
npm run build
```

## Status

Sprint 0 provides repository scaffolding only. Authentication, business modules, payment, multi-tenancy, notifications, file upload, and other advanced product features are intentionally not implemented yet.
