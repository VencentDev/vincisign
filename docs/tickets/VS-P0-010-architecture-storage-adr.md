# VS-P0-010 — Complete Architecture and Storage Decision Record

**Status:** Done for current system-build scope  
**Phase:** Phase 0 — Product, legal, and architecture foundation  
**Window:** S03–S04 / 2026-12-14 – 2027-01-08  
**Owner:** Backend + Platform  
**Estimate:** 6 days  
**Dependencies:** VS-P0-006, VS-P0-008

## Goal

Record the smallest architecture needed for the first project/document flow: API intake, Supabase Storage, PostgreSQL metadata, SHA-256 fingerprints, evidence events, and repository naming.

## Deliverable

- [`docs/adr/0008-vincisign-platform-foundation.md`](../adr/0008-vincisign-platform-foundation.md)

## Acceptance checklist

- [x] Architecture decisions are confirmed by the project owner for the current system-build scope.
- [x] First-product modular API architecture is accepted.
- [x] PostgreSQL metadata/workflow system of record is accepted.
- [x] Supabase Storage boundary for document bytes and artifacts is accepted.
- [x] The initial PDF/PDF-A upload is stored as the immutable working document version.
- [x] SHA-256 fingerprint decision and claim boundaries are accepted.
- [x] PostgreSQL-backed append-oriented/hash-linked evidence storage is accepted.
- [x] The initial flow is synchronous and does not require Redis, workers, conversion, malware scanning, notification providers, webhooks, or external synchronization.
- [x] External integrity anchoring is explicitly optional and non-authoritative.
- [x] The Java package target is confirmed as `com.vincisign.backend`; the current workspace `@app/*` scope remains unchanged.
- [x] No mobile files were changed.

## Notes and rulings

- **Ruling:** Keep the first product in one modular Spring Boot service while enforcing explicit domain boundaries.
- **Ruling:** PostgreSQL owns metadata, workflow, and evidence; Supabase Storage owns document bytes and large artifacts.
- **Ruling:** The initial request flow is PDF/PDF-A upload → Supabase Storage → SHA-256 → PostgreSQL metadata/evidence → response.
- **Ruling:** Redis, background workers, PDF conversion, malware scanning, notifications, webhooks, and external synchronization are deferred because they require additional infrastructure or provider operations.
- **Ruling:** SHA-256 is the primary byte fingerprint; it is not represented as proof of identity, intent, notarization, or enforceability.
- **Ruling:** Blockchain/external anchoring is optional and never the only source of evidence.
- **Ruling:** `com.vincisign.backend` is the confirmed Java package target. The workspace keeps `@app/*` until a separate frontend migration decision.

## Verification

- `git diff --check`
- Confirm acceptance items against [`docs/adr/0008-vincisign-platform-foundation.md`](../adr/0008-vincisign-platform-foundation.md).
- Confirm storage/fingerprint decisions agree with [`docs/document-policy.md`](../document-policy.md) and [`docs/evidence-event-contract.md`](../evidence-event-contract.md).
- Confirm no `apps/mobile` files are modified.
