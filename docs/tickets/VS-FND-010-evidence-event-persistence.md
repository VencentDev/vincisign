# VS-FND-010 — Implement Evidence Event Persistence and Access API

**Status:** Done
**Phase:** Phase 1 — Accounts, organizations, projects, documents, storage, and evidence
**Owner:** Backend
**Dependencies:** VS-P0-008, VS-FND-005, VS-FND-007, VS-FND-008
**Mobile:** Not in scope; no mobile files may be changed

## Goal

Persist an append-only evidence timeline for the first synchronous document-intake flow and make
that timeline available to an authorized project owner through the versioned API. The timeline
must preserve the accepted document/version identity, SHA-256 fingerprint, actor, policy, request
trace, event ordering, and safe event payload without adding workers, scanners, Redis, webhooks, or
other infrastructure.

## API boundary

```text
GET /api/v1/projects/{projectId}/events
```

The endpoint requires authentication and returns events ordered by their database-assigned
sequence number. It only returns events for a project the authenticated caller owns under the
current foundation authorization model. A cross-owner request must not reveal the project's event
history.

## Initial event set

The accepted upload flow records these events synchronously:

1. `document.validation_started` — written before PDF validation.
2. `document.validation_failed` — written in a separate transaction when validation rejects the
   upload; the payload contains only the stable rejection policy code.
3. `document.uploaded` — records the persisted document/version references and safe media/size
   metadata.
4. `document.fingerprinted` — records the SHA-256 algorithm and fingerprint.
5. `document.version_created` — records the initial version number, role, and status.

The successful flow emits events in the same database transaction as document/version persistence.
Validation failures are preserved independently so a rolled-back upload transaction does not erase
the rejection fact.

## Scope

- Flyway migration for an `evidence_events` append-oriented table.
- Database-assigned global sequence number for deterministic event ordering.
- Event envelope fields from `docs/evidence-event-contract.md`: event identity/version, occurrence
  and recording timestamps, organization/workspace/project/document/version references, actor and
  source context, request/correlation trace, policy context, reason code, event hash, and JSONB
  payload.
- PostgreSQL trigger protection against normal application `UPDATE` or `DELETE` mutations.
- Evidence-event writer service with SHA-256 event hashes and safe payload construction.
- Upload-service wiring for validation, upload, fingerprint, and initial-version events.
- Owner-authorized project event read endpoint returning DTOs rather than entities.
- Focused unit tests for event construction, event hashing, upload event wiring, and ownership
  isolation; persistence tests for the append-only database guard where Testcontainers is
  available.

## Explicitly out of scope

- Malware scanning, quarantine, normalization, conversion, workers, Redis, or any new runtime
  infrastructure. VS-FND-009 remains deferred.
- Request idempotency storage and replay responses; VS-API-005 owns public write idempotency.
- Admin investigation views, evidence export, integrity verification jobs, webhooks, notifications,
  signing events, notarization events, and certificate generation.
- Frontend and mobile changes.

## Required behavior

1. Every successful accepted upload has validation, upload, fingerprint, and version-created facts
   tied to the same project, document, and version identifiers.
2. A validation rejection records a safe stable policy code without storing file contents,
   credentials, storage URLs, or parser internals.
3. Event rows are append-only to normal application code; corrections must be represented by later
   events rather than mutation.
4. Event sequence numbers are assigned by the database and project reads return ascending order.
5. Event hashes are lowercase 64-character SHA-256 values over the persisted event identity/context
   and canonical JSON payload.
6. Actor identity, organization/project ownership, policy metadata, and request trace are derived
   from server-side state or the authenticated request context.
7. The read endpoint enforces project ownership before querying or returning event history.
8. Event persistence failure rolls back the related accepted upload rather than leaving a document
   without its required evidence facts.
9. No scanner, worker, notification, webhook, Redis, conversion, or mobile implementation is added.

## Acceptance checklist

- [x] `evidence_events` migration applies cleanly after the project/document schema.
- [x] Event entity/repository/service persist the contract envelope and JSONB payload.
- [x] Append-only trigger rejects direct `UPDATE` and `DELETE` mutations.
- [x] Successful upload emits the four required successful-flow events with correct references.
- [x] Validation rejection emits a separate failure event with its stable policy code.
- [x] `GET /api/v1/projects/{projectId}/events` returns ordered DTOs for the owner.
- [x] Cross-owner event reads are rejected without returning event data.
- [x] Focused tests, application context/Flyway tests, and `git diff --check` pass.
- [x] No mobile, scanner, worker, Redis, notification, webhook, or unrelated feature changes are
      introduced.

## Implementation notes

- Added Flyway `V10__evidence_events.sql` with the event envelope, JSONB payload, database sequence,
  project-scoped indexes, and append-only mutation trigger.
- Added the evidence event writer/read service and owner-only `GET /api/v1/projects/{projectId}/events`
  endpoint.
- Wired synchronous upload validation, validation rejection, upload, fingerprint, and initial
  version events into the document intake flow. Rejection events use a separate transaction.
- Kept event payloads safe and infrastructure-free. Malware scanning remains deferred under
  VS-FND-009; request idempotency remains owned by VS-API-005.

## Verification

Passed:

```text
./mvnw -q -Dtest='*EvidenceEventServiceTest,*EvidenceEventPersistenceIntegrationTest,*DocumentUploadServiceTest' test
./mvnw -q -Dtest=BackendApplicationTests test
git diff --check
```

The full backend suite was also attempted. It reached 80 tests with zero assertion failures, but
9 existing tests errored during shared Testcontainers cleanup because `users.deleteAll()` can run
while organizations still reference the user (`organizations_owner_user_id_fkey`). That unrelated
test-isolation problem is not introduced by VS-FND-010. Spotless remains blocked by the repository's
known Java 25/Google Java Format incompatibility (`Log$DeferredDiagnosticHandler` API mismatch).

## Follow-up

- VS-FND-011 will record project lifecycle transition events through this service.
- VS-API-005 will add public request idempotency and deterministic replay behavior for side-effecting
  API writes.
- VS-VERIFY-001 will add independent event-chain integrity verification.
- VS-FND-014 will add restricted admin investigation access and evidence access logging.
