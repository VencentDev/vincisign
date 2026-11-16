# VinciSign Foundation Implementation Plan

VinciSign is an API-first document signing and evidence platform. This plan converts the existing full-stack template into the first product foundation: a simple document-project vertical slice, then signing and verification.

The full start-to-finish schedule, release gates, dependencies, and ticket catalog are in [`docs/vincisign-gantt-timeline.md`](docs/vincisign-gantt-timeline.md).

## Product rules

- The public product name is **VinciSign**.
- The product is independent and must not copy DocOnChain branding, source code, wording, or proprietary design.
- The core record is a **project** containing document versions, participants, fields, settings, evidence, certificates, and final artifacts.
- Signed and notarized versions are immutable. A replacement is a new version.
- Notarization is a separate regulated workflow and must not be implied by ordinary electronic signing.
- External integrations use versioned REST APIs, OAuth2/API clients, idempotency keys, signed webhooks, and generated OpenAPI clients.

## Initial build constraint

The first implementation uses only the existing Spring Boot API, PostgreSQL, and Supabase Storage. The initial document flow is synchronous and accepts ready PDF/PDF-A files, stores the bytes, calculates SHA-256, records metadata/evidence, and returns a response. Redis, background workers, DOCX/PDF conversion, malware scanning, notification providers, webhooks, and external synchronization are deferred until a separate ticket explicitly enables them.

## Delivery phases

### Phase 0 — Foundation cleanup

- Replace template branding and product copy with VinciSign language.
- Define the product brief, legal boundary, API principles, and naming conventions.
- Move the template todo feature behind an explicit smoke-test label; do not expose it as a product capability.
- Coordinate the Java package and npm package-scope migration before the first public release.
- Keep local Postgres, Keycloak, Flyway, OpenAPI generation, CI, and test commands green.

### Phase 1 — Accounts, organizations, and documents

- Accounts, organization/workspace membership, roles, API clients, and permissions.
- Document upload with PDF/PDF-A validation, Supabase Storage persistence, immutable working version, and fingerprint.
- Projects with controlled lifecycle states and evidence events for every meaningful action.

### Phase 2 — Digital signing

- Signature, initials, date, text, checkbox, attachment, approval, witness, and notary fields.
- Sequential and concurrent participants, guest signing, account-required signing, deadlines, reminders, and access policies.
- Signing session security, consent capture, completion certificate, and finalized document artifact.

### Phase 3 — Protect, Verify, and Passport

- Fingerprint and certificate validation, QR/document-code verification, upload verification, and revocation.
- Active, completed, and notarized passport views.
- Evidence export suitable for audits, disputes, and enterprise compliance review.

### Phase 4 — Vault and enterprise controls

- Folders, search, storage quotas, retention, legal holds, archive, sharing, custom branding, advanced permissions, audit exports, and administrative review.

### Phase 5 — Integration platform

- Organization-scoped API clients, scopes, rate limits, usage metering, credits/plans, reservations, refunds, and reporting.
- Idempotent project creation and signing operations.
- Signed webhooks with retries, delivery history, replay protection, manual redelivery, and observability.

### Phase 6 — Electronic notarization readiness

- Only after jurisdictional/legal review and accreditation requirements are satisfied.
- ENP/ENF onboarding, IEN/REN workflows, identity/location/session controls, notarial certificates, Electronic Notarial Book records, official synchronization, and commission monitoring.

## First engineering slice

The first product slice should replace the todo smoke test with:

1. `POST /api/v1/projects` — create a Draft project with organization ownership and idempotency.
2. `POST /api/v1/projects/{id}/documents` — upload and fingerprint the original document.
3. `GET /api/v1/projects` and `GET /api/v1/projects/{id}` — list and inspect projects scoped to the caller’s organization.
4. `GET /api/v1/projects/{id}/events` — expose the evidence timeline.
5. An authenticated web dashboard showing projects and upload state.

The slice is complete when organization isolation, immutable document metadata, idempotent creation, structured errors, OpenAPI types, and integration tests are in place.

## Verification scenarios

- A user cannot access another organization’s project or document.
- Repeating a create request with the same idempotency key returns the original result without a duplicate project.
- A completed document cannot be edited in place.
- Replacing an uploaded document creates a new version and evidence event.
- A failed upload does not leave a project falsely marked as ready.
- Future webhook delivery, when explicitly enabled, can be retried without duplicate downstream effects.
- Ordinary signing evidence is not labeled as notarization.
