# VinciSign Evidence Event Contract

**Contract ID:** `EVIDENCE-EVENT-0.1`  
**Status:** Approved for system construction  
**Confirmed:** 2026-11-14  
**Applies to:** Project evidence, document integrity, signing, verification, notarization, billing, integration, and administration

## 1. Purpose

An EvidenceEvent is the structured, append-oriented record of a meaningful action or security state involving a VinciSign project, document, participant, identity check, certificate, verification, credit operation, integration, or administrative control.

Evidence events are part of the product’s evidence package. They are not a replacement for infrastructure logs, and infrastructure logs are not a replacement for evidence events.

## 2. Contract principles

1. Every meaningful user-visible or security-sensitive action creates an event.
2. Events describe facts that occurred; mutable status belongs on the related resource.
3. Events are append-only for normal application roles.
4. An event references the exact project/document version/policy version relevant to the action.
5. Replays and duplicate requests do not create duplicate business facts.
6. Event order, chain linkage, and integrity mismatches are detectable.
7. A failed action may create a failure/block event without falsely advancing resource state.
8. Event payloads are access-controlled and minimize sensitive data.
9. Administrative investigation cannot silently modify the events being investigated.
10. Events can be exported with enough metadata to verify the evidence package independently of a UI screen.

## 3. Event envelope

Every event uses the following logical contract:

```json
{
  "eventId": "evt_01...",
  "eventType": "signature.applied",
  "eventVersion": 1,
  "occurredAt": "2026-11-14T08:30:00Z",
  "recordedAt": "2026-11-14T08:30:01Z",
  "timezone": "Asia/Manila",
  "organizationId": "org_...",
  "workspaceId": "wsp_...",
  "projectId": "prj_...",
  "documentId": "doc_...",
  "documentVersionId": "ver_...",
  "participantId": "ptc_...",
  "actor": {
    "type": "ACCOUNT",
    "id": "acct_...",
    "role": "SIGNER",
    "identityLevel": "I2"
  },
  "source": "USER",
  "requestId": "req_...",
  "correlationId": "cor_...",
  "idempotencyKey": "idem_...",
  "aggregateVersion": 12,
  "previousState": "VIEWED",
  "newState": "PARTIALLY_SIGNED",
  "policyVersion": "SIGNATURE-IDENTITY-0.1",
  "reasonCode": null,
  "deviceContextRef": "ctx_...",
  "previousEventHash": "sha256:...",
  "eventHash": "sha256:...",
  "payload": {}
}
```

### Required envelope fields

| Field | Requirement |
|---|---|
| `eventId` | Stable unique ID generated once; reused on delivery retries |
| `eventType` | Namespaced past-tense fact name |
| `eventVersion` | Schema version for the event payload |
| `occurredAt` | Time the action happened or provider reported it |
| `recordedAt` | Time VinciSign persisted the event |
| `timezone` | Relevant actor/project/session time zone when available |
| `organizationId` | Server-owned customer scope for customer events |
| `workspaceId` | Workspace scope when applicable |
| `projectId` | Project scope when the event relates to a project |
| `documentId` | Logical document scope when applicable |
| `documentVersionId` | Exact version when the event concerns document bytes/state |
| `participantId` | Participant scope for participant actions when applicable |
| `actor` | Actor type, ID/reference, role, and identity level when permitted |
| `source` | `USER`, `API_CLIENT`, `SYSTEM`, `SCHEDULER`, `PROVIDER`, `ADMIN`, or `WEBHOOK` |
| `requestId` | Request/callback/job trace reference |
| `correlationId` | Cross-service/workflow correlation reference |
| `idempotencyKey` | Present for idempotent side-effecting requests when available |
| `aggregateVersion` | Resource version observed/created by the event |
| `policyVersion` | Policy used to make the decision when applicable |
| `reasonCode` | Stable reason for rejection, suspension, cancellation, revocation, or review when applicable |
| `eventHash` | Integrity hash over canonical envelope/payload |

`previousEventHash` is required for chained project evidence when the chain is enabled. Events that do not belong to a project chain still retain a stable event hash and request/correlation references.

## 4. Actor model

| Actor type | Meaning | Example |
|---|---|---|
| `ACCOUNT` | Authenticated account acting through the product | Sender signs or cancels |
| `GUEST_PARTICIPANT` | Guest recipient acting through scoped access | Guest accepts consent |
| `API_CLIENT` | Organization integration identity | Partner creates project |
| `SYSTEM` | Synchronous application behavior | Finalizer creates certificate |
| `SCHEDULER` | Scheduled expiry/reminder/retention job | Project expires |
| `PROVIDER` | External identity/storage/payment/video/scanner provider | Scanner returns result |
| `ADMIN` | Authorized staff investigation or operation | Certificate revoked |
| `WEBHOOK` | External callback received or delivery action | Payment callback received |
| `ENP` | Authorized electronic notary acting in a notarial session | ENP applies seal |

An actor reference must not expose unnecessary personal or credential data. The event can reference restricted identity/session records rather than duplicating them in the event payload.

## 5. Event naming and categories

Event names use lowercase dot-separated facts. The event name describes what happened, not the current status.

### Account and organization events

- `account.created`
- `account.contact_verified`
- `account.identity_level_verified`
- `account.identity_level_expired`
- `organization.created`
- `organization.terms_accepted`
- `organization.member_invited`
- `organization.member_joined`
- `organization.member_suspended`
- `organization.member_revoked`
- `organization.policy_changed`
- `workspace.created`
- `workspace.member_added`
- `workspace.member_removed`

### Project lifecycle events

- `project.created`
- `project.document_uploaded`
- `project.document_prepared`
- `project.participants_added`
- `project.ready_to_send`
- `project.sent`
- `project.scheduled`
- `project.viewed`
- `project.partially_signed`
- `project.awaiting_approval`
- `project.awaiting_notarization`
- `project.notarization_started`
- `project.completed`
- `project.archived`
- `project.expired`
- `project.cancelled`
- `project.declined`
- `project.rejected`
- `project.suspended`
- `project.resumed`
- `project.revoked`

### Document and version events

- `document.uploaded`
- `document.validation_started`
- `document.validation_failed`
- `document.scan_pending`
- `document.scanned`
- `document.quarantined`
- `document.released_from_quarantine`
- `document.normalization_started`
- `document.normalized`
- `document.normalization_failed`
- `document.fingerprinted`
- `document.version_created`
- `document.version_locked`
- `document.version_replaced`
- `document.version_finalized`
- `document.version_archived`
- `document.downloaded`
- `document.printed`
- `document.shared`
- `document.share_revoked`

For the initial PDF/PDF-A flow, emit only the upload, validation, fingerprint, version-created, and applicable rejection/eligibility events synchronously. Scan, quarantine, normalization, and provider-delivery events are reserved for future extensions and must not be emitted as if those controls ran.

### Participant, field, consent, and signing events

- `participant.added`
- `participant.invited`
- `participant.accessed`
- `participant.viewed`
- `participant.authentication_completed`
- `participant.identity_check_completed`
- `participant.declined`
- `participant.expired`
- `field.assigned`
- `field.completed`
- `consent.presented`
- `consent.accepted`
- `consent.declined`
- `consent.withdrawn`
- `consent.superseded`
- `signature.intent_confirmed`
- `signature.applied`
- `signature.validated`
- `signature.validation_failed`
- `approval.applied`
- `approval.declined`
- `witness.action_completed`

### Certificate and verification events

- `certificate.completion_issued`
- `certificate.authenticity_issued`
- `certificate.status_changed`
- `certificate.suspended`
- `certificate.revoked`
- `certificate.superseded`
- `certificate.investigation_started`
- `verification.requested`
- `verification.performed`
- `verification.match_found`
- `verification.modified_copy_detected`
- `verification.version_mismatch_detected`
- `verification.not_found`
- `verification.failed`
- `verification.access_restricted`

### Notarization and notarial-record events

- `notarization.requested`
- `notarization.eligibility_checked`
- `notarization.eligibility_failed`
- `notarization.enp_assigned`
- `notarization.enf_assigned`
- `notarization.session_scheduled`
- `notarization.session_started`
- `notarization.identity_checked`
- `notarization.location_checked`
- `notarization.liveness_checked`
- `notarization.act_performed`
- `notarization.certificate_issued`
- `notarization.book_entry_created`
- `notarization.synchronization_pending`
- `notarization.synchronized`
- `notarization.synchronization_failed`
- `notarization.failed`
- `notarization.cancelled`
- `enp.commission_verified`
- `enp.commission_expired`
- `enp.suspended`
- `enp.revoked`
- `enf.status_changed`
- `notarial_book.entry_inspected`
- `notarial_book.certified_copy_requested`
- `notarial_book.correction_created`

### Vault, retention, and administration events

- `vault.folder_created`
- `vault.document_moved`
- `vault.document_archived`
- `vault.document_restored`
- `retention.policy_applied`
- `retention.deletion_requested`
- `retention.deletion_blocked_by_hold`
- `retention.deletion_completed`
- `legal_hold.created`
- `legal_hold.released`
- `admin.project_viewed`
- `admin.evidence_exported`
- `admin.action_performed`
- `admin.access_denied`
- `security.suspicious_activity_detected`
- `security.access_link_revoked`

### API, webhook, and credit events

- `api_client.created`
- `api_client.secret_rotated`
- `api_client.suspended`
- `api_client.revoked`
- `api_request.received`
- `api_request.replayed`
- `api_request.idempotency_conflict`
- `webhook.endpoint_created`
- `webhook.delivery_attempted`
- `webhook.delivery_succeeded`
- `webhook.delivery_failed`
- `webhook.delivery_retried`
- `webhook.delivery_redelivered`
- `credit.purchase_confirmed`
- `credit.reservation_created`
- `credit.reservation_consumed`
- `credit.reservation_released`
- `credit.refunded`
- `credit.expired`
- `credit.reconciled`

## 6. Event payload rules

Event-specific payloads should contain the minimum fields needed to explain and verify the action. Examples:

### `signature.applied`

- Signature method.
- Field ID and placement reference.
- Participant role.
- Consent references.
- Identity/authentication level.
- Session reference.
- Validation status.
- Signature artifact reference/checksum where applicable.

### `project.state_changed`

- Previous state.
- New state.
- Transition command.
- Reason/guard result.
- DocumentVersion reference.
- Actor/source.
- Aggregate version.

### `document.version_replaced`

- Previous DocumentVersion.
- New DocumentVersion.
- Original/working/final role.
- Replacement reason.
- Previous/new fingerprints.
- Actor and policy version.

### `verification.performed`

- Verification method: upload, QR, or document code.
- What was checked.
- Result code.
- Registered DocumentVersion/certificate references when permitted.
- Access-policy result.

### `notarization.book_entry_created`

- ENF and ENP references.
- Notarial act and IEN/REN mode.
- Notarial Book ID/entry reference.
- Principal/witness references.
- Final DocumentVersion/fingerprint.
- Synchronization status.

### `credit.reservation_consumed`

- Organization and CreditAccount.
- Quantity/type.
- Project/API request.
- Previous/new balance or ledger references.
- Charge reason and billable operation.

Sensitive identity, location, video, government-ID, payment, or document content should use restricted references and access-controlled records rather than raw payload duplication.

## 7. Ordering, chain, and integrity

- `occurredAt` describes the source action; `recordedAt` describes persistence time.
- The event store assigns a monotonic per-project sequence or equivalent aggregate version.
- `previousEventHash` and `eventHash` make chain breaks detectable for project evidence.
- Events from external providers retain provider event ID, provider timestamp, and receipt time.
- Clock differences do not permit a client to rewrite server sequence order.
- Out-of-order callbacks are stored with an explicit receipt/result state and cannot advance a stale project without current-state validation.
- Missing, duplicate, or mismatched chain links trigger an integrity event and operator alert.
- Event exports include sequence, hashes, schema versions, and the algorithm used.

## 8. Idempotency and duplicate delivery

- Business operations use an idempotency key and request fingerprint where a retry can create a side effect.
- The original business event keeps its `eventId` across API/webhook retries.
- Duplicate provider events are linked to the original event and do not create duplicate signatures, certificates, charges, transitions, or ENB entries.
- Reusing an idempotency key with a different request fingerprint produces `api_request.idempotency_conflict` and does not mutate the resource.
- Webhook delivery attempts are separate delivery records, not new business facts.
- Manual redelivery references the original event ID and attempt history.

## 9. Immutability, correction, and access

- Normal product roles cannot update or delete a persisted EvidenceEvent.
- Retention/deletion policy may restrict visibility or storage lifecycle, but must not silently rewrite an event that is part of a signed/notarized evidence package.
- Corrections use a new event with a correction/reversal reference.
- Administrative access to restricted events creates `admin.project_viewed`, `admin.evidence_exported`, or `admin.action_performed` as appropriate.
- Event payloads are redacted or reference-based according to organization, participant, privacy, and notarial access policy.
- Public verification returns only permitted summaries, never the unrestricted internal event stream.

## 10. Event-to-state rule

Events record facts; state transitions record the current aggregate state. A state transition should create a corresponding event, but an event alone must not be treated as permission to mutate state.

Example:

```text
signature.applied
  -> signing service validates all guards
  -> project aggregate version advances
  -> project.partially_signed or project.completed is recorded
  -> any future webhook delivery may be derived from the committed event
```

If finalization fails after a signature event, the signature fact remains while the project stays in the correct non-completed state and a failure event explains the blocked finalization.

## 11. Export contract

An evidence export should include:

- Export ID, creation time, requester, and policy version.
- Project/document/document-version identity and fingerprints.
- Ordered events and event schema versions.
- Actor references and permitted identity evidence summaries.
- Consent references and displayed policy versions.
- Signature, approval, witness, certificate, verification, notarization, retention, and credit references where applicable.
- Hash algorithm, event hashes, chain status, and integrity-check result.
- Redaction/access-policy summary.
- Manifest fingerprint.

The export must state whether it is complete, partially redacted, unavailable, or integrity-check failed.

## 12. Operational requirements

- Monitor event write failures separately from ordinary application logs.
- Alert on chain mismatch, sequence gap, duplicate business event, and event-store unavailability.
- Provide a safe retry path for transient persistence/provider failures.
- Preserve request/correlation IDs through the API; future workers, providers, and webhook deliveries must carry them when those extensions are enabled.
- Include evidence health in project/admin dashboards.
- Test backup restore with event hashes and sequence integrity.

## 13. Contract acceptance

This contract is complete for the current system-build scope when:

- The event envelope and required fields are defined.
- Actor/source types and sensitive-data handling are defined.
- Event taxonomy covers account, organization, project, document, participant, consent, signature, certificate, verification, notarization, Vault, administration, API, webhook, and credit actions.
- Ordering, chain, hashing, duplicate, idempotency, and out-of-order callback rules are defined.
- Append-only, correction, access, export, and operational requirements are defined.
- Events are clearly separated from mutable resource status.
