# Authentication

Sprint 3 implements the backend auth/account lifecycle for LaunchStack Java:

- register/login/refresh/logout/current-user endpoints
- email verification token flow
- resend verification endpoint with generic response
- forgot/reset password token flow with generic responses
- single-use, expiring verification/reset tokens
- local/dev email logging via `EmailService` abstraction (no real SMTP required)

## Configuration

Set these environment variables (see `.env.example`):

- `JWT_SECRET` (at least 32 characters for HS256)
- `JWT_ACCESS_TOKEN_EXPIRATION` (seconds, default `900`)
- `JWT_REFRESH_TOKEN_EXPIRATION` (seconds, default `1209600`)
- `EMAIL_VERIFICATION_TOKEN_EXPIRATION` (seconds, default `86400`)
- `PASSWORD_RESET_TOKEN_EXPIRATION` (seconds, default `3600`)
- `APP_BASE_URL` (used to generate verification and password reset links)
- `SEED_ADMIN_EMAIL`
- `SEED_ADMIN_PASSWORD`

## Local/dev email delivery behavior

`com.launchstack.email.EmailService` defines email sending operations used by auth/account flows.

Current implementation: `LoggingEmailService`

- logs `to`, `subject`, and plain-text body
- logs include verification/reset links and tokens
- supports local development without SMTP

A future SMTP implementation can replace `LoggingEmailService` without changing auth/account logic.

## Register flow

`POST /api/auth/register`

1. Validates payload.
2. Normalizes email and hashes password with BCrypt.
3. Creates user with `enabled=false` until verification completes.
4. Creates an email verification token (single-use, expiring).
5. Sends/logs verification email through `EmailService`.

## Verify email flow

`POST /api/auth/verify-email`

1. Validates token presence.
2. Validates token exists, not expired, not used.
3. Marks user as enabled.
4. Marks verification token as used.

## Resend verification flow

`POST /api/auth/resend-verification`

1. Accepts email.
2. If user exists and is still unverified, creates/sends a new verification token.
3. Always returns a generic safe response to avoid user enumeration.

Rate limiting is intentionally not implemented in Sprint 3 and should be added as hardening in a future sprint.

## Login flow

`POST /api/auth/login`

1. Validates payload.
2. Verifies email/password.
3. Requires account to be active (`enabled=true`, non-locked).
4. Returns access token + refresh token.

## Refresh flow

`POST /api/auth/refresh`

1. Looks up refresh token.
2. Rejects invalid/revoked/expired tokens.
3. Returns a new access token.
4. Returns the same refresh token (simple non-rotating flow).

## Logout flow

`POST /api/auth/logout`

1. Accepts refresh token.
2. Marks token as revoked.

## Forgot password flow

`POST /api/auth/forgot-password`

1. Accepts email.
2. If user exists, creates a password reset token and sends/logs reset email.
3. Always returns a generic safe response.

## Reset password flow

`POST /api/auth/reset-password`

1. Validates token and new password.
2. Validates token exists, not expired, not used.
3. Hashes and updates user password.
4. Marks reset token as used.
5. Revokes active refresh tokens for that user.

## Current user flow (`/api/auth/me`)

`GET /api/auth/me`

- Requires Authorization header with a valid JWT access token.
- Returns id/email/name/roles for current user.

## Profile endpoints (`/api/me`)

Sprint 4 adds a profile-focused endpoint pair alongside `/api/auth/me`:

- `GET /api/me` — returns current user profile from the same authenticated principal context.
- `PUT /api/me` — allows updating only `firstName` and `lastName`.

`/api/auth/me` remains available for backward compatibility with previous sprint behavior.
Users cannot change their own roles, enabled flag, or lock status through `/api/me`.
