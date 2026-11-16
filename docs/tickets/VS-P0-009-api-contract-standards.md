# VS-P0-009 — Establish API Versioning and Contract Standards

**Status:** Done for current system-build scope  
**Phase:** Phase 0 — Product, legal, and architecture foundation  
**Window:** S04 / 2026-12-28 – 2027-01-08  
**Owner:** Product + Backend  
**Estimate:** 4 days  
**Dependencies:** VS-P0-006, VS-P0-007

## Goal

Define the public API standards for versioning, OpenAPI ownership, resource naming, authentication, errors, pagination, uploads, idempotency, state transitions, async operations, generated clients, and webhooks.

## Deliverable

- [`docs/api-contract-standards.md`](../api-contract-standards.md)

## Acceptance checklist

- [x] `/api/v1` path versioning and breaking/non-breaking/deprecation rules are defined.
- [x] Resource paths, JSON fields, IDs, timestamps, enums, nullability, and server-owned fields are defined.
- [x] Authentication, API scopes, organization/workspace authorization, and public verification limits are defined.
- [x] Request/response headers, methods, status codes, and structured error contract are defined.
- [x] Pagination, filters, sorting, stable ordering, uploads, async operations, and concurrency are defined.
- [x] Idempotency behavior covers duplicate writes, request fingerprints, conflicts, and scope.
- [x] Project transitions, document immutability, evidence, certificates, credits, and notarization API boundaries are defined.
- [x] OpenAPI generation, shared client updates, CI, and webhook boundary are defined.
- [x] No mobile files were changed.

## Notes and rulings

- **Ruling:** `/api/v1` is the single initial business API version; breaking changes require a new major path rather than endpoint-by-endpoint variants.
- **Ruling:** The existing `ApiError` record is the baseline error shape, with stable machine-readable codes and safe messages.
- **Ruling:** Project status changes use named transition commands; generic client status patches are prohibited.
- **Ruling:** OpenAPI generated from Spring is the contract source, with the committed JSON fallback used when a running API is unavailable.

## Verification

- `git diff --check`
- Confirm acceptance items against `docs/api-contract-standards.md`.
- Confirm API resource names agree with [`docs/domain-vocabulary.md`](../domain-vocabulary.md).
- Confirm error/transition behavior agrees with [`docs/project-lifecycle-policy.md`](../project-lifecycle-policy.md).
- Confirm no `apps/mobile` files are modified.
