# VinciSign Jurisdiction and Market Matrix

**Policy ID:** `PH-BASELINE-0.1`  
**Status:** Approved for system construction  
**Confirmed:** 2026-11-14  
**Initial market:** Philippines  
**Scope:** System behavior and product configuration baseline; not legal advice

Document intake and prohibited-content details are defined in [`document-policy.md`](document-policy.md).
Signature, consent, and identity requirements are defined in [`signature-consent-identity-policy.md`](signature-consent-identity-policy.md).

## 1. Baseline decision

VinciSign will be built Philippines-first with both ordinary electronic signing and electronic notarization as supported product capabilities.

| Capability | Initial baseline | System treatment |
|---|---|---|
| Ordinary electronic signing | Enabled | Available to approved organizations and invited participants |
| Electronic notarization | Enabled in product scope | Supported through ENP/ENF-controlled workflows; production actions require valid ENP/ENF credentials and control checks |
| IEN | Enabled in product scope | Requires in-person appearance, identity checks, participant/witness rules, locked final version, certificate, and Electronic Notarial Book entry |
| REN | Enabled in product scope | Requires approved remote session, identity, location, anti-VPN/location controls, live sight-and-sound, certificate, and Electronic Notarial Book entry |
| Signers outside the Philippines | Allowed for ordinary signing | No Philippine notarization claim is made solely from a signer’s location; notarization uses the applicable IEN/REN eligibility rules |
| Organization launch market | Philippines | Organization profile stores `PH` as its initial jurisdiction profile |
| Other organization markets | Not launched in this baseline | Model as future jurisdiction profiles; do not silently apply Philippine rules to other markets |

Electronic notarization is a first-class system capability, not a disabled placeholder. The implementation must prevent an act from completing unless the required ENP, ENF, document, identity, location, session, certificate, and Electronic Notarial Book conditions are satisfied.

## 2. Supported user and organization boundary

### Initial organization profile

- Organization registration is Philippines-first.
- Store legal name, registration information, address, contact details, owner, organization terms, and jurisdiction profile.
- The initial jurisdiction profile is `PH`.
- Organization settings may configure ordinary signing, notarization, retention, access, notifications, branding, credits, and API policy.
- API clients inherit organization jurisdiction and policy scope; a client cannot select another jurisdiction in an unapproved request.

### Participants and signers

- Ordinary signing may include participants outside the Philippines.
- Guest signing may be used for ordinary projects when the sender enables it.
- An email address or guest link is contact/access evidence, not proof of complete legal identity.
- Notarization participants follow the selected IEN/REN eligibility and location rules; the platform must store the rule result and evidence.
- A participant’s location is captured only when required by the project or notarization mode and is subject to the configured privacy policy.

## 3. Document and workflow matrix

| Workflow | Status | Supported baseline | Required controls |
|---|---|---|---|
| Ordinary document signing | Enabled | Agreements, approvals, acknowledgements, and other permitted electronic documents | Consent, authentication policy, participant binding, fields, evidence events, final fingerprint, completion certificate |
| Document protection | Enabled | Fingerprinting, immutable versions, chained events, trusted timestamps, evidence export | Original preservation, working-version separation, append-only evidence, version lock |
| Uploaded-file verification | Enabled | Compare an uploaded copy with the registered final version | Fingerprint comparison, explanation of result, verification event |
| QR verification | Enabled | Resolve a document/certificate verification identity | Public verification policy, certificate status, access restrictions, verification event |
| Document-code verification | Enabled | Locate the registered project/final version | Active-code state, scope explanation, verification event |
| In-person electronic notarization | Enabled | IEN workflow for eligible Philippine electronic-notarization acts | Qualified ENP, approved ENF, physical appearance, identity/capacity/willingness, locked version, certificate, ENB |
| Remote electronic notarization | Enabled | REN workflow for eligible participants and locations | Qualified ENP, approved ENF, MFA/identity, liveness where required, location and anti-VPN checks, live sight-and-sound, certificate, ENB |
| Traditional/off-platform notarization | Not performed by VinciSign | May be represented as an external record only if explicitly integrated later | Never label ordinary VinciSign signing as traditional or electronic notarization |

## 4. File and document policy baseline

### Accepted initial formats

- PDF and PDF/A are accepted directly.
- The accepted upload is stored as the immutable working version for the initial flow.
- DOCX normalization is deferred; non-PDF/PDF-A uploads are rejected until a later policy version enables conversion.
- The initial per-file size limit is configurable, with a 25 MB default for the first environment baseline.

### Rejected or quarantined inputs

- Corrupted files.
- Empty files.
- Password-protected or unsupported encrypted files.
- Malware scanning is deferred and is not represented as an initial upload status.
- Unsupported file types.
- Files exceeding the configured limit.
- Documents prohibited by the organization or jurisdiction policy.
- Documents that fail the requirements for the selected notarization mode.
- Notarial wills and depositions for the Philippine electronic-notarization workflow.

An upload failure must never leave a project marked `READY_TO_SEND`, `SENT`, or `COMPLETED`.

## 5. Signature and identity matrix

| Capability | Ordinary signing | Electronic notarization |
|---|---|---|
| Typed signature | Enabled | Enabled only when the selected notarial act permits the process |
| Drawn signature | Enabled | Enabled only when the selected notarial act permits the process |
| Uploaded signature image | Enabled with evidence | Enabled only if allowed by the authorized notarial process |
| Click-to-sign confirmation | Enabled | Not sufficient alone for the notarial act |
| Certificate-backed digital signature | Deferred from first signing release | Required only where the approved notarial process or provider requires it |
| Email/contact verification | Configurable | Not sufficient alone |
| MFA | Configurable by organization/project | Required for the approved remote/notarial session policy |
| Government ID verification | Deferred or provider-driven | Required when the approved ENP/ENF process requires it |
| Liveness/anti-spoofing | Optional provider integration | Required where the approved REN control set requires it |
| Location evidence | Optional and policy-controlled | Required for applicable REN/IEN rules and recorded with the decision |

Every applied signature records signer identity, document version, placement, method, time and time zone, consent, authentication level, session identifier, device/session context, lawful location evidence where required, and validation result.

## 6. Notarization operating baseline

The system must support the following separate records:

1. Ordinary signing project and evidence.
2. Notarization request and selected notarial act.
3. ENP profile and commission record.
4. ENF profile and operating/accreditation record.
5. IEN or REN session evidence.
6. Notarial certificate.
7. Electronic Notarial Book entry and duplicate-book record.
8. Final notarized document version and fingerprint.
9. Verification identity and certificate status.

The notarization workflow must enforce:

- Active ENP commission and ENF association before session start and completion.
- Participant identity and capacity/willingness checks.
- Required principal and witness presence.
- Eligible PDF/PDF/A document and permitted notarial act.
- Locked final document version before the notarial act.
- IEN physical-appearance or REN location/session rules.
- MFA, liveness, anti-spoofing, and anti-VPN controls where configured by the approved process.
- Immutable notarial certificate and Electronic Notarial Book records.
- Retryable but duplicate-safe official synchronization.
- No silent correction of a notarized version; corrections create an authorized new act.

The product must not complete or expose a notarized result when a required control is missing. It must show a safe status such as `NOTARIZATION_BLOCKED`, `COMMISSION_EXPIRED`, `IDENTITY_REVIEW_REQUIRED`, `LOCATION_CHECK_FAILED`, or `SYNC_PENDING`.

## 7. Data, privacy, and retention baseline

This matrix establishes system controls, not a final legal retention opinion.

- Store jurisdiction profile, policy version, consent version, and applicable workflow mode on the project.
- Classify account, organization, participant, identity, location, document, signature, session, certificate, ENB, billing, and webhook data separately.
- Restrict identity, location, session, and ENB evidence to authorized roles and purpose-bound access.
- Make storage region and provider configuration explicit in deployment configuration; do not hard-code a cross-border data-transfer claim into product copy.
- Support organization retention policies, legal holds, pending deletion, and deletion evidence from the first records model.
- Do not perform irreversible hard deletion of signed/notarized evidence unless an approved retention/deletion policy and legal-hold check permit it.
- Preserve audit records for administrative access, export, revocation, correction, and deletion requests.

## 8. Future jurisdiction profiles

Other countries are not part of the initial market baseline. Future profiles must define their own organization and signer availability, signature and consent requirements, identity levels, document rules, storage/data-transfer requirements, retention/deletion rules, certificate wording, and notarization/provider requirements.

The system should use a versioned jurisdiction-policy abstraction rather than branching on country strings throughout business logic.

## 9. Engineering consequences

Implement the baseline with these policy concepts:

- `jurisdictionProfile`: organization/project profile, initially `PH`.
- `policyVersion`: immutable policy identifier stored on a project and relevant evidence events.
- `signatureMode`: ordinary signing or notarization-enabled workflow.
- `notarizationMode`: `IEN`, `REN`, or future approved mode.
- `notarizationStatus`: pending, blocked, scheduled, in-progress, completed, failed, revoked, or cancelled.
- `enpCommissionStatus`: pending, active, expired, suspended, or revoked.
- `enfStatus`: pending, active, suspended, expired, or revoked.
- `documentEligibilityStatus`: eligible, rejected, quarantined, or review-required.
- `locationVerificationStatus`: not-required, pending, passed, failed, or review-required.
- `certificateStatus`: pending, valid, suspended, revoked, superseded, or under-investigation.
- `enbEntryStatus`: pending, recorded, synchronization-pending, synchronized, failed, or correction-required.

These values must be server-owned. Clients may request a mode, but the API decides whether the organization, document, participants, ENP, ENF, and session satisfy the policy.

## 10. Open implementation decisions

| Decision | Owner | Default for system build | Needed before |
|---|---|---|---|
| Object storage provider and region | Platform | Supabase Storage through an adapter | Foundation document upload |
| Identity/liveness provider | Security/Notary Operations | Provider adapter with manual-review state | REN production operation |
| Video-session provider | Platform/Notary Operations | Session adapter and evidence boundary | REN production operation |
| Payment provider | Finance/Backend | Adapter and idempotent callback contract | Credits/billing release |
| Final retention defaults | Product/Operations | Organization-configurable; no destructive default hard-coded | Production launch |
| External official synchronization | Notary Operations | Retryable adapter with manual reconciliation | Notarization production operation |

## 11. Matrix acceptance

This matrix is complete for the current system-build scope when:

- Philippines is recorded as the initial organization market.
- Ordinary electronic signing is enabled.
- Electronic notarization is enabled as a first-class product capability.
- IEN and REN controls are represented.
- Signer-location behavior is explicit.
- Supported, rejected, and quarantined document inputs are defined.
- Signature and identity levels are defined.
- Data, retention, deletion, and legal-hold system behavior is defined without pretending to be legal approval.
- Future markets are isolated behind versioned jurisdiction profiles.
- Open provider decisions have owners and release gates.
