# ADR-008: VinciSign Document, Evidence, and Storage Architecture

- **Status:** Accepted for system construction
- **Date:** 2026-11-14
- **Scope:** API, simple document intake, storage, evidence, integrity, and repository naming

## Context

VinciSign is an API-first signing platform. Its core records are not ordinary CRUD rows: document bytes must remain recoverable, signed/notarized versions must not be silently replaced, evidence must be auditable, and partner retries must not duplicate business effects.

The current repository is a Spring Boot/PostgreSQL/Next.js template with Flyway, generated OpenAPI types, and local Docker infrastructure. It does not yet have a production document-storage, processing, evidence, or notification implementation. We need a foundation that works locally without locking the domain model to one vendor.

## Decisions

### 1. Keep one modular domain API for the first product

Use the Spring Boot API as the system boundary and organize product behavior as vertical modules: account/organization, project, document, participant/signing, evidence, certificate, verification, Vault, integration, credits, and notarization.

Keep module boundaries explicit in one deployable service until scale, security isolation, or operational evidence requires a separate service. The initial flow uses the versioned REST API; signed webhooks are a later integration extension.

### 2. PostgreSQL is the metadata and workflow system of record

Store relational metadata and authoritative state in PostgreSQL:

- Accounts, organizations, workspaces, memberships, and policies.
- Projects, documents, document versions, participants, fields, consents, and lifecycle state.
- Evidence event envelopes, sequence/hash metadata, certificates, verification results, retention/legal holds, and access records.
- API clients, webhook endpoints/deliveries, credits, reservations, and ledger entries.
- Notarization requests, ENP/ENF records, IEN/REN session state, certificates, and Electronic Notarial Book references.

Use Flyway for schema changes and keep JPA configured to validate rather than generate schema. Resource queries must include organization/workspace authorization predicates.

### 3. Object storage holds document bytes and large artifacts

Store original uploads, working versions, final signed/notarized documents, certificates, Passports, evidence exports, and restricted session artifacts in an object-storage boundary. PostgreSQL stores immutable object references, content type, size, fingerprint, encryption/key reference, processing status, and retention metadata—not the normal document bytes.

Use Supabase Storage through an object-storage adapter:

- Supabase Storage for the initial document/artifact bucket.
- Keep the adapter boundary so a future S3-compatible provider can be introduced without changing domain modules.
- Private buckets/containers, non-guessable keys, server-side encryption, least-privilege credentials, and signed short-lived access URLs.
- Explicit object lifecycle/retention controls that cannot delete an object under legal hold or active evidence retention.

The storage adapter must expose put, metadata, read/reference, copy-to-new-version, and policy-controlled delete operations without leaking Supabase-specific types into domain modules. Quarantine and restore are deferred until a security-scanning workflow is introduced.

### 4. Keep the initial document flow synchronous and PDF-only

The first implementation accepts only ready-to-use PDF/PDF-A files and completes the intake flow in the API request:

1. Validate file type, size, readability, and password/encryption state.
2. Store the ready PDF/PDF-A bytes in Supabase Storage.
3. Calculate SHA-256 over the exact stored bytes.
4. Persist the document version metadata and fingerprint in PostgreSQL.
5. Append upload and fingerprint evidence events.
6. Return the resulting intake status in the same response.

For this initial flow, the stored upload is the working version. A later version may introduce separate original/working artifacts, conversion, or security scanning as an explicitly approved extension.

Create a new DocumentVersion for every replacement or meaningful output. Lock the prepared version at send and lock final signed/notarized versions permanently.

No controller, client, provider callback, or admin action may overwrite bytes in place. Replacement means a new version plus an EvidenceEvent.

### 5. Use SHA-256 as the primary document fingerprint

Calculate SHA-256 over the exact stored bytes of every original, working, final, certificate, and evidence-export artifact. Store algorithm name, lowercase hex/digest representation, byte length, and the version/object reference.

The fingerprint proves byte identity for the referenced artifact. It does not, by itself, prove identity, legal enforceability, notarization, or that a person intended to sign. Those claims require the linked consent, identity, signature, certificate, and evidence records.

The design may add a second algorithm later through an explicit algorithm field; it must not change the meaning of an existing SHA-256 fingerprint.

### 6. Evidence is PostgreSQL-backed, append-oriented, and hash-linked

Persist the EvidenceEvent envelope in PostgreSQL with a per-project sequence/aggregate version and SHA-256 event hash. Link project events using `previousEventHash` where the project evidence chain is enabled. Store sensitive identity/location/video/payment data in restricted records and reference them from the event.

Use object storage for signed evidence exports, not as the only source of event truth. Export manifests include event sequence, schema versions, hashes, chain status, redaction/access summary, and a manifest fingerprint.

Normal application roles cannot edit/delete events. Corrections, reversals, revocations, and administrative actions create new events. Infrastructure logs remain complementary operational records.

### 7. Defer infrastructure-heavy processing and integrations

The initial flow deliberately does not require Redis, background workers, PDF conversion, malware scanning, notification providers, webhooks, or external synchronization. Fingerprinting and evidence recording are synchronous database operations in the same request boundary.

These capabilities may be added later behind separate, explicitly approved tickets. They are not dependencies of the first project/document flow and agents must not add them while implementing this baseline.

### 8. External integrity anchoring is optional and non-authoritative

Blockchain or another external anchoring service is not required for core integrity. The platform’s primary integrity model is immutable versioning, SHA-256 fingerprints, hash-linked evidence, append-oriented storage, restricted admin access, certificates, and exports.

An external anchor may be added later as a verifiable auxiliary reference. It must never replace PostgreSQL evidence, the final document fingerprint, the certificate, or the ordinary/notarial record separation.

### 9. Keep the confirmed Java package name

The confirmed Java package target is `com.vincisign.backend`. The current template names are retained during active foundation work until the coordinated migration ticket is executed:

- Java package: `com.vencentdev.backend` → `com.vincisign.backend`.
- Java module references, tests, Maven configuration, and source paths move together.
- Keep the current workspace `@app/*` package scope until a separate frontend migration decision is made.

This is a coordinated migration ticket before the first public release. Feature work must not introduce a mix of old and new package scopes.

## Consequences

### Benefits

- Domain state, evidence, billing, retention, and permissions remain queryable and transactionally consistent.
- Large/immutable bytes use the storage system suited to them without coupling business models to a provider.
- The first project/document flow is easy to run locally and easy for an integrating platform to understand.
- Original uploads, signed artifacts, and notarized artifacts have clear identity and retention behavior.
- Integrity does not depend on a single certificate or external blockchain network.
- Future provider changes do not require rewriting domain modules.

### Costs and risks

- The platform must operate database backups, object-storage backups, encryption/key management, retention, and restore tests.
- PDF/PDF-A is the initial supported input boundary; conversion and scanning are intentionally deferred.
- Hash-linked evidence detects integrity issues but does not automatically prove legal facts.
- The coordinated package rename is a broad, high-conflict change and must be isolated from feature development.

## Alternatives considered

### Store document bytes in PostgreSQL

This simplifies transactional inserts for tiny files but increases database size, backup cost, query/storage coupling, and streaming complexity. It does not fit a document platform expected to store final artifacts and evidence exports.

### Use a vendor-specific storage SDK throughout the domain

This might reduce initial adapter code but couples document and retention behavior to a provider, makes local testing harder, and makes provider migration affect every module.

### Use blockchain as the primary integrity store

This adds external availability, cost, privacy, and operational dependencies without replacing the need for document bytes, evidence, retention, certificates, and access control. It is not required for the initial product promise.

### Add workers and integrations before the core flow

This would require Redis, worker operations, conversion/scanning services, notification providers, and failure recovery before the first document can be stored. The initial product does not need those dependencies, so the simple synchronous flow is preferred.

### Split into microservices immediately

This could isolate processing and notary workloads, but it would add deployment, distributed transaction, tracing, and consistency cost before the domain boundaries are proven. Keep module boundaries explicit in one API service first.

## Implementation constraints

- Keep Supabase Storage behind an object-storage interface.
- Use Flyway migrations for metadata/evidence schema; do not add job/queue tables for the initial flow.
- Make object keys opaque and avoid using raw project/document IDs as guessable paths.
- Store policy/version and retention/legal-hold references with artifacts.
- Test duplicate requests, storage failures, partial finalization, restore, replacement, and deletion blocking.
- Never claim that a fingerprint or hash chain alone proves identity, intent, notarization, or legal enforceability.
