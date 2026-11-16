# VS-P0-004 — Define Signature, Consent, and Identity Levels

**Status:** Done for current system-build scope  
**Phase:** Phase 0 — Product, legal, and architecture foundation  
**Window:** S02–S03 / 2026-11-30 – 2026-12-25  
**Owner:** Product  
**Estimate:** 5 days  
**Dependencies:** VS-P0-002, VS-P0-003

## Goal

Define VinciSign’s supported signature methods, required consent records, identity assurance levels, guest-signing rules, and evidence requirements for ordinary signing and electronic notarization.

## Deliverable

- [`docs/signature-consent-identity-policy.md`](../signature-consent-identity-policy.md)

## Acceptance checklist

- [x] Typed, drawn, uploaded-image, click-to-sign, certificate-backed, and ENP-applied methods are defined.
- [x] Ordinary-signing and notarization method availability are separated.
- [x] Signature evidence fields are defined.
- [x] Electronic-record, electronic-signature, project, identity/security, notarial, and privacy consent types are defined.
- [x] Consent versions, timestamps, statuses, and withdrawal behavior are defined.
- [x] Identity levels from unverified through ENP/enterprise authorization are defined.
- [x] Guest signing rules explicitly state that guest access is not full identity proof.
- [x] Ordinary signing and electronic-notarization flows are separate.
- [x] Stable policy codes and server-owned fields are defined.
- [x] Provider decisions have owners and system-build defaults.
- [x] No mobile files were changed.

## Notes and rulings

- **Ruling:** Typed, drawn, uploaded-image, and click-to-sign methods are enabled for ordinary signing; certificate-backed signing is deferred for ordinary signing but remains an adapter requirement for notarization when the approved process requires it.
- **Ruling:** Identity levels describe platform evidence and do not independently make a legal identity or enforceability determination.
- **Ruling:** Guest signing is enabled for ordinary projects but never satisfies notarization identity requirements by itself.
- **Ruling:** Consent is versioned and preserved as evidence; withdrawal changes current status without erasing the historical record.

## Verification

- `git diff --check`
- Confirm acceptance items against `docs/signature-consent-identity-policy.md`.
- Confirm the policy agrees with [`docs/jurisdiction-market-matrix.md`](../jurisdiction-market-matrix.md) and [`docs/document-policy.md`](../document-policy.md).
- Confirm no `apps/mobile` files are modified.
