# VS-FND-007 — Build Document Upload Validation and Working-Version Persistence

**Status:** Done
**Phase:** Phase 1 — Accounts, organizations, projects, documents, storage, and evidence
**Owner:** Backend
**Dependencies:** VS-P0-003, VS-FND-005, VS-FND-006
**Mobile:** Not in scope; no mobile files may be changed

## Goal

Add the first synchronous document-intake endpoint. An authenticated project owner uploads a
ready PDF/PDF-A file, the API validates it before allowing it into the workflow, stores the exact
bytes through the document-storage boundary, and persists a new original/working document version
without exposing provider credentials or accepting client-controlled ownership/status fields.

## API boundary

```text
POST /api/v1/projects/{projectId}/documents
Content-Type: multipart/form-data
file=<PDF/PDF-A bytes>
```

The authenticated subject supplies the caller context. `projectId`, organization ownership,
document/version IDs, storage references, status, and policy fields are server-owned. The initial
response is synchronous and returns the created document/version identifiers, original metadata,
and intake status. It must not expose the service-role key or a permanent public storage URL.

## Scope

- Authenticated multipart upload endpoint under the versioned project/document resource path.
- Server-side project/organization ownership authorization.
- 25 MB default size limit, configurable by backend deployment settings.
- PDF/PDF-A-ready file checks: non-empty, PDF media/extension, readable PDF structure, and no
  password-protected or unsupported encrypted document.
- Supabase Storage persistence through the VS-FND-006 `DocumentStorage` port.
- Initial `Document` and `DocumentVersion` persistence with the accepted bytes as the original and
  working version, status `READY`, storage bucket/key, content type, size, and creator metadata.
- Stable safe error codes/messages based on `docs/document-policy.md`.
- Cleanup of a newly stored object when database persistence fails, without hiding the original
  application error.
- Focused API/service tests for success, authentication/ownership, validation failures, and
  storage failure.

## Explicitly out of scope

- SHA-256 calculation and fingerprint evidence; VS-FND-008.
- Evidence-event persistence; VS-FND-010.
- Malware scanning, quarantine, conversion, DOCX normalization, workers, Redis, notifications,
  webhooks, external synchronization, or KYC.
- Signed-upload URLs, resumable uploads, client-side direct-to-Supabase uploads, or public buckets.
- Signing fields, participants, signing sessions, notarization eligibility, or final artifacts.
- Mobile changes.

## Required behavior

1. Authentication is required; unauthenticated requests fail before storage access.
2. The endpoint checks project ownership/organization scope from server-side records and never
   trusts organization or user IDs from multipart fields.
3. Empty files, files above the configured limit, unsupported types, malformed/corrupt PDFs,
   password-protected PDFs, and unsupported encryption fail with stable safe error codes.
4. Validation completes before the object is stored.
5. Each accepted upload creates one new document/version identity; it never overwrites an existing
   object or mutates a signed/notarized version in place.
6. A storage failure cannot produce a `READY` document/version row. A database failure after a
   successful object write attempts best-effort object cleanup.
7. The accepted version is the document's original and current working version, ready for the
   later fingerprinting flow.
8. Error responses do not reveal Supabase URLs, credentials, stack traces, parser internals, or
   private organization data.

## Acceptance checklist

- [x] Authenticated multipart upload endpoint exists at the documented path.
- [x] Project/organization authorization is server-derived and cross-owner access is rejected.
- [x] Empty, oversized, unsupported, malformed, encrypted, and password-protected files fail with
      stable safe codes/messages.
- [x] Valid PDF/PDF-A bytes are stored through `DocumentStorage` without overwrite.
- [x] The database stores the document/version metadata and storage reference with `READY` status.
- [x] Storage or persistence failures do not leave a false ready state; cleanup is attempted.
- [x] Response exposes stable document/version identifiers and intake metadata without secrets or
      permanent public storage URLs.
- [x] Focused tests and the full backend suite pass; `git diff --check` is clean.
- [x] No mobile or deferred infrastructure changes are introduced.

## Implementation notes

- Added `POST /api/v1/projects/{projectId}/documents` with an authenticated multipart `file` part.
- Project ownership is resolved from the authenticated internal user and the server-side project
  record. Multipart fields cannot set organization, owner, status, version, or storage values.
- PDF parsing uses Apache PDFBox 3.0.8. The application accepts readable PDF/PDF-A-compatible
  files, rejects empty/oversized/unsupported/corrupt/password-protected/encrypted files, and
  sanitizes the stored original filename.
- Storage is called only after validation and uses the existing provider-neutral
  `DocumentStorage` port. Database persistence creates the original and working version at
  `READY`; a failed persistence transaction triggers best-effort object deletion.
- Upload validation and storage failures use stable safe API error codes. No signed URL, public
  bucket, malware scanner, worker, Redis, evidence event, or mobile code was added.

## Verification

```bash
cd apps/backend
./mvnw -q -Dtest='*DocumentUpload*Test,*DocumentUpload*IntegrationTest' test
./mvnw -q test

cd ../..
git diff --check
```

## Follow-up

- VS-FND-008 will fingerprint the exact accepted bytes with SHA-256.
- VS-FND-010 will append upload/validation evidence events.
- VS-FND-012 will add preparation and version-lock behavior before sending.
