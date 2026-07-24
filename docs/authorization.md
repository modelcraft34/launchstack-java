# Authorization

Sprint 4 introduces role-based authorization and admin user management APIs.

## Roles

- `ROLE_ADMIN`
  - full access to admin user-management endpoints
  - can list available roles
- `ROLE_USER`
  - no access to admin user-management endpoints
  - can access authenticated profile endpoints

## Protected endpoint rules

- Admin only:
  - `GET /api/users`
  - `GET /api/users/{id}`
  - `POST /api/users`
  - `PUT /api/users/{id}`
  - `PATCH /api/users/{id}/status`
  - `GET /api/roles`
- Authenticated users:
  - `GET /api/auth/me`
  - `GET /api/me`
  - `PUT /api/me`

## Public endpoints kept public

These remain public as implemented in previous sprints:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/verify-email`
- `POST /api/auth/resend-verification`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- Swagger/OpenAPI and actuator health/readiness endpoints

## User listing pagination format

`GET /api/users` responds with `ApiResponse<PageResponse<UserResponse>>`:

- `content`
- `page`
- `size`
- `totalElements`
- `totalPages`
- `first`
- `last`

Supported optional filters: `email`, `enabled`, `role`.
Supported pagination/sorting params: `page`, `size`, `sortBy`, `sortDirection`.

## Admin guardrails

- Email uniqueness is enforced.
- Passwords are always stored as BCrypt hashes.
- User-management responses never expose `passwordHash`.
- Last-admin safeguards prevent removing the final `ROLE_ADMIN` user.
- Status safeguards prevent disabling/locking the last active `ROLE_ADMIN` user.

## Notes

- Admin-created users are created directly and **no email notification is sent**.
- A complex dynamic permission matrix is intentionally out of scope in this sprint.
