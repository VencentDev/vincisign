# VinciSign Signature, Consent, and Identity Policy

**Policy ID:** `SIGNATURE-IDENTITY-0.1`  
**Status:** Approved for system construction  
**Confirmed:** 2026-11-14  
**Applies to:** Philippines-first ordinary signing and electronic-notarization workflows  
**Scope:** Product and system behavior baseline; not legal advice

## 1. Purpose

This policy defines the signature methods VinciSign supports, the consent records required before a participant acts, the identity assurance levels the platform can record, and the evidence required for ordinary signing and electronic notarization.

An identity level describes evidence collected by the platform. It does not, by itself, make a legal conclusion about a person or guarantee enforceability.

## 2. Core rules

1. A signature method, consent record, identity level, and document version are separate records.
2. A participant cannot apply a signature until the required consent and access checks pass.
3. Email address or guest-link access is not proof of complete legal identity.
4. The sender chooses project requirements within organization policy; the server enforces the minimum required by the workflow.
5. Notarization uses a separate control set from ordinary signing and requires an authorized ENP/ENF process.
6. The final signature evidence references the exact immutable document version signed.
7. Changing a signature or identity policy after a project is sent does not silently weaken that project’s captured requirements.
8. Replayed requests cannot create duplicate applied signatures, consent records, charges, or completion events.

## 3. Signature method matrix

| Method | Ordinary signing | Electronic notarization | Initial treatment |
|---|---|---|---|
| Typed signature | Enabled | Enabled only when allowed by the selected notarial act/process | Store rendered signature and method evidence |
| Drawn signature | Enabled | Enabled only when allowed by the selected notarial act/process | Store rendered signature and method evidence |
| Uploaded signature image | Enabled with sender/organization policy | Only when explicitly permitted by the authorized notarial process | Preserve image reference and checksum; do not treat image alone as identity proof |
| Click-to-sign confirmation | Enabled | Not sufficient alone for the notarial act | Record intent, confirmation, timestamp, and assigned field |
| Certificate-backed digital signature | Deferred from first ordinary-signing release | Enabled when required by the authorized ENP/ENF process or provider | Provider integration boundary; certificate status is separate from completion certificate |
| ENP-applied electronic signature/seal | Not applicable | Required for the authorized notarial act | Server-gated by active ENP/ENF and notarial session state |

Each method must expose a stable method identifier and a human-readable explanation before the participant confirms the action.

## 4. Signature evidence requirements

For every applied signature or signature-like action, store:

- Signature event identifier.
- Participant and account identifier, when available.
- Participant role and assigned field identifier.
- Project, document, and exact DocumentVersion identifiers.
- Signature method and rendered-placement reference.
- Signature creation time and time zone.
- Electronic-record consent reference.
- Electronic-signature consent reference.
- Authentication and identity level at the time of signing.
- Signing-session identifier.
- Device/session context permitted by policy.
- Location evidence where required and lawfully collected.
- Request identifier and idempotency result.
- Validation result and any certificate/provider reference.
- Previous/current participant status transition.

Signature evidence is append-oriented. A correction, invalidation, or replacement creates a new event and status rather than erasing the original event.

## 5. Consent model

### 5.1 Consent records

VinciSign records consent separately for:

1. **Electronic-record consent** — participant agrees to receive, access, and use electronic records for the project.
2. **Electronic-signature consent** — participant agrees that the selected action is intended to function as an electronic signature or approval.
3. **Project disclosure acknowledgement** — participant acknowledges sender, document, role, required actions, expiration, and access/download rules.
4. **Identity and security consent** — participant acknowledges required authentication, identity, device, session, location, liveness, or video checks.
5. **Notarial consent** — participant acknowledges the selected notarial act, ENP/ENF process, IEN/REN mode, session requirements, and separate notarial records.
6. **Privacy/processing acknowledgement** — participant is shown the applicable data-processing notice and policy version.

The project policy determines which consent records are required. Ordinary signing may not require notarial consent. A notarization request always requires the notarial consent set before the notarial session can start.

### 5.2 Consent record fields

Each consent record stores:

- Consent identifier and type.
- Policy/document version shown to the participant.
- Participant and project identifiers.
- Exact consent text or immutable content reference.
- Locale/language.
- Presented time and consented time.
- Time zone.
- Authentication and identity level.
- Session and device context permitted by policy.
- IP/network/location evidence where configured and lawful.
- Accepted, declined, withdrawn, expired, or superseded status.
- Request and evidence event identifiers.

### 5.3 Consent behavior

- Required consent must be accepted before the dependent action is available.
- Declining required consent blocks the dependent action and records the decline reason/status.
- Withdrawal does not erase a historical consent record; it changes the current consent state and creates an event.
- Updating consent text creates a new policy version. Existing projects retain the version shown at the time of consent.
- A participant must be able to review the document and required notices before confirming intent to sign.
- Consent copy must explain what VinciSign records and what it does not certify.

## 6. Identity assurance levels

| Level | Name | Evidence baseline | Ordinary signing | Notarization |
|---|---|---|---|---|
| I0 | Unverified account | Account record only | May view public/pre-auth surfaces; cannot satisfy a project requirement that needs verified contact | Not sufficient |
| I1 | Contact verified | Verified email or phone challenge | Allowed when project requires contact verification | Not sufficient alone |
| I2 | Authenticated with MFA | I1 plus approved MFA challenge | Allowed when project requires stronger authentication | Required where the approved process requires MFA |
| I3 | Organization verified | I1/I2 plus organization membership/authorization evidence | Required for senders, organization administrators, and organization-controlled signing | Required for organization-side request/approval roles |
| I4 | Government-ID verified | Approved identity provider result and evidence reference | Optional/provider-driven for ordinary projects | Required when the authorized ENP/ENF process requires it |
| I5 | ENP verified | I4 or approved professional identity evidence plus commission/ENF records | Not needed for ordinary signers | Required for an ENP performing or completing a notarial act |
| I6 | Enterprise authorized | Organization authorization, role, policy, and SSO/provisioning evidence | Required when enterprise policy requires it | Required for enterprise notarial administration when configured |

The platform must not infer a higher level merely because a user has completed a lower-level action. Each level has an evidence record, verification timestamp, expiry/recheck policy, and provider/status reference where applicable.

## 7. Project requirement policy

The sender may configure the minimum participant requirement within organization policy:

- Account optional or account required.
- Contact verification optional or required.
- MFA optional or required.
- Government-ID verification optional, required, or provider-driven.
- Witness required or not required.
- Sequential or concurrent signing.
- Location evidence optional or required.
- Notarial act and IEN/REN mode when notarization is selected.

Server enforcement rules:

- A sender must meet the organization’s sender/creator requirement.
- A signer must meet the participant’s configured minimum before signing.
- A witness must meet the witness requirement before the dependent signature/act can complete.
- An ENP must have active commission and ENF authorization before a notarial session can start or complete.
- A requested identity level cannot be marked passed by a client request; it requires a server/provider evidence record.
- A project stores the effective requirement and policy version when sent.

## 8. Guest signing

Guest signing is enabled for ordinary signing when the sender’s organization permits it.

Guest signing must use:

- Expiring, scoped access link or equivalent access token.
- Participant binding to the intended recipient/contact.
- One-time or replay-resistant signing completion.
- Required electronic-record and electronic-signature consent.
- Configured contact verification or MFA when required.
- Clear copy stating that guest access is not complete legal identity proof.
- View, consent, field, signature-intent, signature-applied, and validation events.

Guest signing is not sufficient by itself for electronic notarization. A notarization request applies the approved identity, location, session, ENP, ENF, principal, and witness requirements even if the participant began as a guest recipient.

## 9. Ordinary signing flow

```text
Invitation/access
  -> participant binding
  -> required notices presented
  -> electronic-record consent
  -> required authentication/identity checks
  -> document review
  -> required fields completed
  -> signature intent confirmed
  -> signature method selected
  -> signature applied
  -> signature validated
  -> participant status advanced
```

The participant can distinguish these states:

- Viewed.
- Acknowledged.
- Consent accepted.
- Signature intent confirmed.
- Signature applied.
- Signature validated.
- Declined.
- Abandoned.
- Blocked by policy or failed control.

## 10. Electronic-notarization flow

Electronic notarization is a separate flow built on top of, but not equivalent to, ordinary signing:

1. Customer selects a notarial act and IEN/REN mode.
2. Platform checks document policy and notarial eligibility.
3. Principals and witnesses are identified and assigned.
4. ENP and ENF are selected or assigned.
5. Required notarial consent and privacy notices are accepted.
6. Identity, MFA, liveness, location, and session checks run according to the approved process.
7. The final document version is locked.
8. Required signatures and ENP seal/signature are applied according to the act.
9. Notarial certificate and Electronic Notarial Book records are created.
10. Final notarized document is fingerprinted and verified.

The system must block completion when a required control is missing, expired, suspended, failed, or inconsistent with the selected act.

## 11. Stable policy codes

| Code | Meaning | User action |
|---|---|---|
| `CONSENT_REQUIRED` | A required consent has not been accepted | Review and accept the required notice |
| `CONSENT_DECLINED` | Required consent was declined | Contact sender or leave the project |
| `CONSENT_VERSION_CHANGED` | A new policy version must be presented | Review the updated notice |
| `IDENTITY_LEVEL_REQUIRED` | Current evidence does not meet the project requirement | Complete the required verification |
| `IDENTITY_REVIEW_REQUIRED` | Automated identity result needs authorized review | Wait for or complete review |
| `MFA_REQUIRED` | MFA is required before this action | Complete MFA |
| `PARTICIPANT_MISMATCH` | Access does not match the assigned participant | Use the intended invitation or contact sender |
| `SIGNATURE_METHOD_NOT_ALLOWED` | Selected method is not allowed for this workflow | Choose an allowed method |
| `SIGNATURE_FIELD_INCOMPLETE` | Required fields are incomplete | Complete the required fields |
| `SIGNATURE_SESSION_EXPIRED` | Signing session expired | Request a new session |
| `SIGNATURE_REPLAY_DETECTED` | Request was already applied or cannot be replayed | Refresh status; do not retry with a new payload |
| `WITNESS_REQUIRED` | Required witness action is incomplete | Add or complete the witness action |
| `ENP_NOT_ELIGIBLE` | ENP commission or authorization is not active | Assign an eligible ENP |
| `ENF_NOT_ELIGIBLE` | ENF authorization/status is not active | Use an eligible ENF process |
| `NOTARIAL_CONSENT_REQUIRED` | Notarial consent set is incomplete | Review and accept the notarial notices |
| `NOTARIAL_CONTROL_FAILED` | A notarial identity, location, or session control failed | Resolve the control before retrying |

## 12. API and data requirements

Server-owned fields should include:

- `signatureMethod`.
- `identityLevelRequired` and `identityLevelAchieved`.
- `consentStatus` and consent policy version references.
- `authenticationStatus` and MFA status.
- `participantBindingStatus`.
- `signatureIntentStatus`.
- `signatureAppliedStatus` and `signatureValidationStatus`.
- `notarialConsentStatus`.
- `enpEligibilityStatus` and `enfEligibilityStatus`.
- `witnessStatus`.
- `locationVerificationStatus` where required.
- `signatureEvidenceId` and linked event IDs.

Clients may request a signature method or identity requirement, but they cannot mark consent accepted, identity verified, signature applied, ENP eligible, or notarial control passed.

## 13. Open implementation decisions

| Decision | Owner | Current system-build default |
|---|---|---|
| MFA provider | Identity/Security | Existing identity provider with a server-side challenge result |
| Government-ID provider | Security/Notary Operations | Adapter with manual-review state |
| Liveness provider | Security/Notary Operations | Adapter required for approved REN process |
| Signature rendering | Backend/Web | Deterministic server-side artifact generation |
| Certificate-backed signature provider | Security/Notary Operations | Deferred for ordinary signing; provider adapter for notarization when required |
| Consent localization | Product/Operations | Versioned copy with English baseline and locale field |
| Guest-link duration | Product/Security | Configurable project policy with expiration required |

## 14. Policy acceptance

This policy is complete for the current system-build scope when:

- Initial signature methods and their workflow availability are explicit.
- Signature evidence requirements are explicit.
- Consent types, fields, and state transitions are explicit.
- Identity levels and evidence requirements are explicit.
- Guest-signing limits are explicit.
- Ordinary and notarization flows are separate.
- Stable policy codes are defined.
- Server-owned fields and client restrictions are defined.
- Provider decisions have owners and system-build defaults.
