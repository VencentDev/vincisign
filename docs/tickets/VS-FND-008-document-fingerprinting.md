# VS-FND-008 — Add Document Fingerprinting for Accepted Working Versions

**Status:** Done
**Phase:** Phase 1 — Accounts, organizations, projects, documents, storage, and evidence
**Owner:** Backend
**Dependencies:** VS-FND-007
**Mobile:** Not in scope; no mobile files may be changed

## Goal

Compute a deterministic SHA-256 fingerprint for the exact bytes accepted by VS-FND-007 and
persist it on the created `DocumentVersion`. The original upload and current working version must
share the fingerprint for the same accepted bytes, while later versions can receive their own
fingerprint without mutating prior history.

## Scope

- SHA-256 calculation over the validated upload byte array before persistence.
- Lowercase, fixed-width 64-character hexadecimal representation.
- Fingerprint algorithm metadata remains `SHA-256` from the existing document-version model.
- Initial upload response includes the persisted fingerprint for API consumers that need to record
  or compare the accepted artifact.
- Tests prove same bytes produce the same fingerprint and different bytes produce different
  fingerprints.
- Upload wiring tests prove the fingerprint is passed into the initial `DocumentVersion` and the
  accepted upload remains the original/current working version.

## Explicitly out of scope

- Evidence-event persistence; VS-FND-010 owns upload and fingerprint evidence events.
- Malware scanning, quarantine, conversion, workers, Redis, notifications, webhooks, or external
  synchronization.
- PDF re-parsing, normalization, encryption, signing, notarization, or final artifact generation.
- Verification endpoints, QR codes, certificate fingerprints, or public hash lookup.
- Mobile and frontend changes.

## Required behavior

1. The digest is computed from the exact bytes returned by the completed VS-FND-007 validator.
2. The digest is calculated before storage/database persistence so the version metadata is created
   in one consistent write flow.
3. SHA-256 output is lowercase hexadecimal and always 64 characters.
4. Identical byte sequences produce identical fingerprints; any byte change produces a different
   fingerprint.
5. The fingerprint is stored on `document_versions.fingerprint` with the existing `SHA-256`
   algorithm marker.
6. Failed validation or storage does not create a fingerprinted ready version.
7. No client-supplied fingerprint, algorithm, owner, document ID, or version status is accepted.
8. Fingerprints do not expose document contents, storage credentials, or storage URLs.

## Acceptance checklist

- [x] SHA-256 fingerprint service exists in the document module.
- [x] Accepted upload computes the digest from exact validated bytes.
- [x] Initial document version persists the fingerprint and `SHA-256` algorithm metadata.
- [x] Upload response exposes the persisted fingerprint without secrets or private storage URLs.
- [x] Same-byte and different-byte behavior is covered by focused tests.
- [x] Failed validation/storage cannot leave a false `READY` fingerprinted version.
- [x] Full backend suite, focused tests, and `git diff --check` pass.
- [x] No evidence event, infrastructure, mobile, or unrelated feature changes are introduced.

## Implementation notes

- Added a document-module SHA-256 service using the JVM `MessageDigest` implementation and
  lowercase hexadecimal output.
- The upload service hashes the exact validated bytes before the storage write, then persists the
  digest on the initial `DocumentVersion`. The existing model default records the `SHA-256`
  algorithm marker.
- The upload response now includes the persisted fingerprint. It does not expose storage URLs,
  credentials, or document contents.
- No migration was required because `document_versions.fingerprint` and
  `document_versions.fingerprint_algorithm` were created by VS-FND-005.

## Verification

```bash
cd apps/backend
./mvnw -q -Dtest='*Fingerprint*Test,*DocumentUpload*Test' test
./mvnw -q test

cd ../..
git diff --check
```

## Follow-up

- VS-FND-010 will persist upload/fingerprint evidence events.
- VS-FND-012 will add preparation and version-lock behavior before sending.
- VS-VERIFY-004 will compare uploaded/final artifact fingerprints during verification.
