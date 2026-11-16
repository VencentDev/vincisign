# VS-AUTH-001 — Implement Manual Email/Password Authentication

**Status:** Done
**Phase:** Phase 1 — Accounts and organization foundation
**Owner:** Backend + Web
**Dependencies:** VS-FND-001, existing OAuth authentication baseline

## Goal

Allow a user to create an account and sign in with email and password while preserving the existing Google OAuth flow. GitHub remains optional and is not required for manual account creation.

## Delivered

- Added `POST /api/v1/auth/signup`.
- Added `POST /api/v1/auth/login`.
- Added BCrypt password hashing; plaintext passwords are never stored.
- Added PostgreSQL-backed opaque sessions with SHA-256 token hashes.
- Added configurable 30-day session expiry through `APP_AUTH_SESSION_TTL`.
- Added authentication rate limiting to signup and login requests.
- Added local-session resolution to the existing bearer-token filter.
- Added local session revocation on logout.
- Added Auth.js Credentials authentication to the web application.
- Made the existing login and signup forms submit to the API.
- Preserved Google login and made GitHub conditional on configured credentials.
- Added OpenAPI paths and generated API types.
- Added integration coverage for signup, login, invalid credentials, duplicate email, password hashing, and authenticated `/me` access.
- Passwords are SHA-256 digested before BCrypt so the API accepts the documented 128-character maximum without BCrypt truncation.

## Account defaults

Manual accounts are created as:

- `role`: `USER`
- `userType`: `INDIVIDUAL`
- `identityLevel`: `I0`
- `contactVerificationStatus`: `UNVERIFIED`

Email verification, password reset, multi-factor authentication, and admin bootstrap are outside this ticket.

## API examples

### Signup

```http
POST /api/v1/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "correct horse battery staple",
  "displayName": "Example User"
}
```

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "correct horse battery staple"
}
```

Use the returned `accessToken` as:

```http
Authorization: Bearer <accessToken>
```

## Acceptance checklist

- [x] A valid signup creates a local account.
- [x] Password hashes are stored instead of plaintext passwords.
- [x] Duplicate email signup is rejected.
- [x] Correct credentials issue an authenticated session token.
- [x] Incorrect credentials return `401 Unauthorized`.
- [x] A local session can access protected endpoints.
- [x] Logout revokes a local session.
- [x] Sessions expire according to configuration.
- [x] Google OAuth remains available.
- [x] Missing GitHub credentials do not prevent the frontend from starting.
- [x] Mobile remains untouched.
- [x] Backend authentication integration tests pass.
- [x] Frontend typecheck passes.
- [x] OpenAPI types regenerate successfully.

## Verification

```bash
cd apps/backend
./mvnw -q -Dtest=AuthControllerIntegrationTest test

cd ../..
pnpm --filter @app/frontend typecheck
pnpm --filter @app/api-types check:generated
```
