# VS-FND-011 — Implement Project Lifecycle Transition Service

**Status:** Done
**Phase:** Phase 1 — Accounts, organizations, projects, documents, storage, and evidence
**Owner:** Backend
**Dependencies:** VS-P0-007, VS-FND-005, VS-FND-010
**Mobile:** Not in scope; no mobile files may be changed

## Goal

Replace arbitrary project status mutation with named, server-side transition commands. Every
successful transition must atomically advance the project aggregate version and record the
corresponding lifecycle evidence event. The initial implementation uses the existing owner
authorization model and does not add participant, scheduler, notification, or idempotency
infrastructure.

## API boundary

```text
POST /api/v1/projects/{projectId}/transitions
```

Request:

```json
{
  "action": "prepare",
  "expectedAggregateVersion": 1,
  "reasonCode": "SENDER_CONFIGURATION_COMPLETE"
}
```

The server derives the actor and ownership from authentication. Clients cannot submit a new
status, owner, organization, or workspace. Public idempotency replay remains VS-API-005; this
ticket uses the expected aggregate version for optimistic concurrency.

## Supported commands

- Preparation: `prepare`, `reopen-document`, `add-participants`, `correct-participants`,
  `mark-ready`, `edit-before-send`.
- Workflow: `send`, `view`, `partially-sign`, `reopen-approval`, `await-approval`,
  `await-notarization`, `start-notarization`, `complete`.
- Exceptional/admin-controlled: `cancel`, `decline`, `expire`, `reject`, `suspend`, `resume`,
  `archive`, `revoke`.

The transition matrix is implemented in backend code from the approved project lifecycle policy.
The `DRAFT → DOCUMENT_UPLOADED` transition is internal to accepted document upload and is not a
client-controlled arbitrary status patch.

## Required behavior

1. Invalid command/state pairs fail with `PROJECT_TRANSITION_NOT_ALLOWED`.
2. Unsupported commands fail with `PROJECT_TRANSITION_INVALID_ACTION`.
3. A stale expected aggregate version fails with `PROJECT_VERSION_CONFLICT` and leaves the project
   unchanged.
4. The project owner is required for the current foundation API; cross-owner callers cannot
   transition the project.
5. `cancel`, `decline`, `expire`, `reject`, `suspend`, and `revoke` require a stable reason code.
6. Suspension stores the prior eligible state and `resume` restores it.
7. Terminal projects cannot return to draft or accept later ordinary transitions.
8. Project update and evidence-event persistence commit or roll back together.
9. Successful events use the lifecycle taxonomy, include previous/current state and aggregate
   versions, and return the evidence event ID.
10. Accepted uploads advance a draft project to `DOCUMENT_UPLOADED`; cancelled, expired,
    suspended, completed, archived, and revoked projects cannot accept a new upload.
11. No mobile, Redis, worker, scanner, notification, webhook, or unrelated infrastructure change
    is introduced.

## Acceptance checklist

- [x] Named transition endpoint and DTOs are implemented.
- [x] Server-side allowed-transition matrix follows `docs/project-lifecycle-policy.md`.
- [x] Optimistic aggregate-version checks and stable transition errors are implemented.
- [x] Owner authorization is enforced before mutation.
- [x] Suspension prior-state persistence and resume behavior are implemented.
- [x] Every successful transition records the lifecycle event atomically.
- [x] Accepted upload advances the project lifecycle and blocked project states reject uploads.
- [x] Unit and Testcontainers tests cover valid, invalid, stale-version, ownership, terminal, and
      evidence behavior.
- [x] No mobile or deferred infrastructure features are added.

## Implementation notes

- Added the owner-authorized `POST /api/v1/projects/{projectId}/transitions` endpoint with named
  actions and expected aggregate-version validation.
- Added the server-side lifecycle matrix, stable `PROJECT_*` transition errors, and JPA optimistic
  locking handling.
- Added `suspended_from_status` persistence so authorized resume restores the recorded prior state.
- Transition changes flush the project and write the corresponding lifecycle evidence event in the
  same transaction. Responses include the transition ID and evidence event ID.
- Accepted document uploads now perform the internal `DRAFT → DOCUMENT_UPLOADED` transition;
  blocked project states are rejected before validation/storage work begins.
- Updated the OpenAPI snapshot and regenerated `@app/api-types` declarations.

## Verification

Passed:

```text
./mvnw -q -Dtest=ProjectTransitionServiceTest,DocumentUploadServiceTest,EvidenceEventServiceTest test
./mvnw -q -Dtest=ProjectTransitionIntegrationTest test
./mvnw -q -Dtest=BackendApplicationTests test
./mvnw -q -DskipTests compile
pnpm --filter @app/api-types generate:types
python -m json.tool packages/api-types/openapi.json
git diff --check
```

The focused unit suite, the four-case Testcontainers lifecycle/HTTP suite, and the Spring context
test all pass. `spotless:check` remains blocked by the repository's known Java 25/Google Java Format
incompatibility (`Log$DeferredDiagnosticHandler.getDiagnostics()` API mismatch), including on the
pre-existing `BackendApplicationTests.java` file.

## Explicitly out of scope

- Public idempotency-key replay storage; VS-API-005.
- Participant role/membership guards; the participant module is not implemented yet.
- Scheduler-driven expiration, notification delivery, webhook delivery, signing field validation,
  notarization session validation, certificates, and finalization artifacts.
- Admin investigation UI and access logging; VS-FND-014.

## Follow-up

- VS-API-005 will add idempotency-key persistence and deterministic replay responses.
- Participant and signing tickets will add role, consent, identity, field, and version guards to
  the named commands.
- VS-FND-012 will expose lifecycle state in the organization dashboard.
