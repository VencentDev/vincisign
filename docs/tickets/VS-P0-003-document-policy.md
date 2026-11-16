# VS-P0-003 — Define Document and Prohibited-Content Policy

**Status:** Done for current system-build scope  
**Phase:** Phase 0 — Product, legal, and architecture foundation  
**Window:** S02 / 2026-11-30 – 2026-12-11  
**Owner:** Product  
**Estimate:** 4 days  
**Dependencies:** VS-P0-002

## Goal

Define what VinciSign accepts or rejects in the initial PDF/PDF-A flow, with separate rules for ordinary signing and Philippines-first IEN/REN workflows.

## Deliverable

- [`docs/document-policy.md`](../document-policy.md)

## Acceptance checklist

- [x] Supported file types and size limits are defined.
- [x] Accepted PDF/PDF-A storage and immutable working-version behavior are defined.
- [x] Corrupted, password-protected, and encrypted inputs have explicit behavior.
- [x] Ordinary-signing eligibility is separate from electronic-notarization eligibility.
- [x] PDF/PDF/A requirements for electronic notarization are explicit.
- [x] Notarial wills and depositions are excluded from the Philippine electronic-notarization baseline.
- [x] Prohibited, rejected, and review-required categories are defined.
- [x] Stable user-facing rejection and review codes are defined.
- [x] Server-owned eligibility fields and evidence requirements are defined.
- [x] Provider decisions have owners and system-build defaults.
- [x] No mobile files were changed.

## Notes and rulings

- **Ruling:** PDF/PDF-A is the only accepted format in the initial flow; DOCX normalization is deferred.
- **Amendment:** The initial implementation accepts PDF/PDF-A only. DOCX normalization, malware scanning, quarantine, background processing, notifications, webhooks, and external synchronization are deferred and must not be added to the first flow.
- **Ruling:** The accepted PDF/PDF-A upload is stored as the working version, fingerprinted, and recorded with evidence synchronously.
- **Ruling:** The initial default is 25 MB per file and 20 files per project, both configurable so deployment constraints do not require a code change.
- **Ruling:** Notarial wills and depositions are rejected for the Philippine electronic-notarization workflow, while ordinary signing eligibility remains a separate decision.
- **Ruling:** Security and provider failures quarantine or block the document rather than allowing a project to proceed optimistically.

## Verification

- `git diff --check`
- Confirm acceptance items against `docs/document-policy.md`.
- Confirm the policy agrees with [`docs/jurisdiction-market-matrix.md`](../jurisdiction-market-matrix.md).
- Confirm no `apps/mobile` files are modified.
