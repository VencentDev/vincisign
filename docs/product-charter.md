# VinciSign Product Charter

**Status:** Approved for system construction — formal legal/compliance review deferred  
**Version:** 0.1  
**Prepared:** 2026-11-14  
**Product:** VinciSign  
**Primary delivery window:** 2026-11-16 through 2028-11-10

## 1. Purpose

VinciSign provides document signing, evidence preservation, verification, and document lifecycle management as an API-first platform. A partner product should be able to create a document project, collect trusted participant actions, receive reliable status updates, retrieve the completed artifact, and explain how that artifact was produced without building signing infrastructure itself.

The first-party web client demonstrates the platform and supports operations. It is not the product boundary. The versioned API, organization-scoped access model, evidence record, and signed webhooks are the product boundary.

For the current system-build slice, the boundary is smaller: the API, PostgreSQL, Supabase Storage, synchronous PDF/PDF-A intake, SHA-256 fingerprints, and evidence events. Webhooks and other provider-backed capabilities remain future extensions.

## 2. Problem

Organizations currently assemble document workflows from email, file storage, custom forms, identity checks, signature tools, audit logs, and manual verification. This creates fragmented evidence, weak retry behavior, unclear document versions, poor integration ergonomics, and uncertainty about what a signature or certificate actually proves.

VinciSign addresses the platform problem by treating a completed document as an evidence package:

```text
Document
  + document version
  + identity evidence
  + consent
  + signature evidence
  + event history
  + certificate
  + verification method
  + retention record
```

## 3. Vision

Make trustworthy document signing and verification embeddable in any product through a clear, secure, and auditable API.

## 4. Product promise

For every completed signing project, VinciSign preserves the document, its versions, the participants’ actions, the consent record, and the evidence needed to understand how the final artifact was produced and later verified.

The promise has five parts:

1. **Sign** — create, prepare, send, sign, approve, and complete documents.
2. **Protect** — preserve immutable versions, cryptographic fingerprints, consent, and evidence events.
3. **Verify** — verify uploaded files, QR codes, document codes, certificates, and revocation state.
4. **Passport** — present the lifecycle, participants, events, certificates, and verification state in a human-readable record.
5. **Vault** — store, organize, retrieve, archive, share, and retain documents under policy.

## 5. Target users and jobs

### Platform and product teams

**Job:** Embed signing into onboarding, lending, HR, procurement, sales, insurance, or workflow software without creating a complete signing and evidence system.

**Need:** Stable API contracts, organization-scoped API clients, idempotent writes, signed webhooks, sandbox support, usage visibility, and predictable failure/retry behavior.

### Businesses and operations teams

**Job:** Send agreements to customers, employees, vendors, or partners and know which actions are complete.

**Need:** Projects, participants, fields, reminders, expiration, access policies, certificates, search, storage, retention, and administrative investigation.

### Signers and recipients

**Job:** Review and sign a document with understandable consent, secure access, and a clear record of what happened.

**Need:** Accessible signing flows, guest or account-required access, identity challenges when required, visible intent confirmation, completion feedback, and permitted access to the final document.

### Auditors, reviewers, and verifiers

**Job:** Determine whether a document is the registered version, what participants did, when they acted, and whether its certificate or evidence is still valid.

**Need:** Upload verification, QR/document-code verification, Passport views, evidence export, certificate history, revocation status, and explanations instead of unexplained status badges.

### Qualified electronic notaries and accredited facilities

**Job:** Conduct a regulated electronic-notarization act only if VinciSign’s operating model, accreditation, and jurisdictional requirements allow it.

**Need:** Separate ENP/ENF onboarding, commission monitoring, IEN/REN controls, identity and location evidence, notarial certificates, Electronic Notarial Book records, and official synchronization.

## 6. Core user journeys

### 6.1 API customer creates a signing project

1. An approved organization creates an API client and receives scoped credentials.
2. The client submits an external project reference, document, participants, fields or preparation instructions, expiration, notification, and callback settings.
3. VinciSign validates the request, reserves no billable credit until policy allows, and creates an organization-owned Draft project.
4. The client uploads or references the document; VinciSign preserves the original, creates a fingerprint, creates a working version, and records evidence.
5. The sender prepares fields and participants, reviews the final configuration, and sends the project.
6. Participants securely access, consent, authenticate as required, review, and sign.
7. VinciSign finalizes an immutable document version, creates the completion certificate, updates the Passport, and sends signed webhook events.

### 6.2 Recipient signs securely

1. The recipient opens an expiring project link.
2. VinciSign shows sender/project information and requests electronic-record consent.
3. The recipient completes the required authentication challenge.
4. The recipient reviews the document, completes assigned fields, confirms intent, and applies a signature.
5. VinciSign records identity, document version, placement, method, time, consent, authentication, session, device, and validation evidence.
6. The recipient receives confirmation and the final artifact when project policy permits.

### 6.3 Third party verifies a document

1. A verifier uploads a copy, scans a QR code, or enters a document code.
2. VinciSign identifies the matching registered document or code, checks version and certificate status, and applies access policy.
3. VinciSign explains the result: authentic match, modified copy, newer version, not found, revoked, expired, restricted, or unavailable.
4. VinciSign records the verification event without exposing information the verifier is not allowed to see.

### 6.4 Qualified notarization, if enabled

Electronic notarization is not an extension of ordinary signing. The user starts a separate request, VinciSign validates document eligibility and participant/location/commission requirements, the authorized ENP performs the act through the approved ENF process, and VinciSign preserves the notarial certificate, Electronic Notarial Book entry, session evidence, and notarized Passport. This journey is enabled in the system scope and remains server-gated by valid ENP/ENF credentials and required operating controls.

## 7. Scope

### In scope for the product program

- Accounts, organizations, workspaces, memberships, roles, API clients, and permissions.
- Document projects, immutable document versions, PDF/PDF-A upload validation, Supabase Storage, fingerprints, and evidence.
- Participant, field, signing-order, approval, witness, and notary-ready models.
- Electronic-record consent, signing sessions, signature evidence, completion artifacts, and completion certificates.
- Chained evidence, timestamps, verification, certificate status, QR/document codes, Passport, Vault, retention, legal holds, and evidence export.
- API versioning, idempotency, signed webhooks, retries, replay protection, rate limits, usage reporting, credits, reservations, refunds, and ledger reconciliation.
- Enterprise hierarchy, SSO boundary, custom branding, dedicated policies, audit reporting, disaster recovery, and service operations.
- Electronic-notarization readiness, IEN/REN workflows, and partner integration as first-class capabilities, subject to ENP/ENF control checks and production operating requirements.

The following are not part of the current system-build slice: Redis, background workers, DOCX/PDF conversion, malware scanning, notification providers, webhooks, and external synchronization.

### Out of scope for the first release

- Universal legal-enforceability claims.
- Calling ordinary electronic signatures notarization.
- Blockchain anchoring as a prerequisite for integrity.
- A cryptocurrency or transferable customer token.
- Supporting every jurisdiction, document type, identity provider, payment method, or notarial act.
- Building every enterprise policy before the core project/signing/verification flow works.
- Mobile-client work in the current delivery program.

## 8. Anti-goals and failure definitions

VinciSign fails this charter if it:

- Silently replaces a signed or notarized document version.
- Allows a user or API client to access another organization’s project, document, evidence, or billing data.
- Treats an email address or guest link as proof of complete legal identity.
- Returns successful side effects twice because of retries or duplicate callbacks.
- Labels an ordinary signed document as notarized.
- Generates a certificate that is the only source of truth or cannot be checked against the final document and evidence record.
- Lets administrators investigate or correct a record by erasing the original evidence.
- Claims a verification result without explaining whether it checks file equality, platform identity, certificate state, or access availability.
- Launches electronic notarization without the required legal, accreditation, ENP, ENF, identity, location, and recordkeeping controls.

## 9. Product principles

1. **API first:** First-party UI behavior must be explainable through public domain contracts and not rely on hidden UI-only state.
2. **Evidence by default:** User-visible and security-sensitive actions produce structured evidence events.
3. **Immutable history:** Corrections and replacements create new versions or events; they do not erase history.
4. **Explicit trust:** The product explains what a signature, certificate, code, or verification result proves and what it does not prove.
5. **Organization isolation:** Ownership, authorization, storage, billing, and evidence access are scoped to an organization/workspace.
6. **Safe retries:** Idempotency, duplicate-delivery tolerance, and recovery are part of every side-effecting integration.
7. **Regulated separation:** Notarization is modeled as a separate regulated capability, not a marketing label for standard signing.
8. **Operational truth:** Support and compliance staff can investigate failures without altering the evidence they are investigating.

## 10. Proposed success measures

These are proposed baselines for G0 review. They are targets to validate and revise, not current performance claims.

### Customer and product outcomes

- At least three design partners complete the foundation alpha workflow.
- At least 80% of pilot projects created through the dashboard or API reach a deliberate terminal outcome: completed, declined, cancelled, or expired.
- A new integration can create a sandbox project, receive a signed webhook, and retrieve its result from the public documentation without direct engineering assistance.
- At least 90% of pilot verifiers understand whether a result verifies the uploaded bytes, the registered platform document, or only certificate/code status in usability testing.

### Trust and correctness guardrails

- 100% of completed projects have a final document fingerprint, evidence timeline, consent record where required, and certificate or explicit reason no certificate was issued.
- 100% of tested cross-organization access attempts are denied and recorded appropriately.
- 100% of duplicate idempotent requests return one stable result without duplicate projects, charges, signatures, or webhook event IDs.
- 0 silent replacements of signed or notarized document versions.
- 0 unresolved critical security findings at a release gate.

### Reliability and operations

- Foundation alpha has a documented recovery path for storage, scanning, notification, and conversion failures.
- Integration release has reconciled API usage, credit ledger entries, reservations, releases, refunds, and payment callbacks.
- Production readiness has measured recovery point/recovery time targets, actionable alerts, on-call ownership, and incident drills.

### Compliance and regulated capability

- Every supported release has an approved jurisdiction/document/signature policy matrix.
- Retention, deletion, legal hold, certificate revocation, legal request, and incident procedures are documented before production release.
- Electronic-notarization capability is enabled in the system scope, while every production act is gated by valid ENP/ENF credentials and required operating controls.

## 11. First release boundary

The first product release should prove the non-regulated core:

1. Account and organization onboarding.
2. Organization-scoped Draft projects.
3. Ready PDF/PDF-A upload, validation, Supabase Storage persistence, and fingerprint.
4. Evidence events and controlled project lifecycle.
5. Participant invitations and signing sessions.
6. Required fields, consent, signatures, final artifacts, and Certificate of Completion.
7. Uploaded-file, QR/document-code, and certificate-status verification.
8. Active/completed Passport and core Vault storage.
9. Versioned API contracts and idempotent writes; webhooks and usage integrations are later extensions.

Electronic notarization, payments/credits, advanced enterprise controls, and higher-volume commitments require their own release gates. Electronic notarization is in scope for implementation from the beginning, but production operation must remain blocked until its required controls are configured.

## 12. Decisions required at G0

- Initial launch jurisdiction and counsel of record.
- Initial supported/prohibited document types and file limits.
- Initial signature methods and identity levels.
- Guest signing policy.
- Storage, document-processing, notification, identity, and payment provider choices.
- Retention/deletion defaults and legal-hold authority.
- Certificate validity, revocation, and public-verification policy.
- Whether VinciSign will be an ENF, integrate with an accredited ENF, or defer notarization.
- First design partners and pilot success criteria.

## 13. Charter approval

| Role | Name | Decision | Date |
|---|---|---|---|
| Product owner | VinciSign project owner | Approved for system construction | 2026-11-14 |
| Engineering owner | Engineering team | Approved to proceed | 2026-11-14 |
| Legal/compliance owner | Deferred from current system-build scope | Not a delivery blocker; constraints remain documented | 2026-11-14 |
| Operations/security owner | Engineering team | Follow roadmap gates during implementation | 2026-11-14 |

Approval of this charter authorizes the team to execute the system-build roadmap. Formal legal/compliance review is deferred from the current implementation scope. The documented jurisdiction, privacy, retention, notarization, and legal-enforceability boundaries remain engineering constraints and must not be represented as approved legal advice or production authorization.
