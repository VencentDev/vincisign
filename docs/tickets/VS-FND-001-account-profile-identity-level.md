# VS-FND-001 — Add Account Profile and Identity-Level Model

**Status:** Done for current system-build scope  
**Phase:** Phase 1 — Accounts, organizations, projects, documents, storage, and evidence  
**Window:** S05 / 2027-01-11 – 2027-01-22  
**Owner:** Backend  
**Estimate:** 5 days  
**Dependencies:** VS-P0-004, VS-P0-006

## Goal

Create the first account-profile foundation for the API without adding a KYC subsystem. The account must expose an explicit identity level and an independent contact-verification state so later verification workflows can add evidence without conflating different types of proof.

## Delivered

- Added the `IdentityLevel` model with I0 through I6, matching the approved identity policy.
- Added the independent `ContactVerificationStatus` model and nullable verification timestamp.
- Added a Flyway migration with non-null defaults and database-level vocabulary checks.
- Added `GET /api/v1/accounts/me` using the existing authenticated account lookup.
- Added the new fields to the API response and tracked OpenAPI fallback contract.
- Removed the template-only KYC field from the active account model; identity assurance is represented by the explicit identity-level model.
- Kept the implementation synchronous and local to the existing backend/PostgreSQL flow; no mobile, Redis, worker, or external identity provider work was added.

## Acceptance checklist

- [x] A new account defaults to identity level `I0` (unverified account).
- [x] Contact verification is represented separately as `UNVERIFIED`, `PENDING`, `VERIFIED`, `EXPIRED`, or `REVOKED`.
- [x] Government-ID, organization, notary, and enterprise assurance levels have distinct identity-level values and are not conflated with contact verification.
- [x] Contact verification timestamp is nullable and is not fabricated for an unverified account.
- [x] `GET /api/v1/accounts/me` returns the account profile and independent identity/contact states.
- [x] Existing `/api/v1/users/me` and `/api/v1/auth/me` response compatibility is preserved with additive identity/contact fields and no KYC field.
- [x] Database defaults and checks prevent invalid identity or contact-verification values.
- [x] No `apps/mobile` files were changed.

## Verification

- Focused `UserControllerIntegrationTest` passed after implementation.
- Full backend test suite passed.
- Spotless formatting check passed.
- OpenAPI JSON parsing and repository diff checks passed.
