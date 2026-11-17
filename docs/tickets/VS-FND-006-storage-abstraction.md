# VS-FND-006 — Implement Document Storage Abstraction

**Status:** Done for current system-build scope
**Phase:** Phase 1 — Accounts, organizations, projects, documents, storage, and evidence
**Owner:** Platform + Backend
**Dependencies:** VS-P0-010, VS-FND-005
**Mobile:** Not in scope; no mobile files may be changed

## Goal

Create the provider-neutral storage boundary that allows VinciSign to persist document bytes in
Supabase Storage without coupling project/document domain code to Supabase-specific client types.
The boundary must support the synchronous first flow and make private, opaque, metadata-rich
object storage the default.

## Scope

- A backend storage port for storing, referencing, signing downloads, and deleting objects.
- A Supabase Storage adapter implementation behind that port.
- Server-only configuration for the Supabase project URL, private bucket, service-role credential,
  and signed-download lifetime.
- Opaque object-key generation that does not put project, document, organization, email, or
  filename identifiers in predictable storage paths.
- Storage metadata rules for media type, byte length, bucket, object key, and private access
  behavior. Fingerprint calculation remains VS-FND-008.
- Focused unit tests without a live Supabase dependency.

## Explicitly out of scope

- Upload, PDF/PDF-A, corruption, password, or size validation endpoint; VS-FND-007.
- SHA-256 calculation; VS-FND-008.
- PDF password/envelope encryption or client-side encryption. Storage encryption at rest is a
  provider/deployment control; application-level document encryption requires a separate design.
- Malware scanning, quarantine, conversion, workers, Redis, notifications, webhooks, or external
  synchronization.
- Public document URLs, frontend Supabase credentials, or mobile changes.
- Database migration. The document-version storage reference columns were created by VS-FND-005.

## Storage baseline

1. The document bucket is private.
2. The service-role credential is backend-only and is never logged, returned, or exposed as a
   frontend/public environment variable.
3. Download access is represented by a short-lived signed URL or an equivalent authenticated
   server-side read; object URLs are not persisted as durable document metadata.
4. Storage writes use the content type supplied by the validated intake flow and do not overwrite
   an existing object key. Replacement creates a new document version and a new object key.
5. Object keys are generated from random values and a stable category prefix; raw project,
   document, organization, user, email, and filename values are not used as path components.
6. PostgreSQL stores the bucket/key reference and metadata, never normal document bytes.

## Acceptance checklist

- [x] Domain code depends on a provider-neutral storage port rather than Supabase types.
- [x] Supabase Storage adapter supports the synchronous operations needed by VS-FND-007.
- [x] Configuration has no committed secret and service-role credentials remain server-only.
- [x] The adapter assumes a private bucket and exposes only time-limited signed download access.
- [x] Object keys are opaque and tests show that project IDs are not present or sufficient to
      predict a key.
- [x] Content type and byte length are passed as storage metadata and remain available for the
      document-version record.
- [x] Storage/provider failures map to safe application errors without leaking provider details.
- [x] No public endpoint, worker, scanner, Redis dependency, or mobile file is introduced.
- [x] Focused and full backend tests pass and `git diff --check` is clean.

## Verification

```bash
cd apps/backend
./mvnw -q -Dtest='*Storage*Test' test
./mvnw -q -DskipTests compile

cd ../..
git diff --check
```

## Implemented

- Added the provider-neutral `DocumentStorage` port and metadata/upload value objects.
- Added the Supabase adapter and REST transport for immutable-key uploads, signed downloads, and
  object cleanup without exposing Supabase types to document domain code.
- Added conditional server configuration so local runs without Supabase credentials remain valid;
  when configured, credentials are applied only by the backend REST client.
- Added opaque UUID-based object-key generation, safe storage exceptions, content-type/size
  metadata propagation, and focused tests for key opacity, TTL binding, failure masking, and
  blank-credential startup behavior.
- Added backend environment examples and documented the private-bucket/signed-access baseline.

## Follow-up

- VS-FND-007 will call this boundary after validating and accepting a PDF/PDF-A upload.
- VS-FND-008 will calculate SHA-256 over the exact bytes stored through this boundary.
- VS-FND-015 will add storage-outage and recovery coverage across the document flow.
