# VinciSign API Versioning and Contract Standards

**Contract ID:** `API-CONTRACT-0.1`  
**Status:** Approved for system construction  
**Confirmed:** 2026-11-14  
**Applies to:** Public REST API, first-party web client, partner integrations, generated clients, and webhooks

## 1. Purpose

This document defines the contract rules for VinciSign’s API. It makes the API usable by partner platforms, keeps first-party clients honest consumers of the same contract, and gives implementations a stable boundary for authentication, errors, versioning, retries, files, state transitions, and webhooks.

The Spring Boot API is the domain API. The Next.js application must not create business-resource route handlers that bypass this contract.

## 2. API principles

1. The API is versioned, documented, and usable without the VinciSign web UI.
2. OpenAPI is the source of truth for request/response shapes, security, errors, parameters, and examples.
3. Resource ownership and authorization are enforced server-side from the authenticated account/API client, never from trusted request-body owner fields.
4. Side-effecting writes are idempotent when retries could duplicate projects, signatures, payments, credits, certificates, or webhook effects.
5. Document versions, evidence events, certificates, and ledger entries are not silently overwritten through an API update.
6. Project state changes use named transition commands, not arbitrary status patches.
7. Webhook events are versioned, signed, replay-resistant, and separate from synchronous API response contracts.
8. API errors are safe, structured, traceable, and stable enough for programmatic handling.

## 3. Base URL and versioning

### Version 1 path

All business resources use:

```text
/api/v1/{resource}
```

Examples:

```text
/api/v1/projects
/api/v1/projects/{projectId}/documents
/api/v1/projects/{projectId}/participants
/api/v1/projects/{projectId}/events
/api/v1/projects/{projectId}/certificates
/api/v1/projects/{projectId}/verifications
/api/v1/api-clients
/api/v1/webhook-endpoints
/api/v1/credit-ledger
```

Health, OpenAPI, and Swagger endpoints remain operational endpoints and are not customer business resources:

```text
/actuator/health
/v3/api-docs
/swagger-ui
```

### Versioning rules

- Breaking resource, field, semantic, authorization, or error-contract changes require a new major API path such as `/api/v2`.
- Additive response fields, optional request fields, new endpoints, and new webhook event types are non-breaking only when existing clients remain safe.
- Do not version individual endpoints independently.
- Do not make a breaking change to `/api/v1` because the first-party UI has been updated.
- Every response and webhook includes a contract/schema version where consumers need to distinguish payload shapes.
- The server may support multiple major versions during a documented migration window.
- A deprecated endpoint or field remains functional until the published removal date and migration path pass the release gate.

### Deprecation requirements

Deprecation must include `Deprecation` and, when known, `Sunset` response headers, OpenAPI `deprecated: true`, a migration guide, usage telemetry, customer notification, and a removal ticket with compatibility and rollback review.

## 4. Resource and field naming

- Use plural kebab-case resource paths: `/projects`, `/api-clients`, `/webhook-endpoints`.
- Use lower camelCase JSON fields: `projectId`, `documentVersionId`, `createdAt`.
- Use stable opaque IDs in public contracts; do not expose database sequence assumptions.
- Use ISO 8601/RFC 3339 timestamps with an explicit offset or UTC `Z`.
- Use strings for enum values and document unknown-value handling.
- Use `null` only when null has a defined meaning; omit optional fields when absent unless the contract says otherwise.
- Do not expose JPA entities directly.
- Do not accept trusted `ownerId`, `organizationId`, `createdBy`, audit fields, fingerprint, status, certificate status, or eligibility status from client request bodies when the server derives them.
- Use exact domain names from [`domain-vocabulary.md`](domain-vocabulary.md): `DocumentVersion`, `EvidenceEvent`, `ConsentRecord`, `APIClient`, `CreditLedgerEntry`, `ENP`, `ENF`, `IEN`, `REN`, and `ENB`.

## 5. Authentication and authorization

### Authentication

- Interactive first-party access uses the configured OAuth2/Keycloak bearer token.
- Partner API access uses an organization-scoped API client credential/token with explicit scopes.
- Public verification endpoints are limited to the permitted verification identity and never expose unrestricted project evidence.
- Health and API documentation endpoints follow the environment’s operational exposure policy.

### Initial scopes

```text
projects:read
projects:write
documents:read
documents:write
participants:read
participants:write
signatures:read
signatures:write
verification:read
verification:write
certificates:read
webhooks:manage
credits:read
credits:write
notarization:read
notarization:write
organization:admin
audit:read
```

Scopes narrow access; they do not replace organization membership, workspace policy, participant role, project state, identity level, ENP/ENF eligibility, or retention/legal-hold checks.

### Authorization rules

- Resolve organization/workspace scope from the authenticated subject and API client.
- Query repositories with the authorized scope predicate.
- Enforce ownership and participant permissions in services, not only in controllers.
- Return safe `404` or `403` behavior according to the resource exposure policy; do not leak another organization’s metadata.
- Record access to sensitive documents, evidence, identity, location, notarial, certificate, and billing data.

## 6. Request and response headers

| Header | Direction | Rule |
|---|---|---|
| `Authorization` | Request | Bearer token/API credential according to auth mode |
| `Accept` | Request | Client-supported response media type |
| `Content-Type` | Request | `application/json` for JSON; multipart or signed-upload protocol for files |
| `Idempotency-Key` | Request | Required for side-effecting public writes that can be retried |
| `X-Request-Id` | Request/response | Client may provide a safe correlation ID; server returns/records the effective request ID |
| `X-Correlation-Id` | Request/response | Correlates one business workflow across requests/jobs/providers |
| `ETag` | Response | Optional/current aggregate version for concurrency-sensitive resources |
| `If-Match` | Request | Required where the operation must confirm the caller’s expected resource version |
| `Deprecation` | Response | Present for deprecated endpoint/field migration |
| `Sunset` | Response | Present when a removal date is published |

Server-generated request IDs must not be accepted as proof of idempotency. `Idempotency-Key` and request fingerprinting provide duplicate protection.

## 7. HTTP methods and status codes

| Method | Use |
|---|---|
| `GET` | Read a permitted resource or collection; no business side effects |
| `POST` | Create a resource, submit a command, upload, verify, or start an operation |
| `PATCH` | Partial update only for explicitly mutable draft/configuration resources; never arbitrary project status |
| `PUT` | Full replacement only when the resource contract explicitly supports it; do not use for signed bytes/evidence |
| `DELETE` | Policy-controlled deletion/revocation of a mutable resource; never silent deletion of signed/notarized evidence |

Initial response meanings:

```text
200 OK                  Successful read/update/command result
201 Created             New resource created
202 Accepted            Async processing accepted; poll or follow webhook
204 No Content          Successful operation with no response body
400 Bad Request         Malformed or invalid request
401 Unauthorized        Authentication missing/invalid
403 Forbidden           Authenticated but not allowed
404 Not Found           Resource unavailable to the caller/policy
409 Conflict            State, idempotency, uniqueness, or concurrency conflict
413 Content Too Large   Upload exceeds configured limit
415 Unsupported Media   Unsupported content type
422 Unprocessable       Valid shape but policy/workflow validation failed
429 Too Many Requests   Rate limit exceeded
500 Internal Error      Unexpected server failure
502/503/504             Upstream/dependency or temporary service failure
```

## 8. Error contract

The existing backend `ApiError` shape is the baseline:

```json
{
  "status": 409,
  "code": "IDEMPOTENCY_CONFLICT",
  "message": "The idempotency key was already used with a different request.",
  "traceId": "req_01...",
  "timestamp": "2026-11-14T08:30:00Z",
  "errors": [
    {
      "field": "Idempotency-Key",
      "message": "Use a new key for a different request."
    }
  ]
}
```

Rules:

- `code` is stable and machine-readable; clients must not parse `message`.
- `message` is safe, concise, and appropriate for the caller.
- `traceId` or request correlation lets support locate the operation.
- `timestamp` is server-generated.
- `errors` contains field/path-level validation only when useful and safe.
- Do not expose stack traces, SQL, secrets, provider credentials, internal object keys, or private identity/location details.
- Use stable policy codes such as `FILE_MALWARE_DETECTED`, `CONSENT_REQUIRED`, `SIGNATURE_REPLAY_DETECTED`, `NOTARIZATION_CONTROL_FAILED`, and `CERTIFICATE_REVOKED`.
- Errors that may be retried must say so through documented code/metadata or endpoint documentation.

## 9. Collections, pagination, filtering, and sorting

Collection endpoints use a consistent page contract:

```json
{
  "content": [],
  "page": 0,
  "size": 25,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

Rules:

- Default page size is 25; maximum page size is 100 unless a resource documents another safe limit.
- Use zero-based `page` and positive `size`.
- Filtering uses named query parameters such as `status`, `createdAfter`, `createdBefore`, `participantId`, `folderId`, or `jurisdictionProfile`.
- Sorting uses an allow-listed field and direction such as `sort=createdAt,desc`.
- Every list query applies organization/workspace authorization before filtering or counting.
- Stable ordering is required; use a unique tie-breaker when timestamps match.
- Do not expose raw database query syntax.

## 10. Idempotency and concurrency

For side-effecting public writes:

1. Require `Idempotency-Key` with a documented retention period.
2. Hash the normalized request payload and relevant auth/scope context.
3. Store the original key, fingerprint, result status, response, resource IDs, and expiration.
4. Return the original result for the same key and fingerprint.
5. Return `409 IDEMPOTENCY_CONFLICT` for the same key with a different payload or scope.
6. Ensure only one project, signature, certificate, payment callback, reservation, or webhook event is created.
7. Pair idempotency with aggregate-version/`If-Match` checks where concurrent edits matter.

Idempotency does not bypass current authorization or lifecycle guards. A replay returns the original result only when the stored operation belongs to the same authorized scope and contract version.

## 11. Resource contract rules

### Projects

- `POST /api/v1/projects` creates an organization-owned Draft and accepts an external reference.
- Project creation honors idempotency and ignores client-supplied ownership/audit fields.
- Project state changes use named commands; no generic `PATCH status`.
- Project responses expose current state, policy version, resource IDs, timestamps, and pending async work where relevant.

### Documents

- Upload contracts preserve original metadata and return document/version processing status.
- Large files may use an authorized signed-upload flow, but final metadata/ownership is committed through the API.
- Upload responses expose stable policy/error codes, not scanner internals.
- Signed/notarized bytes are immutable and cannot be replaced by `PUT` or `PATCH`.

### Participants, fields, and consent

- Participant APIs accept contact/role/action/order requirements but not trusted identity proof or server-owned status.
- Field APIs are mutable only until the prepared version is locked.
- Consent APIs expose policy/version references and current status without allowing clients to forge acceptance timestamps or identity evidence.

### Evidence, certificates, and verification

- Evidence is read/export oriented; normal clients cannot update/delete events.
- Certificates reference final document versions and evidence; certificate status changes are named operations.
- Verification endpoints explain what was checked and create an evidence event.

### Notarization

- Notarization requests are separate resources with IEN/REN mode and eligibility state.
- ENP/ENF, identity, location, session, certificate, and ENB checks remain server-owned.
- Notarization completion can be asynchronous and returns a status resource/webhook path.

## 12. Initial file-upload contract

The initial document path is a direct multipart upload to the API. The API accepts only ready PDF/PDF-A files, validates size/readability/password state, stores the bytes in Supabase Storage, calculates SHA-256, persists the DocumentVersion metadata, appends the upload/fingerprint evidence events, and returns the result synchronously.

The initial path does not use signed upload URLs, external file references, scan status, normalization status, background workers, or `202 Accepted` processing responses. Those are future extensions and require a separate architecture decision before implementation.

## 13. OpenAPI and generated clients

- Spring controllers/DTOs and OpenAPI annotations define the contract.
- `/v3/api-docs` is the generated source consumed by `packages/api-types`.
- `packages/api-types/openapi.json` is the deterministic fallback when a running API is unavailable.
- Every contract change updates OpenAPI and generated TypeScript types; Dart/client artifacts are updated when those clients are in scope.
- CI fails on stale generated types, invalid schema, undocumented error responses, or missing security requirements.
- Examples include success, validation failure, authorization failure, idempotency conflict, and async response where relevant.

## 14. Webhook boundary

Webhooks are not a replacement for API reads. A webhook contains stable event ID, event type/version, timestamps, resource references, delivery metadata, and a signed payload. Consumers validate the event and retrieve current resource state through the API.

Webhook delivery preserves the EvidenceEvent/business-event ID across retries and redelivery; delivery attempts are separate records.

## 15. Contract acceptance

This standard is complete for the current system-build scope when:

- `/api/v1` versioning and breaking/non-breaking rules are explicit.
- Resource, field, timestamp, enum, auth, scope, header, method, and status-code rules are explicit.
- Error, pagination, filtering, sorting, upload, async, idempotency, and concurrency contracts are explicit.
- Project transitions, document immutability, evidence, certificates, credits, and notarization boundaries are explicit.
- OpenAPI generation, shared-client updates, deprecation, and CI expectations are explicit.
- Webhook/API responsibilities are separated.
