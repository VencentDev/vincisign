# DocOnChain-Style Platform Replication Plan

## Purpose

This document describes a framework-neutral product and operational plan for building a DocOnChain-style document platform without blockchain. It covers document signing, document integrity, certificates, verification, document passports, storage, API integration, credits, and Philippine electronic notarization.

This plan assumes that “ENB” means the Philippine electronic-notarization model involving an **Electronic Notary Public (ENP)** and an **Electronic Notarization Facility (ENF)**.

The plan describes product flow and responsibilities rather than Spring Boot, Next.js, database, or infrastructure specifications.

## Important boundaries

The platform should be presented as:

- A document-management service.
- An electronic-signature and evidence service.
- A document-integrity and verification service.
- An electronic-notarization facility only if the required accreditation exists.

Do not claim that every electronically signed document is notarized or legally enforceable. DocOnChain’s own public terms state that some documents may still require traditional notarization and that enforceability is not guaranteed. See the [DocOnChain FAQ](https://doconchain.com/resources/faqs) and [terms of use](https://doconchain.com/terms-of-use).

Do not copy DocOnChain’s branding, source code, wording, or proprietary design. Replicate the functional model and create an independent product identity.

## Product model

The platform should be organized around five product areas:

1. **Sign** — create, send, sign, approve, and complete documents.
2. **Protect** — preserve document integrity and evidence.
3. **Verify** — verify uploaded documents, QR codes, and document codes.
4. **Passport** — show the document’s complete lifecycle and evidence history.
5. **Vault** — store, organize, share, archive, and retrieve documents.

DocOnChain publicly describes a similar lifecycle: DOC Sign, DOC Protect, DOC Verify, DOC Passport, and DOC Vault. See the [DocOnChain products page](https://doconchain.com/solutions/products).

## 1. Legal and business foundation

Before development, define:

- Supported countries and jurisdictions.
- Supported document types.
- Prohibited document types.
- Supported signature types.
- Supported notarization types.
- Whether guest signing is permitted.
- Whether account creation is mandatory for certain projects.
- Evidence and retention periods.
- Data-deletion rules.
- Legal-hold rules.
- Certificate validity and revocation rules.
- Whether the platform is an ENF, works with an accredited ENF, or only integrates with one.
- Whether notarization is performed by your business, an independent notary, or a partner.

Prepare:

- Terms of service.
- Privacy notice.
- Electronic-record consent.
- Electronic-signature consent.
- Notarization terms.
- Acceptable-use policy.
- Data-retention policy.
- Refund and credit policy.
- Enterprise agreement.
- Data-processing agreement.
- Notary agreement.
- Incident-response procedure.
- Certificate-revocation procedure.
- Legal-request and subpoena procedure.

## 2. Core business concepts

### Account

A person who can create projects, sign documents, manage an organization, or verify documents.

### Organization

A business or group that owns projects, members, policies, branding, billing, and credits.

### Workspace

A controlled area inside an organization for documents and projects.

### Project

A complete document transaction containing the document, participants, fields, settings, evidence, certificates, and final result.

### Document version

Every meaningful document change creates a new version. A signed or notarized version must never be silently replaced.

### Participant

A sender, signer, witness, approver, viewer, notary, administrator, or auditor connected with a project.

### Evidence event

A recorded action involving a document, participant, project, or security control.

Examples include upload, conversion, viewing, identity verification, signing, downloading, printing, sharing, verification, notarization, certificate issuance, and certificate revocation.

## 3. Project lifecycle

Use one controlled state model throughout the product:

```text
Draft
  -> Document uploaded
  -> Document prepared
  -> Participants added
  -> Ready to send
  -> Sent
  -> Viewed
  -> Partially signed
  -> Awaiting approval
  -> Awaiting notarization
  -> Notarization in progress
  -> Completed
  -> Archived
```

Alternative terminal or exceptional states:

```text
Expired
Cancelled
Declined
Rejected
Suspended
Revoked
```

Every state transition should record:

- Actor identity.
- Previous state.
- New state.
- Date and time.
- Document version.
- Participant role.
- Device and session context.
- Reason, when applicable.
- Whether the transition was user-triggered, system-triggered, or administrator-triggered.

Rules:

- Completed documents cannot return to draft.
- Notarized documents cannot be edited.
- Cancelled projects cannot accept new signatures.
- Revoked certificates must remain visibly revoked.
- Replaced documents must receive a new version and evidence history.

## 4. Account registration and onboarding

The registration flow should be:

1. User enters registration information.
2. User verifies email or phone.
3. User accepts electronic-record and platform terms.
4. User chooses individual or organization use.
5. User completes profile information.
6. User configures authentication.
7. User optionally completes stronger identity verification.
8. User creates or joins an organization.
9. User sees an onboarding checklist.
10. User receives an explanation of what the platform does and does not certify.

Identity levels may include:

- Unverified account.
- Contact-verified account.
- Government-ID-verified account.
- Organization-verified account.
- Notary-verified account.
- Enterprise-authorized account.

Guest signing may be permitted for ordinary projects, but an email address alone should not be represented as proof of a person’s complete legal identity.

## 5. Organization creation and administration

When an organization is created:

1. Capture legal name.
2. Capture registration information.
3. Capture address and contact details.
4. Identify the owner.
5. Configure branding.
6. Configure default project rules.
7. Configure billing ownership.
8. Invite members.
9. Assign roles.
10. Configure retention and access policies.
11. Configure API access.
12. Configure notification addresses.
13. Configure approval requirements.
14. Record acceptance of organization terms.

Roles should include at least:

- Platform owner.
- Compliance administrator.
- Organization owner.
- Organization administrator.
- Organization member.
- Sender.
- Signer.
- Witness.
- Approver.
- Viewer.
- Notary.
- Notary supervisor.
- Auditor.
- API client.
- Guest recipient.

Organization dashboard areas should include active projects, completed projects, expiring projects, failed projects, storage, credits, API activity, members, notary activity, security events, and compliance alerts.

## 6. Project creation flow

### 6.1 Start the project

The sender selects whether the project is for:

- Signing.
- Approval.
- Document protection.
- Verification.
- Notarization.
- A combination of these.

### 6.2 Upload the document

The platform should:

- Validate file type and size.
- Detect corrupted files.
- Preserve the original upload.
- Create a normalized working version.
- Record the original file identity.
- Create a document fingerprint.
- Record uploader and upload time.
- Scan for malicious content.
- Detect password-protected or unsupported files.
- Prevent silent replacement.

For Philippine electronic notarization, the document flow must support PDF or PDF/A and exclude notarial wills and depositions. See the [Supreme Court eNotarization FAQ](https://sc.judiciary.gov.ph/enotarization/faqs/).

### 6.3 Prepare the document

The sender may place:

- Signature fields.
- Initial fields.
- Date fields.
- Name fields.
- Text fields.
- Checkbox fields.
- Attachment fields.
- Witness fields.
- Approval fields.
- Notary fields.
- Required-reading acknowledgements.
- Optional QR or verification markers.

The sender defines:

- Required versus optional fields.
- Field ownership.
- Completion order.
- Conditional fields.
- Signing deadline.
- Reminder schedule.
- Download permissions.
- Print permissions.
- Sharing permissions.
- Delegation rules.

### 6.4 Add participants

Capture for each participant:

- Name.
- Email or contact address.
- Role.
- Organization.
- Required action.
- Signing order.
- Identity requirements.
- Authentication requirements.
- Whether an account is required.
- Whether a witness is required.
- Whether the person is physically present or remote for notarization.

### 6.5 Configure project settings

Settings should include:

- Project name.
- Sender message.
- Expiration.
- Reminder frequency.
- Language.
- Branding.
- Access restrictions.
- Completed-document access.
- Download and print rules.
- Audit settings.
- Verification settings.
- Notarization mode.
- Certificate settings.
- Retention settings.
- Optional password protection.

Active-document access settings should become fixed once the document is sent. Completed-document access settings may remain changeable according to policy.

### 6.6 Review before sending

Show:

- Document preview.
- Participants.
- Signing order.
- Required fields.
- Identity requirements.
- Notarization requirements.
- Expiration.
- Credits or fees.
- Privacy settings.
- Expected outputs.
- Legal notices.

Require the sender to confirm that the document is final, participants are correct, signing order is correct, the sender has authority, and the sender understands whether notarization is included.

### 6.7 Send the project

When sent:

1. Lock the prepared document version.
2. Create participant access links.
3. Generate one-time access credentials where appropriate.
4. Record the send event.
5. Notify participants.
6. Reserve required credits.
7. Start expiration timers.
8. Enable the audit trail.
9. Prevent unapproved document changes.

Support both immediate sending and scheduled sending.

## 7. Recipient and guest-signing flow

The recipient flow is:

1. Receive the invitation.
2. Open the secure link.
3. Review sender and project information.
4. Accept electronic-record consent.
5. Complete required authentication.
6. Review the document.
7. Complete required fields.
8. Configure or select a signature.
9. Confirm intent to sign.
10. Apply the signature.
11. Complete any verification challenge.
12. Receive confirmation.
13. Continue to the next required action.
14. Receive the final document when the project completes.

Distinguish these events:

- Viewed.
- Acknowledged.
- Signature intent confirmed.
- Signature applied.
- Signature validated.
- Declined.
- Abandoned.

Support both sequential signing and concurrent signing for recipients assigned to the same sequence.

## 8. Signature evidence

Supported signature types may include:

- Typed signature.
- Drawn signature.
- Uploaded signature image.
- Click-to-sign confirmation.
- Certificate-backed digital signature.
- Notary-applied digital signature.

For every signature record:

- Signer identity.
- Document version.
- Signature placement.
- Signature type.
- Time and time zone.
- Consent.
- Authentication level.
- Session identifier.
- Device information.
- Location evidence where lawful.
- Signature event.
- Validation result.

## 9. Document integrity without blockchain

Replace blockchain dependence with layered, tamper-evident evidence:

1. Immutable document versioning.
2. Cryptographic fingerprints for files and versions.
3. Chained event history.
4. Trusted timestamps.
5. Digitally signed completion records.
6. Append-only audit storage.
7. Independent backups.
8. Optional external anchoring.
9. Controlled administrator access.
10. Complete access and change logs.
11. Certificate revocation records.
12. Version and status history.
13. Evidence export.

Use claims such as “tamper-evident,” “verifiable,” “cryptographically fingerprinted,” and “auditable.” Avoid absolute claims such as “impossible to alter” or “hack-proof.”

## 10. Certificate of completion

When all required actions are complete, generate a Certificate of Completion containing:

- Certificate identifier.
- Project identifier.
- Document identifier.
- Final document version.
- Final document fingerprint.
- Completion status.
- Completion date and time.
- Sender identity.
- Participant identities.
- Signature events.
- Authentication methods.
- Witnesses, if any.
- Notary information, if applicable.
- Completion conditions.
- Verification URL or code.
- QR code, if enabled.
- Certificate history.
- Revocation status.
- Statement describing the certificate’s scope.
- Disclaimer distinguishing signing evidence from notarization.

The certificate should reference the final document and evidence record rather than becoming the only source of truth.

If evidence is later invalidated, mark the certificate as valid, suspended, revoked, superseded, or under investigation. Never silently regenerate it.

## 11. Digital certificate of authenticity

Treat a Digital Certificate of Authenticity as separate from the Certificate of Completion.

It may be embedded in or attached to the final document and should identify:

- Issuer.
- Document identity.
- Document fingerprint.
- Issuance time.
- Verification method.
- Certificate status.
- Revocation information.

Do not claim government or certificate-authority backing unless the appropriate authority has actually issued or approved it.

## 12. Document Passport

The Passport is the human-readable evidence summary.

It should show:

- Document identity.
- Current status.
- Original and final fingerprints.
- Participants and roles.
- Signing history.
- Viewing history.
- Download history.
- Printing history where detectable.
- Verification history.
- Notarization history.
- Certificate information.
- Security events.
- Access restrictions.
- Revocation status.

### Active passport

Shows current progress, pending participants, events to date, and the current document version. It does not show a final completion certificate.

### Completed passport

Shows the full signing history, final document, completion certificate, verification code, and completed participant history.

### Notarized passport

Shows the notarial act, ENP identity, commission information, IEN or REN mode, notarial certificate, Notarial Book reference, session evidence, final notarized document, and certificate status.

## 13. Vault and storage flow

The Vault should allow users to:

- View completed projects.
- Organize files into folders and subfolders.
- Search and filter documents.
- Download final files.
- Download certificates and passports.
- Export evidence.
- Share controlled links.
- Revoke shared links.
- Archive documents.
- Request deletion where allowed.

Storage states:

```text
Temporary upload
  -> Active project
  -> Completed document
  -> Archived document
  -> Legal hold
  -> Retention expired
  -> Pending deletion
  -> Deleted
```

A legal hold must override normal deletion rules.

## 14. Verification flow

### Upload verification

1. User uploads a document.
2. Platform calculates its fingerprint.
3. Platform searches for a matching final document.
4. Platform compares the uploaded file with the registered version.
5. Platform returns a result and explanation.
6. Platform displays the authorized original where permitted.
7. Platform records the verification event.

Possible results:

- Authentic match.
- Authentic document with modified copy.
- Authentic document with newer version.
- Document not found.
- Certificate revoked.
- Certificate expired.
- Access restricted.
- Verification unavailable.

### QR verification

1. User scans the QR code.
2. Platform resolves the verification identity.
3. Platform checks document and certificate status.
4. Platform displays the permitted verification page.
5. Platform records the verification event.

### Document-code verification

1. User enters the document code.
2. Platform locates the project or final version.
3. Platform checks whether the code is active.
4. Platform displays permitted information.
5. Platform explains whether the code verifies the uploaded copy or only links to the platform copy.

DocOnChain distinguishes uploaded-file verification from QR or document-code verification because a code may identify the platform’s original document without proving that the external copy is identical. See the [DocOnChain verification manual](https://doconchain.com/hubfs/DOCONCHAIN%20-%20User%20Manual%20%28v1.01%29.pdf?hsLang=en).

## 15. Electronic notarization

Philippine electronic notarization is a separate regulated product line. The Rules cover electronic documents in PDF or PDF/A format and recognize acknowledgment, affirmation or oath, jurat, and signature witnessing. Notarial wills and depositions are excluded. See the [Supreme Court Rules on Electronic Notarization](https://sc.judiciary.gov.ph/enotary-services/enotarization-rules/) and [FAQs](https://sc.judiciary.gov.ph/enotarization/faqs/).

### 15.1 ENF preparation

Before offering electronic notarization:

1. Determine accreditation requirements.
2. Prepare the facility description.
3. Document security controls.
4. Document identity verification.
5. Document MFA, liveness, and anti-spoofing controls.
6. Document location and VPN controls.
7. Document video-session controls.
8. Document electronic-signature and seal controls.
9. Document Electronic Notarial Book controls.
10. Document retention and backup.
11. Document privacy and incident response.
12. Submit required applications and supporting materials.
13. Complete accreditation.
14. Maintain accreditation and renewal evidence.

An ordinary software vendor is not automatically an accredited ENF provider.

### 15.2 ENP onboarding

For each ENP:

1. Verify identity.
2. Verify Philippine Bar membership and good standing.
3. Verify MCLE compliance.
4. Verify ULAS compliance.
5. Verify Philippine residency and regular place of business.
6. Verify professional tax information.
7. Verify commission information.
8. Verify ENF affiliation.
9. Verify electronic-signature capability.
10. Record commission start and expiration dates.
11. Record suspension or revocation status.
12. Provide ENF training.
13. Activate the ENP only after approval.
14. Automatically block activity after expiration, suspension, or revocation.

A traditional notarial commission does not automatically authorize electronic notarization. The ENP requires a separate electronic notarial commission. See the [Supreme Court ENP FAQ](https://sc.judiciary.gov.ph/enotarization/faqs/).

### 15.3 Customer notarization request

1. Customer starts a notarization request.
2. Customer uploads the document.
3. Customer selects the notarial act.
4. Customer identifies principals and witnesses.
5. Customer selects IEN, REN, or a combination.
6. Customer chooses or is assigned an ENP.
7. Customer schedules a session where required.
8. Customer reviews requirements and price.
9. Customer pays or uses approved credits.
10. Customer receives preparation instructions.

### 15.4 Pre-session validation

Before the session:

- Confirm accepted file format.
- Confirm that the document is not excluded.
- Confirm all principals and witnesses.
- Confirm identities.
- Confirm participant locations.
- Detect location manipulation.
- Confirm consent.
- Confirm active ENP commission.
- Confirm ENP authorization.
- Confirm final document version is locked.
- Confirm payment or credit availability.

The Supreme Court states that REN facilities must support location verification and prevent VPN or similar location manipulation. See the [Supreme Court eNotarization FAQ](https://sc.judiciary.gov.ph/enotarization/faqs/).

### 15.5 IEN flow

For In-Person Electronic Notarization:

1. Principal, witnesses, and ENP physically appear before one another.
2. ENP verifies identities.
3. ENP confirms capacity and willingness.
4. ENP reviews the document.
5. ENP confirms the notarial act.
6. Required signatures occur in the ENP’s presence.
7. ENP applies the electronic signature and seal.
8. Platform generates the notarial certificate.
9. Platform records the act in the Electronic Notarial Book.
10. Platform produces the finalized notarized document.
11. Platform generates the notarized Passport.
12. Platform performs required reporting or synchronization.
13. Participants receive the final records.

### 15.6 REN flow

For Remote Electronic Notarization:

1. Principal joins through the approved facility.
2. Witnesses join where required.
3. ENP verifies each identity.
4. Platform verifies each participant’s location.
5. Platform confirms live sight-and-sound communication.
6. Participants remain within camera view as required.
7. ENP confirms willingness and understanding.
8. ENP confirms the document and notarial act.
9. Signatures are applied according to the act’s requirements.
10. ENP performs the notarial act.
11. ENP applies the electronic signature and seal.
12. Platform generates the certificate.
13. Platform records the Electronic Notarial Book entry.
14. Platform stores session evidence where created or requested.
15. Platform produces the final notarized PDF.
16. Platform provides verification access.
17. Platform performs required reporting or synchronization.

For principals abroad, REN is limited to qualifying locations such as Philippine embassies, consular offices, or Philippine Honorary Consul offices, with the required confirmation. It is not unrestricted worldwide notarization.

### 15.7 Notarial certificate

Generate the certificate from verified ENP commission data. Include:

- ENP name exactly as stated in the commission.
- Roll of Attorneys number.
- “Electronic Notary Public.”
- “Philippines.”
- Commission number.
- Commission validity.
- Professional tax receipt information.
- IBP membership number.
- ENP email.
- Regular place of business.
- MCLE and ULAS compliance.
- IEN or REN mode.
- Document identity.
- Notarial act.
- Principal and witness information.
- Notarial Book reference.
- Final document fingerprint.
- Verification identifier.

See the [Supreme Court reference certificate formats](https://sc.judiciary.gov.ph/wp-content/uploads/2026/10/SC-x-LRA_Redacted.pdf).

### 15.8 Electronic Notarial Book

Maintain one active book per ENF and a duplicate copy. Entries should include:

- Type of notarial act.
- Date and time.
- Document title or description.
- Principal names and addresses.
- Witness names and addresses.
- Competent evidence of identity.
- Fee charged.
- Participant location confirmation.
- IEN or REN mode.
- Other significant circumstances.
- Reason an attempted act was not completed.
- Inspection or certified-copy requests.
- Requester identity and lawful purpose.
- Required participant signatures.

This is not merely an ordinary application audit log. It is a controlled official notarial record.

### 15.9 Post-notarization

1. Lock the notarized version.
2. Lock the notarial certificate.
3. Record the Notarial Book entry.
4. Generate the notarized Passport.
5. Generate verification identifiers.
6. Notify participants.
7. Apply retention rules.
8. Prevent editing of the notarized version.
9. Handle corrections only through an authorized new act.
10. Preserve the original evidence.
11. Track revocation, suspension, and correction.
12. Support authorized official access.

## 16. API integration

### Client onboarding

1. Enterprise requests integration access.
2. Platform verifies the organization.
3. Platform approves the use case.
4. Organization creates an integration profile.
5. Organization receives credentials.
6. Organization defines callback destinations.
7. Organization selects permitted operations.
8. Organization tests in a non-production environment.
9. Platform approves production access.

### Project creation through API

The client submits:

- External project identifier.
- Document reference.
- Participant information.
- Roles.
- Signature requirements.
- Approval requirements.
- Notarization requirements.
- Expiration.
- Notification preferences.
- Return URL.
- Callback URL.
- Organization context.

The platform returns:

- Platform project identifier.
- Current status.
- Participant action links.
- Expiration.
- Required credits.
- Verification identity.
- Tracking information.

### API status and webhooks

Support retrieval of:

- Project status.
- Participant status.
- Document status.
- Signature status.
- Notarization status.
- Certificate status.
- Verification status.
- Failure reason.
- Completion time.

Webhook events should include:

- Project created.
- Project sent.
- Recipient viewed.
- Recipient signed.
- Recipient declined.
- Project expired.
- Project completed.
- Certificate issued.
- Verification performed.
- Notarization scheduled.
- Notarization started.
- Notarization completed.
- Notarization failed.
- Certificate revoked.

Each event needs a stable identifier, timestamp, version, duplicate-delivery tolerance, retry behavior, signature verification, replay protection, delivery history, and manual redelivery.

## 17. Credits and API token model

Do not begin with a cryptocurrency token.

Separate:

1. Subscription plan.
2. Usage allowance.
3. Purchased credits.
4. Reserved credits.
5. Consumed credits.
6. Refunded credits.
7. Promotional credits.
8. Expired credits.
9. API rate limits.
10. Invoices and money.

The API authentication token is a credential. It is not a spendable customer token. The customer balance should be represented as internal, non-transferable usage credits.

### Credit lifecycle

When credits are purchased:

1. Payment is authorized.
2. Payment is confirmed.
3. Credit batch is created.
4. Balance is increased.
5. Ledger entry is created.
6. Receipt or invoice is issued.
7. Expiration rules are applied if applicable.
8. Payment reference is attached.

When an API request is billable:

1. Calculate expected usage.
2. Check available balance.
3. Reserve credits.
4. Create the project.
5. Consume credits only after successful delivery.
6. Release unused reservations.
7. Record the final charge.
8. Send the usage event.

If the platform fails before delivering the paid service, do not consume credits. Define partial-failure and refund rules explicitly.

### Credit ledger

Every entry should include:

- Ledger entry identifier.
- Organization.
- Account.
- Credit type.
- Quantity.
- Reason.
- Project.
- API request.
- Payment reference.
- Expiration date.
- Previous balance.
- New balance.
- Status.
- Timestamp.
- Reversal reference.

Balances should be reconcilable against the ledger and should never depend only on a mutable number.

### API idempotency

For every billable operation:

1. Client sends an idempotency key.
2. Platform stores the key and request fingerprint.
3. Repeated requests return the original result.
4. Reuse of the same key with a different payload is rejected.
5. Credit reservation occurs once.
6. Webhook events use unique identifiers.
7. Retries never create duplicate projects or charges.

## 18. Administration and compliance operations

Create internal workflows for:

- User review.
- Organization review.
- Notary approval.
- Commission expiration.
- Suspicious activity.
- Failed identity verification.
- Access complaints.
- Certificate revocation.
- Credit disputes.
- API failures.
- Webhook failures.
- Retention deadlines.
- Legal holds.
- Security incidents.
- Government information requests.

Support staff must be able to investigate a project without altering its evidence.

Administrative actions should be permission-controlled, reason-required, logged, reviewable, and reversible where appropriate.

## 19. Security and privacy

For every sensitive action:

- Authenticate the user.
- Authorize the resource.
- Validate the request.
- Check project state.
- Check document version.
- Check participant role.
- Check organization policy.
- Check credit status.
- Record the action.
- Return only permitted data.

Plan for:

- Account takeover.
- Stolen signing links.
- Replay attacks.
- Duplicate API requests.
- Malicious uploads.
- Unauthorized downloads.
- Administrator abuse.
- Forged certificates.
- Expired notary commissions.
- Compromised signing credentials.
- Deleted evidence.
- Insider access.
- Data-subject requests.
- Government requests.

The Supreme Court’s eNotarization materials emphasize MFA, electronic-notarial-book integrity, and data protection. See the [Supreme Court eNotarization announcement](https://sc.judiciary.gov.ph/sc-approves-rules-on-electronic-notarization-advancing-digital-innovation-and-access-to-justice/).

## 20. Testing and acceptance

### Signing scenarios

- One signer.
- Multiple signers.
- Sequential signing.
- Concurrent signing.
- Guest signing.
- Account-required signing.
- Declined signing.
- Expired request.
- Resent request.
- Duplicate request.
- Incorrect signer.
- Signing after cancellation.

### Integrity scenarios

- Modified document.
- Replaced document.
- Corrupted upload.
- Certificate mismatch.
- Invalid QR code.
- Revoked certificate.
- Conflicting versions.
- Missing event.
- Duplicate event.
- Out-of-order event.

### Notarization scenarios

- IEN.
- REN.
- Mixed participation.
- Missing witness.
- Invalid document type.
- Expired ENP commission.
- Suspended ENP.
- Failed identity verification.
- Failed location verification.
- Interrupted session.
- Pre-signed jurat document.
- Certificate generation.
- Notarial Book entry.
- Official synchronization failure.

### Credit and API scenarios

- Successful purchase.
- Failed payment.
- Duplicate payment callback.
- Credit reservation.
- Credit release.
- Partial failure.
- Refund.
- Expired credits.
- Concurrent API requests.
- Duplicate idempotency key.
- Negative balance prevention.

### Operational scenarios

- Storage outage.
- Email failure.
- Video-session failure.
- Webhook failure.
- Backup restoration.
- Administrator investigation.
- Legal hold.
- Certificate revocation.
- Account deletion request.

## 21. Recommended delivery phases

### Phase 1 — Foundation

- Product definitions.
- Legal review.
- Accounts.
- Organizations.
- Roles.
- Document upload.
- Storage.
- Basic audit events.
- Basic dashboard.

### Phase 2 — Digital signing

- Project creation.
- Participant invitations.
- Signature fields.
- Guest signing.
- Account-required signing.
- Signing order.
- Expiration.
- Notifications.
- Completion certificate.

### Phase 3 — Verification and Passport

- Document fingerprints.
- Upload verification.
- QR verification.
- Document codes.
- Certificate validation.
- Passport view.
- Revocation and status checking.

### Phase 4 — Vault and enterprise controls

- Folders.
- Search.
- Retention.
- Legal holds.
- Branding.
- Advanced permissions.
- Evidence export.
- Administrative review.

### Phase 5 — Credits and API

- Plans.
- Internal credits.
- Ledger.
- Reservations.
- Refunds.
- API clients.
- Idempotency.
- Webhooks.
- Usage reporting.

### Phase 6 — Electronic notarization

- ENP onboarding.
- ENF compliance preparation.
- IEN workflow.
- REN workflow.
- Identity and location controls.
- Notarial certificate.
- Electronic Notarial Book.
- Official reporting or synchronization.
- Notarized Passport.
- Commission monitoring.

### Phase 7 — Enterprise readiness

- Organization hierarchy.
- Sub-organizations.
- Single sign-on.
- Dedicated retention policies.
- Custom branding.
- Higher-volume APIs.
- Service-level commitments.
- Audit exports.
- Compliance reporting.
- Disaster-recovery exercises.

## Final principle

Treat each completed or notarized document as a complete evidence package:

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

The document alone is not the entire product. The surrounding evidence is what makes the system useful in audits, disputes, compliance reviews, integrations, and notarization workflows.

For Philippine eNotarization, keep the ENP, ENF, official rules, commission status, notarial certificate, Electronic Notarial Book, and ordinary platform audit trail as separate but connected concepts. The platform may facilitate and preserve the process, but the legally significant notarial act is performed by a commissioned ENP through an accredited ENF.
