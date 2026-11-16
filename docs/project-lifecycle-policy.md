# VinciSign Project Lifecycle and Transition Policy

**Policy ID:** `PROJECT-LIFECYCLE-0.1`  
**Status:** Approved for system construction  
**Confirmed:** 2026-11-14  
**Applies to:** Ordinary signing, approvals, verification-linked projects, and electronic-notarization projects

## 1. Purpose

This policy defines the single controlled lifecycle for a VinciSign Project. It specifies which states exist, which transitions are allowed, which server-side guards must pass, what evidence each transition records, and how exceptional conditions are handled.

The project lifecycle is the workflow source of truth. Participant status, document-version status, certificate status, notarization status, and webhook delivery status are related state machines and must not be used to bypass a project transition.

## 2. Canonical states

### Active workflow states

| State | Meaning | Can accept participant action? |
|---|---|---:|
| `DRAFT` | Project exists but no accepted document version is ready | Sender/configuration only |
| `DOCUMENT_UPLOADED` | Original upload and required intake checks have produced an accepted document record | Sender/configuration only |
| `DOCUMENT_PREPARED` | Fields/settings are being prepared against a working version | Sender/configuration only |
| `PARTICIPANTS_ADDED` | Required participants and roles have been recorded | Sender/configuration only |
| `READY_TO_SEND` | Document, fields, participants, settings, and policy checks pass pre-send review | Sender send action |
| `SENT` | Project is sent and participant access links/invitations are active | Recipient may open |
| `VIEWED` | At least one participant has opened/reviewed the project | Recipient action |
| `PARTIALLY_SIGNED` | At least one required signature/approval is complete and more actions remain | Eligible participants |
| `AWAITING_APPROVAL` | Signature requirements are satisfied and an approval action remains | Eligible approver |
| `AWAITING_NOTARIZATION` | Ordinary project requirements are satisfied and a notarial act remains | Eligible notary/session |
| `NOTARIZATION_IN_PROGRESS` | An authorized IEN/REN session has started and is not complete | Authorized session only |
| `COMPLETED` | All required actions and enabled finalization conditions passed; final artifact/certificate are created or explicitly accounted for | No editing/signing |
| `ARCHIVED` | Completed project is placed in Vault archive under policy | No workflow action |

### Exceptional states

| State | Meaning | Recovery |
|---|---|---|
| `EXPIRED` | Project or participant deadline passed before completion | New project/version or authorized resend policy; no late signing |
| `CANCELLED` | Sender or authorized administrator stopped the project | No participant action; create new project/version if needed |
| `DECLINED` | Required participant declined the action | No completion; sender may create a new project/version |
| `REJECTED` | Validation, policy, notarization, or finalization rule rejected the project | Correct through an allowed new version/review path |
| `SUSPENDED` | Security, compliance, provider, or operational hold blocks action | Authorized resume to recorded prior state or resolve to terminal state |
| `REVOKED` | Completed/certified result is no longer valid for permitted use | Remains visible with reason/history; no return to signing |

`REVOKED` is a project-level visibility state for a result that can no longer be relied upon. The authoritative certificate state remains `CertificateStatus`, and revocation must also create certificate/evidence records.

## 3. State graph

```text
DRAFT
  -> DOCUMENT_UPLOADED
  -> CANCELLED

DOCUMENT_UPLOADED
  -> DOCUMENT_PREPARED
  -> REJECTED
  -> CANCELLED

DOCUMENT_PREPARED
  -> PARTICIPANTS_ADDED
  -> DOCUMENT_UPLOADED       (allowed only before send when a new working version is created)
  -> REJECTED
  -> CANCELLED

PARTICIPANTS_ADDED
  -> READY_TO_SEND
  -> DOCUMENT_PREPARED        (configuration correction before send)
  -> REJECTED
  -> CANCELLED

READY_TO_SEND
  -> SENT
  -> DOCUMENT_PREPARED        (sender changes draft settings before send)
  -> REJECTED
  -> CANCELLED

SENT
  -> VIEWED
  -> EXPIRED
  -> CANCELLED
  -> SUSPENDED

VIEWED
  -> PARTIALLY_SIGNED
  -> AWAITING_APPROVAL
  -> AWAITING_NOTARIZATION
  -> COMPLETED                 (only when no intermediate action remains)
  -> DECLINED
  -> EXPIRED
  -> CANCELLED
  -> SUSPENDED

PARTIALLY_SIGNED
  -> VIEWED                    (only for a new eligible participant view event; no state regression in the audit history)
  -> AWAITING_APPROVAL
  -> AWAITING_NOTARIZATION
  -> COMPLETED
  -> DECLINED
  -> EXPIRED
  -> CANCELLED
  -> SUSPENDED

AWAITING_APPROVAL
  -> PARTIALLY_SIGNED          (if an approval requirement is re-opened by policy before finalization)
  -> AWAITING_NOTARIZATION
  -> COMPLETED
  -> DECLINED
  -> EXPIRED
  -> CANCELLED
  -> SUSPENDED

AWAITING_NOTARIZATION
  -> NOTARIZATION_IN_PROGRESS
  -> COMPLETED                 (only when notarization is not required after policy reevaluation)
  -> REJECTED
  -> EXPIRED
  -> CANCELLED
  -> SUSPENDED

NOTARIZATION_IN_PROGRESS
  -> COMPLETED
  -> REJECTED
  -> EXPIRED
  -> CANCELLED                 (only when the active notarial process allows cancellation)
  -> SUSPENDED

COMPLETED
  -> ARCHIVED
  -> REVOKED

SUSPENDED
  -> recorded prior eligible state
  -> REJECTED
  -> CANCELLED

EXPIRED / CANCELLED / DECLINED / REJECTED / ARCHIVED / REVOKED
  -> no ordinary workflow transitions
```

The implementation may derive a display state such as `VIEWED` from the event timeline while retaining the full transition history. A state label must never erase or reorder past events.

## 4. Transition table

| From | To | Trigger | Required guards |
|---|---|---|---|
| `DRAFT` | `DOCUMENT_UPLOADED` | Accepted upload | Upload validation, scan/processing state, original preserved, fingerprint recorded |
| `DOCUMENT_UPLOADED` | `DOCUMENT_PREPARED` | Sender begins/prepares fields | Working version ready, no blocking scan/conversion state |
| `DOCUMENT_PREPARED` | `PARTICIPANTS_ADDED` | Sender saves participants | Fields valid, participant roles/ownership valid, required actions represented |
| `PARTICIPANTS_ADDED` | `READY_TO_SEND` | Sender submits review | Document finality, participant/order/settings/policy/credit checks pass |
| `READY_TO_SEND` | `SENT` | Sender sends immediately or scheduler runs | Final prepared version locked, links created, notifications queued, credits reserved if billable |
| `SENT` | `VIEWED` | Participant opens/reviews | Valid participant access, active link/session, project not expired/cancelled/suspended |
| `VIEWED` | `PARTIALLY_SIGNED` | Required action applied with actions remaining | Consent, identity, field, session, signature validation, version match |
| `VIEWED` | `AWAITING_APPROVAL` | All signature requirements complete; approval remains | Approval participant and required fields valid |
| `VIEWED` | `AWAITING_NOTARIZATION` | Signing/approval complete; notarial act remains | Notarization request, eligible document, ENP/ENF path, participant/witness data |
| `VIEWED` | `COMPLETED` | No remaining required action | Finalization conditions and artifact/certificate creation pass |
| `PARTIALLY_SIGNED` | `AWAITING_APPROVAL` | Remaining signatures complete | Approval requirements become the next action |
| `PARTIALLY_SIGNED` | `AWAITING_NOTARIZATION` | Remaining signatures/approvals complete | Notarial eligibility and locked-version preconditions pass |
| `PARTIALLY_SIGNED` | `COMPLETED` | All required actions complete | Final artifact, fingerprint, certificate, and evidence package complete |
| `AWAITING_APPROVAL` | `COMPLETED` | Approval applied | All finalization conditions pass |
| `AWAITING_APPROVAL` | `AWAITING_NOTARIZATION` | Approval applied; notarial act remains | Notarial eligibility and session prerequisites pass |
| `AWAITING_NOTARIZATION` | `NOTARIZATION_IN_PROGRESS` | IEN/REN session starts | Active ENP/ENF, final version locked, identity/location/session controls pass |
| `NOTARIZATION_IN_PROGRESS` | `COMPLETED` | Notarial act and finalization complete | Certificate, ENB, final notarized version, fingerprint, and sync state satisfy policy |
| Any pre-completion state | `EXPIRED` | Deadline/expiration job | Deadline passed; no completed finalization |
| Any cancellable state | `CANCELLED` | Sender/admin cancellation | Actor authorized; reason recorded; no prohibited active notarial action |
| Any actionable state | `DECLINED` | Required participant declines | Participant authorized; reason/status recorded |
| Any state with blocking failure | `REJECTED` | Policy/validation/finalization rejection | Failure code and remediation recorded |
| Any active state | `SUSPENDED` | Security/compliance/provider/ops hold | Authorized actor/system rule; suspension reason and prior state recorded |
| `COMPLETED` | `ARCHIVED` | Vault/archive action or retention workflow | Retention/access policy allows archive |
| `COMPLETED` | `REVOKED` | Authorized certificate/evidence revocation | Revocation authority, reason, certificate status, and evidence event |
| `SUSPENDED` | Prior eligible state | Authorized resume | Hold resolved, revalidation passes, prior state still valid |

## 5. Transition guards

Every transition is evaluated server-side against:

1. Actor identity and organization/workspace membership.
2. Required project role and permission.
3. Current project state and optimistic version.
4. DocumentVersion identity, fingerprint, lock state, and eligibility.
5. Participant status, role, order, and assigned fields.
6. Required ConsentRecords and policy versions.
7. IdentityEvidence/authentication level.
8. Notarization request, ENP/ENF state, IEN/REN mode, location, session, and witness controls where applicable.
9. Credit reservation/charge state where the operation is billable.
10. Retention, access, legal-hold, or administrative policy.
11. Idempotency key/request fingerprint for retried external requests.
12. Provider availability and required output artifact state.

The client may request a transition, but cannot set the new state directly. A failed guard returns a stable error code and leaves the project state unchanged unless the failure itself is an explicitly modeled transition such as `REJECTED` or `SUSPENDED`.

The event envelope and taxonomy are defined in [`evidence-event-contract.md`](evidence-event-contract.md).

## 6. Transition evidence contract

Every successful or blocked transition records:

- `transitionId`.
- Project ID and organization/workspace scope.
- Actor type and actor ID, or system/job/provider source.
- Previous state and requested/current state.
- Transition reason and stable transition/error code.
- Date/time and time zone.
- DocumentVersion ID and fingerprint when applicable.
- Participant ID and role when participant-driven.
- Consent and identity evidence references when applicable.
- Device/session/request/correlation identifiers allowed by policy.
- Whether the action was user-, system-, administrator-, provider-, or scheduler-triggered.
- Idempotency key/result where present.
- Previous/current project aggregate version for concurrency control.
- Related evidence, certificate, credit, webhook, or notarial record IDs.

Blocked attempts should create a security or workflow event when they are meaningful or suspicious, without exposing sensitive details in the client error.

## 7. State-specific rules

### Draft and preparation

- Drafts may be edited by authorized senders.
- Upload replacement creates a new DocumentVersion; it does not overwrite the original.
- Fields and participant assignments may change until the prepared version is locked.
- A sender cannot send until required document, field, participant, policy, and billing checks pass.

### Sent and recipient action

- Active-document access settings become fixed at send time according to organization policy.
- Participant links are scoped, expiring, and replay-resistant.
- Cancelled, expired, declined, suspended, or rejected projects cannot accept signatures.
- A resend creates a new invitation/access record, not a duplicate participant.

### Signing and approval

- A participant can act only on assigned fields and only when sequence/order permits.
- Required consent and identity checks must pass before signature intent and application.
- Duplicate signature requests return the original result or a stable replay response.
- A partial signature does not permit finalization until all required actions are complete.

### Notarization

- `AWAITING_NOTARIZATION` is not proof that a notarial act occurred.
- `NOTARIZATION_IN_PROGRESS` requires an active authorized ENP/ENF path and the selected IEN/REN controls.
- Notarized versions, notarial certificates, and ENB records are locked after completion.
- A notarial correction creates a new authorized act and preserves the original evidence.

### Completed, archived, and revoked

- Completed documents cannot return to draft or accept new signatures.
- Archived projects remain retrievable subject to retention and access policy.
- Revocation remains visible and records the authority, reason, effective time, impacted certificate/version, and notification state.
- Revocation does not delete the final document or evidence package.

## 8. Exceptional-state rules

### Expired

Expiration is scheduler- or policy-driven and records the deadline that passed. A late recipient action is rejected even if the link was not yet refreshed. Resending after expiry requires an explicit policy action and must not mutate the expired project’s history.

### Cancelled

Cancellation is authorized by the sender or permitted administrator. It stops new participant actions, invalidates active access links, releases unused reservations according to credit policy, and records the reason.

### Declined

A required participant’s decline prevents ordinary completion. The decline reason/status is visible only to permitted actors. A new project/version is required to change the requested document or participant intent.

### Rejected

Rejection is used for validation, policy, document, notarial, security, or finalization failure. The rejection code identifies the remediation path. The original failed version remains evidenced.

### Suspended

Suspension temporarily blocks action for security, compliance, provider, operational, or legal-hold reasons. The system stores the prior state and required resolution. Resume requires revalidation and an authorized action; suspension must not be used to erase a failure.

### Revoked

Revocation applies to a completed/certified result when the certificate or evidence can no longer be relied upon. The project, certificate, Passport, verification endpoint, and webhook state must agree on the visible revocation status.

## 9. Concurrency, retries, and jobs

- State transitions use optimistic concurrency or an equivalent aggregate-version check.
- Repeating the same transition request with the same idempotency key returns the original result.
- Reusing a key with a different payload is rejected.
- Scheduler jobs for expiry, reminders, archive, retention, and synchronization are idempotent.
- A provider callback cannot advance a project from a stale state without rechecking current state and version.
- Webhook delivery failure does not roll back the project transition; delivery status is tracked separately.
- Finalization is a single controlled operation that locks the final version, creates artifacts/evidence, and advances the project only when all required outputs succeed.

## 10. API behavior

Recommended transition endpoint shape:

```text
POST /api/v1/projects/{projectId}/transitions
```

Request fields should identify the requested action, idempotency key, client reason, and expected aggregate version. The server response returns project status, transition ID, related evidence IDs, and any pending asynchronous work.

The API must not expose a generic arbitrary `status` patch. Each action has a named command and its own authorization/guard set, such as:

```text
prepare
add-participants
mark-ready
send
cancel
decline
start-notarization
complete-notarization
archive
resume
revoke
```

Exact routes may evolve with OpenAPI review, but arbitrary client-controlled status mutation is prohibited.

## 11. Lifecycle acceptance

This policy is complete for the current system-build scope when:

- Canonical active and exceptional states are defined.
- Allowed transitions and server-side guards are explicit.
- Transition evidence fields are explicit.
- Completed, notarized, cancelled, expired, declined, rejected, suspended, archived, and revoked rules are explicit.
- Concurrency, idempotency, scheduler, provider callback, and webhook behavior is explicit.
- Ordinary signing and electronic-notarization transitions are separated.
- API clients cannot arbitrarily patch project status.
