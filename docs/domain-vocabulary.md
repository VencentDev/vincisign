# VinciSign Core Domain Vocabulary and Ownership

**Vocabulary ID:** `DOMAIN-VOCABULARY-0.1`  
**Status:** Approved for system construction  
**Confirmed:** 2026-11-14  
**Applies to:** Product language, API contracts, database schema, events, tickets, and first-party clients

## 1. Purpose

This is the authoritative vocabulary for VinciSign. Product copy, API schemas, database entities, event names, UI labels, support documentation, and tickets should use these terms consistently.

The vocabulary also defines ownership boundaries so a resource cannot be accidentally treated as user-owned, organization-owned, mutable after signing, or interchangeable with another record.

## 2. Domain hierarchy

```text
Account
  └── Organization membership
        └── Organization
              └── Workspace
                    └── Project
                          ├── Document
                          │     └── DocumentVersion
                          ├── Participant
                          │     └── Field assignment / action
                          ├── ConsentRecord
                          ├── EvidenceEvent
                          ├── Certificate
                          ├── Verification
                          └── Retention / access policy

Organization
  ├── APIClient
  ├── CreditAccount
  │     └── CreditLedgerEntry
  ├── WebhookEndpoint
  └── policy / branding / membership
```

An account may belong to multiple organizations. An organization owns the customer’s projects, workspaces, documents, policies, API clients, webhook endpoints, storage usage, and credit ledger. A project is the controlled transaction boundary for a document workflow.

## 3. Core terms

### Account

A person’s VinciSign account and profile. An account can create or join organizations, act as a sender, signer, witness, approver, viewer, notary, administrator, auditor, or API operator according to membership and identity evidence.

An account is not the same as an organization, participant, or API client. A guest participant may act in a project without having an account, subject to project policy.

### Organization

The customer/business boundary that owns workspaces, projects, members, policies, branding, API clients, webhooks, storage usage, and credits. Organization ownership is the primary authorization boundary for customer data.

The initial organization jurisdiction profile is `PH`. Future jurisdiction profiles must be explicit and versioned.

### Workspace

A controlled area inside an organization for grouping projects, members, policies, and access. A workspace is not a separate customer account and does not override organization-level security, retention, or billing rules.

### Membership

The relationship between an account and an organization or workspace. Membership carries role, scope, invitation state, effective dates, and organization authorization. Membership is not a project participant record.

### Role

An allowed action category assigned through membership or project participation. Organization roles include platform owner, compliance administrator, organization owner, organization administrator, organization member, auditor, and API operator. Project roles include sender, signer, witness, approver, viewer, notary, and guest recipient.

Role names are not identity proof. A signer role says what action is assigned; it does not prove who the person is without identity evidence.

### Project

A complete document transaction and the primary workflow boundary. A project contains the document, versions, participants, fields, settings, workflow state, consent, evidence, certificates, verification identities, retention policy, and final artifacts.

A project is organization-owned. Its lifecycle is controlled by server-side state transitions. A project may be for ordinary signing, approval, protection, verification, electronic notarization, or a combination permitted by policy.

### Document

The logical document represented in a project. A Document is the stable identity that groups its accepted working versions, signed versions, notarized versions, and replacement history. The initial flow accepts a PDF/PDF-A upload as the working version; normalized versions are a deferred extension.

The Document is not the file bytes. File bytes belong to a DocumentVersion stored through the document-storage boundary.

### DocumentVersion

An immutable or transition-controlled representation of a document’s bytes and metadata at a point in time. It contains original/working/final role, media type, storage reference, fingerprint, conversion/scanning results, creator, timestamp, and lock state.

Every meaningful replacement creates a new DocumentVersion. A signed or notarized version cannot be edited or silently replaced.

### Participant

A person or recipient assignment connected to a project. A participant has a project role, required action, contact details, identity requirements, signing order, access policy, and participant status.

A Participant may reference an Account, but a guest participant may not. Participant identity evidence is separate from contact data and must not be inferred from an email address alone.

### Field

A location or input requirement placed on a prepared DocumentVersion and assigned to a Participant or workflow role. Field types include signature, initials, date, name, text, checkbox, attachment, witness, approval, notary, required-reading acknowledgement, and verification marker.

A field belongs to a project/document version and cannot be moved or changed after the prepared version is locked without creating a new version and evidence history.

### ConsentRecord

An immutable record that a participant was shown and accepted, declined, withdrew, or superseded a specific consent/policy version. Consent types include electronic-record, electronic-signature, project disclosure, identity/security, privacy/processing, and notarial consent.

Consent is evidence of the recorded action and presented content. It is not itself a signature, identity verification, or legal conclusion.

### EvidenceEvent

An append-oriented record of a meaningful action or security state involving a project, document, participant, certificate, verification, credit, or administrative control. Examples include upload, conversion, viewing, identity verification, consent, signing, downloading, sharing, verification, notarization, certificate issuance, revocation, and policy change.

An EvidenceEvent records actor, action, previous/current state where relevant, timestamp, version, participant role, device/session context, reason, trigger source, request ID, and chain metadata. Evidence events are not ordinary application logs and must not be silently edited or deleted.

### Certificate

A signed or generated evidence document that summarizes a defined claim about a final document and its recorded evidence. Certificate types are separate:

- Certificate of Completion — ordinary project completion and signing evidence.
- Digital Certificate of Authenticity — document identity, fingerprint, issuer, issuance, and verification status.
- Notarial Certificate — authorized notarial act, ENP/ENF process, act details, commission data, and Electronic Notarial Book reference.

A certificate references the final DocumentVersion and evidence record. It is not the only source of truth and must have a visible status such as valid, suspended, revoked, superseded, or under investigation.

### Verification

A request and result for checking a document, code, QR identity, certificate, or status. Verification must state what it checked: uploaded-byte equality, registered platform-document identity, certificate status, code lookup, or availability/access policy.

Verification does not mutate the verified document and creates an EvidenceEvent.

### Passport

A human-readable evidence summary for a document/project. An active Passport shows current progress and events. A completed Passport shows the final document, completion certificate, verification identity, and completed participant history. A notarized Passport adds the notarial act, ENP/commission, IEN/REN, certificate, Electronic Notarial Book reference, session evidence, and status.

Passport is a view/package over source records, not a replacement for the DocumentVersion or EvidenceEvents.

### Vault

The storage, organization, search, retrieval, sharing, archive, retention, and deletion-policy area for project artifacts. Vault operations must not mutate signed or notarized document bytes or erase evidence.

### APIClient

An organization-owned integration identity with credentials, scopes, callback configuration, rate limits, status, and audit history. An APIClient is not an Account, Participant, credit, or payment token.

### WebhookEndpoint

An organization-owned callback destination for signed, versioned event deliveries. It has endpoint status, secret/key reference, event subscriptions, delivery history, retries, replay protection, and manual-redelivery controls.

### CreditAccount

An organization’s internal usage-credit balance and policy context. Credits are not cryptocurrency, API credentials, or transferable customer tokens.

### CreditLedgerEntry

An append-oriented credit movement recording purchase, promotion, reservation, consumption, release, refund, expiration, reversal, or adjustment. The balance must be reconcilable from ledger entries and references the organization, account, project, API request, or payment where applicable.

### PolicyVersion

An immutable version of a product, jurisdiction, document, consent, retention, access, billing, or notarization policy used to make a decision. A project and relevant evidence record store the policy version active when the decision occurred.

### IdentityEvidence

A record proving that a configured identity or authentication check produced a result at a time. It has level, provider/reference, status, expiry/recheck, subject, and evidence access policy. IdentityEvidence does not get replaced by changing an Account profile field.

### ENP

Electronic Notary Public. A qualified notary record with identity, commission, professional evidence, ENF affiliation, validity, suspension, and revocation status. An ENP record is not an ordinary user role until required evidence and activation checks pass.

### ENF

Electronic Notarization Facility. The facility/process boundary through which eligible electronic-notarization acts are performed, including security, identity, location, session, recordkeeping, retention, and synchronization controls.

### IEN

In-Person Electronic Notarization. A notarial mode where the required principals, witnesses, and ENP physically appear according to the applicable operating procedure.

### REN

Remote Electronic Notarization. A notarial mode using an approved remote session with identity, location, live sight-and-sound, MFA, liveness/anti-spoofing, and session controls as required by the operating procedure.

### Electronic Notarial Book (ENB)

A controlled official notarial record for eligible acts. It is separate from the ordinary application audit log and has its own entry, duplicate-copy, inspection, correction, retention, and authorized-access rules.

## 4. Ownership map

| Concept | Customer owner | System owner/module | Mutable after creation? | Primary authorization scope |
|---|---|---|---|---|
| Account | Individual | Account/User | Profile fields may change; identity evidence is historical | Account/session |
| Organization | Customer organization | Organization/Membership | Settings may change; ownership history is retained | Organization |
| Workspace | Organization | Workspace/Membership | Policy/grouping may change | Organization + workspace |
| Membership | Organization | Membership/Access | Can be invited, accepted, suspended, revoked | Organization/workspace |
| Project | Organization | Project/Workflow | State transitions only; terminal states are controlled | Organization/workspace |
| Document | Organization/project | Document | Logical identity persists; history retained | Organization/project |
| DocumentVersion | Organization/project | Document/Storage | Draft/working versions may transition; signed/notarized versions lock | Organization/project |
| Participant | Organization/project | Participant/Signing | Status transitions; historical actions retained | Organization/project/participant |
| Field | Organization/project/version | Preparation/Signing | Mutable until prepared version lock | Organization/project |
| ConsentRecord | Organization/project/participant | Consent/Policy | Historical record immutable; current state can transition | Organization/project/participant |
| EvidenceEvent | Organization/project | Evidence/Audit | Append-only; no normal user edits | Organization/project/admin policy |
| Certificate | Organization/project/version | Certificate | Status can transition; issued content immutable | Organization/project/public verification policy |
| Verification | Organization/project or public request | Verification | Result immutable; request status can finish/fail | Requester + verification policy |
| Passport | Organization/project | Passport/read model | Regenerated view; source records immutable | Organization/project/public policy |
| Vault record | Organization/project | Vault/Storage | Folder/share/archive state changes; artifact bytes do not | Organization/workspace |
| APIClient | Organization | Integration/API | Credentials rotate; audit/history retained | Organization |
| WebhookEndpoint | Organization | Integration/Webhooks | Config/status changes; deliveries retained | Organization |
| CreditAccount | Organization | Billing/Credits | Balance derived from entries | Organization |
| CreditLedgerEntry | Organization | Billing/Credits | Append-only; reversal references original | Organization/finance |
| PolicyVersion | VinciSign/operator | Policy/Compliance | Immutable; superseded by new version | Applicable jurisdiction/workflow |
| IdentityEvidence | Account/participant/ENP | Identity/Verification | Status can expire/revoke; historical result retained | Restricted identity policy |
| ENP | VinciSign/ENF operator | Notary Operations | Commission status changes; historical acts retained | Notary operations |
| ENF | VinciSign/partner operator | Notary Operations | Accreditation/status changes; history retained | Notary operations |
| ENB entry | ENF/notarial process | Notary Records | Append/correction workflow; no silent edits | ENF/notary operations |

## 5. Ownership and authorization rules

1. The organization is the default customer data owner for projects, documents, evidence, certificates, storage, API clients, webhooks, and credits.
2. Workspace membership narrows access; it does not grant access outside the organization.
3. Project roles grant assigned actions, not unrestricted project ownership.
4. A participant may access assigned fields and permitted artifacts only.
5. API clients inherit organization scope and may only use explicitly granted scopes.
6. Public verification exposes only the information allowed by the project/certificate verification policy.
7. Administrators can investigate with permission and a reason, but cannot erase or silently rewrite evidence.
8. ENP/ENF records are restricted operational records; an active user role alone does not authorize a notarial act.
9. Credit balances are organization-scoped and must be reconciled from CreditLedgerEntries.
10. A resource query must include its ownership/scope predicate; clients never supply trusted owner IDs in request bodies.

The canonical transition rules and guards are defined in [`project-lifecycle-policy.md`](project-lifecycle-policy.md).

## 6. Lifecycle vocabulary

### Project states

```text
DRAFT
DOCUMENT_UPLOADED
DOCUMENT_PREPARED
PARTICIPANTS_ADDED
READY_TO_SEND
SENT
VIEWED
PARTIALLY_SIGNED
AWAITING_APPROVAL
AWAITING_NOTARIZATION
NOTARIZATION_IN_PROGRESS
COMPLETED
ARCHIVED
```

Exceptional/terminal states:

```text
EXPIRED
CANCELLED
DECLINED
REJECTED
SUSPENDED
REVOKED
```

### Document version states

```text
RECEIVED -> VALIDATING -> SCAN_PENDING -> NORMALIZING -> READY
                              ├-> QUARANTINED
                              ├-> REJECTED
                              └-> FAILED
READY -> LOCKED_FOR_SEND -> SIGNED_FINAL -> NOTARIZED_FINAL -> ARCHIVED
```

Not every project uses every state. A signed or notarized final version cannot transition back to a draft/working state.

### Evidence event triggers

Use past-tense or completed-action event names for facts, such as `document.uploaded`, `document.scanned`, `participant.viewed`, `consent.accepted`, `signature.applied`, `certificate.issued`, `verification.performed`, `notarization.completed`, and `certificate.revoked`.

Use a separate status/state field for current conditions, such as `scanStatus`, `certificateStatus`, `notarizationStatus`, or `membershipStatus`. Do not use an event name as a mutable status.

## 7. Naming rules

- Use singular nouns for resource concepts: `project`, `document`, `participant`, `certificate`.
- Use `DocumentVersion` for the version record; do not call a file path, blob, or PDF itself a document version.
- Use `EvidenceEvent` for auditable facts; use `AuditLog` only for infrastructure/security log records that are not the project evidence model.
- Use `CertificateOfCompletion`, `CertificateOfAuthenticity`, and `NotarialCertificate` when type-specific behavior matters.
- Use `Verification` for a check/result, not for the document identity or verification URL itself.
- Use `APIClient` for machine identity and `Account` for a person identity.
- Use `CreditLedgerEntry` for money/usage movement; never call usage credits tokens in customer-facing or code terminology.
- Use `ENP`, `ENF`, `IEN`, `REN`, and `ENB` only when the record or workflow is genuinely within the electronic-notarization domain.
- Use `organizationId` and `workspaceId` as server-authorized scope values; do not accept arbitrary trusted owner IDs from request bodies.

## 8. API resource examples

The initial public API should use these resource boundaries:

```text
/api/v1/organizations
/api/v1/organizations/{organizationId}/workspaces
/api/v1/projects
/api/v1/projects/{projectId}
/api/v1/projects/{projectId}/documents
/api/v1/projects/{projectId}/participants
/api/v1/projects/{projectId}/fields
/api/v1/projects/{projectId}/consents
/api/v1/projects/{projectId}/events
/api/v1/projects/{projectId}/certificates
/api/v1/projects/{projectId}/verifications
/api/v1/api-clients
/api/v1/webhook-endpoints
/api/v1/credit-ledger
```

Notarization resources should remain explicit and separate from ordinary signing resources:

```text
/api/v1/projects/{projectId}/notarization-requests
/api/v1/notaries/enps
/api/v1/notaries/enfs
/api/v1/notarial-books
```

Exact endpoint names may evolve with OpenAPI review, but the ownership boundaries must not be collapsed.

## 9. Anti-confusion rules

| Do not use | Use instead | Reason |
|---|---|---|
| User as the owner of every record | Account, Membership, Organization | A person can belong to multiple organizations |
| File as the signed artifact | DocumentVersion | Bytes and logical document history are separate |
| Audit log as the complete evidence record | EvidenceEvent plus infrastructure logs | Project evidence has a defined schema and retention |
| Signature as proof of identity | Signature + ConsentRecord + IdentityEvidence | These records prove different things |
| Certificate as the source of truth | Certificate referencing DocumentVersion and EvidenceEvents | Certificates summarize evidence |
| Token for API credential or credits | APIClient credential / CreditLedgerEntry | Credentials and usage balances are different |
| Notary flag on an ordinary project | NotarizationRequest and notarial records | Notarization is a separate controlled workflow |
| Status event as current state | Event + status field | Facts are append-only; state is derived/current |
| Delete to correct history | New version/event/reversal | Historical evidence must remain inspectable |

## 10. Vocabulary acceptance

This vocabulary is complete for the current system-build scope when:

- Core business concepts have one authoritative definition.
- Ownership, mutability, and authorization scope are defined.
- Project and document-version lifecycle terms are defined.
- Ordinary signing, verification, certificates, credits, API clients, and notarization records are separated.
- Evidence events and mutable statuses are distinguished.
- API naming examples follow the ownership model.
- Anti-confusion rules prevent common template/domain mistakes.
