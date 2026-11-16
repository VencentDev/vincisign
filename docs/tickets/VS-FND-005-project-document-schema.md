# VS-FND-005 — Create Project and Document-Version Schema

**Status:** Done for current system-build scope
**Phase:** Phase 1 — Accounts, organizations, projects, documents, storage, and evidence
**Owner:** Backend
**Dependencies:** VS-P0-007, VS-FND-002
**Mobile:** Not in scope; no mobile files may be changed

## Goal

Create the persistent project/document model that later upload, signing, verification, and
notarization work can build on. A project is the organization/workspace-scoped transaction
boundary. A document is its stable logical identity. A document version is an immutable
point-in-time representation of document metadata and storage references.

## Scope

- Project entity, lifecycle status, organization/workspace ownership, and creator metadata.
- Document entity linked to a project, with explicit original and working version references.
- Document-version entity with version number, role, lifecycle status, file metadata, storage
  reference fields, fingerprint fields, replacement lineage, and lock/immutability metadata.
- Repositories and indexes for project ownership, document lookup, and version history.
- Flyway migration with foreign keys, status/type checks, uniqueness constraints, and database
  protections for locked/final versions.
- Focused integration/repository coverage for creation, replacement history, references, and
  immutable-version protection.

## Explicitly out of scope

- Upload endpoints and PDF/PDF-A/password/corruption validation; VS-FND-007.
- Storage adapters or Supabase Storage calls; VS-FND-006.
- Fingerprint calculation; VS-FND-008.
- Participant, field, signing-session, evidence-event, certificate, Vault, or notarization
  workflow tables.
- Mobile changes, Redis, workers, malware scanning, email, webhooks, or other infrastructure.

## Required model behavior

1. A project belongs to one organization and workspace and has a server-owned lifecycle status.
2. A document belongs to one project and keeps its logical identity across replacements.
3. A document can point to its original version and current working version explicitly.
4. Every replacement creates a new document-version row; existing version bytes/metadata are not
   overwritten in place.
5. Version history records the superseded version relationship and monotonically increasing
   version number per document.
6. Signed, notarized, archived, or explicitly immutable versions cannot be updated or deleted
   through JPA/service code or direct SQL.
7. The database allows a working version to transition into a final/locked state, then blocks
   later mutation of that row.
8. Storage bytes are represented by references and metadata only; bytes do not belong in the
   relational tables.

## Acceptance checklist

- [x] Project, Document, and DocumentVersion entities exist in the established backend module
      conventions.
- [x] Project lifecycle status values match `docs/project-lifecycle-policy.md`.
- [x] Document-version roles/statuses match `docs/domain-vocabulary.md` and
      `docs/document-policy.md`.
- [x] Original and working version references are persisted.
- [x] Replacement creates a new version row with history and does not overwrite the prior row.
- [x] Project/document/version ownership and query indexes are present.
- [x] Signed/notarized/archived/immutable version mutation is rejected at the service boundary.
- [x] The database rejects direct updates/deletes of already locked/final versions.
- [x] Flyway migration applies successfully to Testcontainers PostgreSQL.
- [x] Integration/repository tests cover the acceptance behavior.
- [x] OpenAPI is unchanged because this ticket introduces no public endpoint.
- [x] Mobile remains untouched.
- [x] No external infrastructure is introduced.

## Verification

```bash
cd apps/backend
./mvnw -q -Dtest=ProjectDocumentSchemaIntegrationTest test
./mvnw -q test

cd ../..
git diff --check
```

## Implemented

- Added organization/workspace-scoped `Project`, `Document`, and `DocumentVersion` persistence
  models with explicit version roles and repository queries that require organization scope.
- Added Flyway `V9__project_document_versions.sql` with lifecycle checks, version uniqueness,
  replacement lineage, storage-reference-only fields, tenant-safe foreign keys, and an immutable
  version trigger.
- Added a service boundary for initial versions, replacements, mutable metadata updates,
  send-locking, finalization, and deletion guards.
- Added Testcontainers integration coverage for references, replacement history, service guards,
  and direct SQL mutation/deletion protection.

## Follow-up

- VS-FND-006 will connect the storage references to the local/Supabase Storage adapter boundary.
- VS-FND-007 will create the upload and validation flow that produces the first accepted
  original/working version.
- VS-FND-008 will calculate and persist fingerprints for accepted working versions.
