# VS-FND-004 — Build Onboarding, Consent, and Account Explanation Flow

**Status:** Done for current system-build scope
**Phase:** Phase 1 — Accounts, organizations, projects, documents, storage, and evidence
**Owner:** Backend + Web
**Dependencies:** VS-P0-005, VS-FND-001, VS-FND-002
**Mobile:** Not in scope; no mobile files were changed

## Goal

Give every new account a clear, auditable onboarding step before it can use protected
VinciSign business features. The flow records the selected individual/organization context,
the engineering-draft policy version and reference, the exact policy text shown by the server,
the decision status, the decision timestamp, and the acting account. A declined or withdrawn
required consent keeps the account blocked while preserving the historical record.

This ticket implements the system behavior only. The policy catalog is explicitly marked as
`engineering-draft-0.1`; production policy approval and release-gate decisions remain outside
this engineering ticket.

## Delivered

### Backend and database

- Added `users.onboarding_completed_at` as the server-side onboarding completion marker.
- Added `consent_records` with:
  - consent type
  - policy ID, version, and document reference
  - exact server-owned consent text
  - locale
  - accepted, declined, or withdrawn status
  - decision timestamp
  - acting user ID
  - inherited audit columns and historical rows
- Added the server-owned onboarding policy catalog for:
  - Terms of Service
  - Privacy Notice
  - Electronic Records
  - Platform Limits Acknowledgement
- Added:
  - `GET /api/v1/onboarding/status`
  - `POST /api/v1/onboarding/complete`
  - `POST /api/v1/onboarding/consents/{consentType}/withdraw`
- Repeated submission of the same current policy decision is idempotent.
- A changed policy version requires a new decision instead of treating an old acceptance as
  current.
- Withdrawal appends a historical `WITHDRAWN` record and clears completion. A declined or
  already withdrawn record cannot be withdrawn as if it were accepted.
- Protected organization, invitation, and todo endpoints enforce completed onboarding on the
  backend with method security. The account and onboarding endpoints remain available so the
  user can complete or review setup.
- The baseline stores the server decision timestamp. It does not claim a browser presentation
  timestamp that the server did not independently observe; detailed `consent.presented` event
  evidence remains part of the later signing/evidence work.

### Web

- Added authenticated `/onboarding` page and layout.
- Added individual/organization account-context selection.
- Added server-driven consent cards showing the title, description, reference, version, and
  required status.
- Added platform-limits explanation from the backend policy response.
- Added loading, retry, API error, blocked, validation, and completion states.
- Added safe same-origin callback handling after setup.
- Added server-side route guards for `/todos`, `/organizations`, and invitation acceptance.
- Invalidated the account query after onboarding so the selected account context is refreshed.

### API contract and tests

- Updated `packages/api-types/openapi.json` with onboarding paths and schemas.
- Added onboarding integration coverage for:
  - new-account status
  - accepted consent persistence
  - account type persistence
  - declined consent recording and blocking
  - backend business-route enforcement
  - accepted-consent withdrawal
  - declined-consent withdrawal rejection
- Updated existing organization and todo fixtures to represent completed accounts explicitly.

## Acceptance checklist

- [x] A new authenticated account receives an incomplete onboarding status.
- [x] The server returns the current consent requirements and policy metadata.
- [x] Individual/organization choice is persisted on completion.
- [x] Required accepted decisions create auditable historical records.
- [x] Declined required decisions are recorded and keep onboarding incomplete.
- [x] Withdrawal preserves history and reopens onboarding.
- [x] Direct API calls cannot bypass onboarding for protected organization, invitation, or todo
      operations.
- [x] The web flow blocks protected routes until onboarding is complete.
- [x] OpenAPI documents the onboarding API.
- [x] Mobile remains untouched.
- [x] No Redis, worker, malware scanner, email provider, or other external infrastructure was
      added.

## Verification

```bash
cd apps/backend
./mvnw -q test

cd ../..
pnpm --filter @app/frontend typecheck
pnpm --filter @app/frontend build
pnpm --filter @app/api-types check:generated
git diff --check
```

The backend suite uses Testcontainers PostgreSQL and applies Flyway migrations through V8.
The repository's Spotless formatter is known to fail in this environment under Java 25 because
the pinned Google Java Format version is incompatible; tests and compilation remain green.

## Follow-up

- Add the full evidence-event `consent.presented`/`consent.accepted` timeline when signing
  projects and participant sessions are implemented.
- Add a settings surface for reviewing and withdrawing current consents after onboarding.
- Replace engineering-draft policy text and references only through the appropriate production
  release gate.
