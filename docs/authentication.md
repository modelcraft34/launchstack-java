# Authentication

Sprint 2 implements the backend authentication core with register/login/refresh/logout/me endpoints.

## Configuration

Set these environment variables (see `.env.example`):

- `JWT_SECRET` (at least 32 characters for HS256)
- `JWT_ACCESS_TOKEN_EXPIRATION` (seconds, default `900`)
- `JWT_REFRESH_TOKEN_EXPIRATION` (seconds, default `1209600`)
- `SEED_ADMIN_EMAIL`
- `SEED_ADMIN_PASSWORD`

## Register flow

`POST /api/auth/register`

1. Validates request payload.
2. Normalizes email.
3. Hashes password with BCrypt.
4. Creates user with default role `ROLE_USER`.
5. Returns access token + refresh token.

## Login flow

`POST /api/auth/login`

1. Validates request payload.
2. Finds user by email.
3. Verifies password with BCrypt.
4. Returns access token + refresh token.

## Refresh flow

`POST /api/auth/refresh`

1. Looks up refresh token.
2. Rejects invalid/revoked/expired tokens.
3. Issues a new access token.
4. Returns same refresh token (simple non-rotating flow for Sprint 2).

## Logout flow

`POST /api/auth/logout`

1. Accepts refresh token.
2. Marks token as revoked.
3. Subsequent refresh attempts with that token fail.

## Current user flow

`GET /api/auth/me`

- Requires an `Authorization` header using the `Bearer` scheme with an access token.
- Returns current user id/email/name/roles.
