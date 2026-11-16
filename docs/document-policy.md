# VinciSign Document and Prohibited-Content Policy

**Policy ID:** `DOC-POLICY-0.2`  
**Status:** Approved for system construction  
**Confirmed:** 2026-11-14  
**Applies to:** Philippines-first ordinary signing and electronic-notarization workflows  
**Scope:** Product and system behavior baseline; not legal advice

Signature methods, consent, and identity requirements are defined in [`signature-consent-identity-policy.md`](signature-consent-identity-policy.md).

## 1. Purpose

This policy defines the small document-intake flow for the first VinciSign implementation. It prevents the upload pipeline from treating every file as a valid signing document and keeps ordinary signing eligibility separate from notarization eligibility.

The policy applies consistently to dashboard uploads, API uploads, partner-platform references, retries, replacement versions, and administrative reprocessing.

## 2. Policy principles

1. Store the accepted PDF/PDF-A upload as an immutable document version.
2. Never silently replace an original or signed document version.
3. A file must pass basic type, size, readability, and password/encryption validation before it can be prepared or sent.
4. Ordinary signing eligibility does not imply electronic-notarization eligibility.
5. Notarization eligibility is checked again immediately before the notarial act.
6. Every accept, reject, fingerprint, replacement, and eligibility decision creates an evidence event.
7. User-facing errors explain the action required without exposing internal storage details or sensitive information.
8. Organization policy may be stricter than this baseline but may not weaken server-owned safety controls.

Malware scanning, document conversion, background workers, notifications, webhooks, and external synchronization are deferred. The initial implementation must not pretend that a deferred control has run.

## 3. File intake baseline

| Rule | Initial value | Enforcement |
|---|---|---|
| Maximum file size | 25 MB per file by default | Configurable deployment/organization setting; reject before storage when exceeded |
| Maximum project documents | 20 files by default | Configurable; sender sees the limit before upload |
| Directly accepted formats | PDF, PDF/A | Store the upload and create one immutable working-version record |
| Normalizable ordinary-signing format | None in the initial flow | Reject; conversion is deferred |
| Future formats | Disabled until a policy version adds them | Reject with unsupported-format code |
| Empty file | Rejected | Validate byte length and parser result |
| Corrupted file | Rejected | Readability/parser failure; no ready state |
| Password-protected/encrypted file | Rejected | Do not request or store passwords in the upload flow |
| Malware scanning | Deferred | Do not expose a scan result or require a scanner service in the initial flow |
| Original/working version | One accepted PDF/PDF-A version | Store the upload as the working version |
| Working version | Replaceable only by explicit new version | Fingerprint every version and link replacement evidence |

The default limits are system-build values. They must be configurable without changing application code and stored with the project policy snapshot where they affect an upload decision.

## 4. Ordinary signing eligibility

An ordinary-signing project may accept a document when:

- The file is PDF or PDF/A.
- The file passes size, readability, corruption, and encryption checks.
- The organization permits the document category.
- The sender has permission to create the project and use the selected workflow.
- Required document metadata and policy version are stored.
- The accepted upload has its fingerprint.
- No blocking review state remains.

Ordinary signing supports these initial document uses:

- Agreements and contracts.
- Employment and onboarding documents.
- Procurement and vendor documents.
- Customer acknowledgements and approvals.
- Internal policies and attestations.
- Forms and workflow records.
- Other organization-permitted documents that do not require the separate notarization path.

The product must not describe ordinary signing as notarization, government certification, or guaranteed legal enforceability.

## 5. Electronic-notarization eligibility

Electronic notarization is enabled as a first-class capability, but a document must pass additional checks before a notarial session can start or complete.

### Required baseline

- Final document is PDF or PDF/A.
- Document is not password-protected or unsupported encrypted.
- Document passes the same basic readability and encryption checks as ordinary signing.
- Document is eligible for the selected Philippine notarial act.
- Required principal and witness information is present.
- The final working version is locked before the act.
- An active authorized ENP is assigned.
- The ENP is associated with an active approved ENF process.
- Required identity, capacity, willingness, and authentication checks pass.
- IEN physical-appearance or REN location/session checks pass.
- Required signatures, electronic seal, certificate, and Electronic Notarial Book records can be produced.

### Notarization exclusions in this baseline

- Notarial wills.
- Depositions.
- Files that are not PDF/PDF/A at the notarial eligibility checkpoint.
- Pre-signed documents when the selected notarial act requires signing in the ENP’s presence.
- Documents with unresolved identity, witness, location, commission, session, or version-lock failures.
- Any document or act rejected by the configured jurisdiction policy or authorized ENF/ENP process.

The exclusion result must be recorded as a structured eligibility decision. It must not be bypassed by a dashboard action, API client, administrator, retry, or webhook replay.

## 6. Prohibited and rejected content

### Always reject at intake

- Empty, malformed, corrupted, or unreadable files.
- Unsupported file types.
- Files exceeding the configured size or project count limit.
- Password-protected or unsupported encrypted files.
- Files with invalid or unsafe parser output.
- Content explicitly prohibited by the organization’s acceptable-use policy.
- Notarial wills and depositions when submitted for electronic notarization.

### Deferred security and manual-review behavior

The initial implementation has no malware scanner or quarantine workflow. A future security extension may add those states, but it must be introduced by a separate policy version and ticket. Until then, documents fail closed on basic validation and workflow eligibility checks.

### Reject at workflow validation

These inputs may be technically readable but cannot proceed in the selected workflow:

- A document missing required participant or witness information.
- A document with fields assigned to a participant who cannot act in the selected role.
- A document whose final version changes after send or lock.
- A document selected for notarization without an eligible act type.
- A document with an expired, suspended, or revoked ENP/ENF dependency.
- A document with failed required identity or location verification.
- A document sent after cancellation, expiry, or revocation.

## 7. Upload and validation pipeline

```text
Received
  -> Intake metadata recorded
  -> Size/type validation
  -> Accepted bytes stored
  -> Parser/corruption check
  -> Encryption/password check
  -> Fingerprint the accepted working version
  -> Policy classification
  -> Ordinary signing eligible OR notarization review required
```

Blocking states:

```text
Rejected
Eligibility review required
```

Rules:

- A failed step does not advance the project to the next state.
- Repeating the same request is idempotent for the same document version.
- Replacing the source file creates a new `DocumentVersion`; it does not mutate the prior version.
- The project stores the policy ID/version and validation result that produced the decision.

## 8. Stable rejection and review codes

| Code | User-facing message | State | Next action |
|---|---|---|---|
| `FILE_EMPTY` | The uploaded file is empty. Choose a file with content and try again. | Rejected | Upload another file |
| `FILE_TOO_LARGE` | This file exceeds the allowed size of {limit}. | Rejected | Reduce file size or contact the organization admin |
| `PROJECT_FILE_LIMIT` | This project has reached its document limit. | Rejected | Remove an allowed draft version or create a new project |
| `FILE_TYPE_UNSUPPORTED` | This file type is not supported for this workflow. | Rejected | Upload a PDF or PDF/A file |
| `FILE_CORRUPT` | We could not read this file. It may be damaged. | Rejected | Export or save a new copy and retry |
| `FILE_PASSWORD_PROTECTED` | Password-protected files cannot be processed. | Rejected | Remove protection and upload a permitted copy |
| `FILE_ENCRYPTION_UNSUPPORTED` | This encryption format cannot be processed. | Rejected | Upload a permitted unencrypted file |
| `DOCUMENT_POLICY_BLOCKED` | This document is not allowed by the organization or workflow policy. | Rejected | Review the project policy |
| `NOTARIZATION_FORMAT_REQUIRED` | Electronic notarization requires an eligible PDF or PDF/A document. | Rejected | Upload an eligible PDF/PDF/A |
| `NOTARIZATION_DOCUMENT_EXCLUDED` | This document type is not eligible for electronic notarization. | Rejected | Use the permitted workflow or consult the authorized notary |
| `NOTARIZATION_ELIGIBILITY_REVIEW` | This document requires an eligibility review before notarization. | Review required | Complete authorized review |
| `NOTARIZATION_PARTICIPANTS_INCOMPLETE` | Required principals or witnesses are missing. | Rejected | Add the required participants |
| `NOTARIZATION_CONTROL_FAILED` | A required identity, location, commission, or session check did not pass. | Blocked | Resolve the failed control |
| `DOCUMENT_VERSION_LOCKED` | This document version is locked and cannot be replaced in place. | Rejected | Create a new document version |

Messages must not reveal private identity details or internal infrastructure information.

## 9. API and data requirements

Every upload/eligibility response should expose, where permitted:

- `documentId` and `documentVersionId`.
- Original filename and media type.
- Working-version fingerprint.
- Intake status and validation status.
- Eligibility status for ordinary signing and notarization separately.
- `policyId` and `policyVersion`.
- Stable error/review code and safe message.
- Evidence event identifier.
- Retryability and next action.

Server-owned fields include validation status, fingerprints, eligibility status, notarization status, and policy decision. Clients may request a workflow, but cannot mark a file eligible, notarized, or ready to send.

## 10. Engineering implementation notes

- Add policy configuration rather than scattering file type and size checks across controllers.
- Use a small PDF/PDF-A validation service in the request path; do not add a worker or external processing provider for the initial flow.
- Store the accepted upload as the working-version object and keep its metadata immutable after creation.
- Add evidence events for upload, validation, fingerprint, eligibility decision, replacement, and rejection.
- Add integration tests for duplicate uploads, storage failure, corrupted files, password-protected files, replacement versions, and ordinary-signing versus notarization eligibility.
- Treat `policyVersion` as part of the project snapshot so a later policy change does not silently reclassify an existing project.

## 11. Deferred extensions

The following are intentionally not part of the initial implementation: malware scanning, quarantine/release, DOCX normalization, background processing, notification delivery, webhooks, and external synchronization. Adding any of them requires a new ticket and policy/architecture update.

| Decision | Owner | Initial status |
|---|---|---|
| Default file size | Product/Platform | 25 MB, configurable |
| Default project file count | Product/Platform | 20 files, configurable |
| Organization document categories | Product | Organization policy may restrict the baseline |
| Manual eligibility review role | Product/Operations | Required only for a workflow that explicitly enters review |

## 12. Policy acceptance

This policy is complete for the current system-build scope when:

- Supported file formats and size limits are explicit.
- Accepted PDF/PDF-A intake, immutable storage, and working-version behavior are explicit.
- Corruption and encryption behavior is explicit.
- Ordinary signing eligibility is separate from electronic-notarization eligibility.
- PDF/PDF/A and Philippine electronic-notarization exclusions are explicit.
- Prohibited, rejected, and review-required categories are explicit.
- Stable user-facing rejection/review codes are defined.
- Server-owned eligibility fields and evidence requirements are defined.
- Deferred integrations are clearly excluded from the initial implementation.
