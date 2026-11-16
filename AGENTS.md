# VinciSign Agent Instructions

## Product identity

This repository is **VinciSign**, an API-first document signing platform. It is intended to be integrated into other products through a versioned REST API, OAuth2/API clients, and signed webhooks.

The product capabilities are:

- **Sign** — create projects, collect signatures and approvals, and complete transactions.
- **Protect** — preserve document versions, fingerprints, consent, and evidence events.
- **Verify** — verify files, QR/document codes, certificates, and revocation status.
- **Passport** — expose document lifecycle and evidence history.
- **Vault** — store, organize, retrieve, archive, and share documents.

Read [`docs/product-charter.md`](docs/product-charter.md), [`docs/domain-vocabulary.md`](docs/domain-vocabulary.md), [`docs/project-lifecycle-policy.md`](docs/project-lifecycle-policy.md), [`docs/evidence-event-contract.md`](docs/evidence-event-contract.md), [`docs/api-contract-standards.md`](docs/api-contract-standards.md), [`docs/adr/0008-vincisign-platform-foundation.md`](docs/adr/0008-vincisign-platform-foundation.md), [`docs/security/threat-model-and-privacy-baseline.md`](docs/security/threat-model-and-privacy-baseline.md), [`docs/environment-baseline.md`](docs/environment-baseline.md), [`docs/project-brief.md`](docs/project-brief.md), [`implementation-plan.md`](implementation-plan.md), and [`docs/vincisign-gantt-timeline.md`](docs/vincisign-gantt-timeline.md), plus [`docs/doconchain-replication-plan.md`](docs/doconchain-replication-plan.md), before starting product work. The charter is the product decision baseline; the domain vocabulary is the naming/ownership baseline; the lifecycle policy is the state-transition baseline; the evidence contract is the audit/evidence baseline; the API contract is the integration baseline; ADR-008 is the storage/architecture baseline; the threat model is the security/privacy baseline; the environment baseline is the runtime-configuration baseline; the Gantt is the delivery baseline; the DocOnChain document is a functional reference only. Do not copy its branding, wording, source code, or proprietary design.

## Scope for the current phase

- Work on the web app, Spring Boot API, shared OpenAPI types, infrastructure, and product documentation.
- **Mobile is out of scope. Do not modify `apps/mobile` unless the user explicitly asks for mobile work.**
- The first implementation is intentionally simple: use the existing Spring Boot API, PostgreSQL, and Supabase Storage only. Do not add Redis, workers, malware scanners, document-conversion services, notification providers, webhooks, or external synchronization unless a new ticket explicitly reopens one of those decisions.
- The existing todo module and todo UI are template smoke tests, not VinciSign product requirements. Replace them with document-project functionality when implementing the first vertical slice; do not expand the todo feature.
- Do not claim ordinary electronic signatures are notarization or universally legally enforceable. Electronic notarization is a separate regulated workflow requiring legal and accreditation review.

## Immediate product target

Replace the todo smoke test with the first document-project slice:

1. `POST /api/v1/projects` creates an organization-owned Draft project and honors an idempotency key.
2. `POST /api/v1/projects/{id}/documents` accepts a ready PDF/PDF-A, stores it in Supabase Storage, fingerprints it, records evidence, and returns the result synchronously.
3. `GET /api/v1/projects` and `GET /api/v1/projects/{id}` provide organization-scoped project access.
4. `GET /api/v1/projects/{id}/events` exposes the evidence timeline.
5. The authenticated web dashboard lists projects and upload state.

## Engineering rules

- Use vertical backend modules under `apps/backend/src/main/java/com/vencentdev/backend/modules/` until the coordinated package rename is explicitly undertaken.
- Use Java 21, Spring Boot 4, Maven, PostgreSQL, Flyway, Lombok entities, Java record DTOs, MapStruct, and integration tests with Testcontainers.
- Treat document versions as immutable after signing or notarization. Replacement means a new version plus an evidence event.
- For the initial document flow, the accepted PDF/PDF-A upload is the working version; DOCX conversion and malware scanning are deferred.
- Scope every resource query and mutation to the caller’s organization. Never trust organization ownership from an untrusted request body.
- Use idempotency for side-effecting public API writes and signed, retryable, replay-resistant webhooks for integration events.
- Keep API contracts in OpenAPI and regenerate shared client types after contract changes.
- Frontend server data belongs in TanStack Query; auth/session state belongs to Auth.js; ephemeral UI state belongs to Zustand.
- Preserve unrelated user changes and do not rename the Java package or `@app/*` package scope piecemeal.

## Verification expectations

Before claiming work is complete, run the narrowest relevant tests plus `git diff --check`. If dependencies are not installed, report that validation limitation explicitly. For API work, cover organization isolation, idempotency, immutable versions, structured errors, and evidence events.
