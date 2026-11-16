# VS-P0-002 — Create Jurisdiction and Market Matrix

**Status:** Done for current system-build scope  
**Phase:** Phase 0 — Product, legal, and architecture foundation  
**Window:** S01–S02 / 2026-11-16 – 2026-12-11  
**Owner:** Product  
**Estimate:** 5 days  
**Dependencies:** VS-P0-001

## Goal

Define the initial jurisdiction and market baseline that controls VinciSign’s system behavior: Philippines-first organization launch, ordinary electronic signing, electronic notarization, documents, signatures, identity, data handling, retention, and future-market isolation.

## Confirmed baseline

- Philippines is the initial organization market.
- Ordinary electronic signing is enabled.
- Electronic notarization is enabled as a first-class product capability.
- IEN and REN are represented as supported notarization modes.
- Ordinary signing may include participants outside the Philippines.
- Notarization is controlled by ENP/ENF, document, identity, location, session, certificate, and Electronic Notarial Book checks.
- Formal legal/compliance review is deferred from the current system-build scope; it is not recorded as completed approval.

## Deliverable

- [`docs/jurisdiction-market-matrix.md`](../jurisdiction-market-matrix.md)

## Acceptance checklist

- [x] Philippines is the initial organization jurisdiction profile.
- [x] Ordinary electronic signing is enabled.
- [x] Electronic notarization is enabled as a first-class system capability.
- [x] IEN and REN operating requirements are represented.
- [x] Organization and participant location rules are explicit.
- [x] Supported, rejected, and quarantined document inputs are defined.
- [x] Signature and identity levels are defined.
- [x] Data classification, storage configuration, retention, deletion, and legal-hold behavior are defined as system controls.
- [x] Future jurisdictions are isolated behind versioned profiles.
- [x] Open provider decisions have owners and release gates.
- [x] No mobile files were changed.

## Notes and rulings

- **Ruling:** Philippines is the first organization market because the source plan defines the Philippine electronic-notarization model and the user confirmed Philippines-first.
- **Ruling:** Electronic notarization is included as an enabled product capability rather than deferred or disabled. Production execution remains server-gated by active ENP/ENF credentials and required control evidence.
- **Ruling:** Ordinary signers may be outside the Philippines, but participant location alone does not create a Philippine notarization claim.
- **Ruling:** Retention and storage-provider defaults remain configurable because the system needs the policy model now, while provider and final policy choices are separate implementation decisions.

## Verification

- `git diff --check`
- Confirm acceptance items against `docs/jurisdiction-market-matrix.md`.
- Confirm no `apps/mobile` files are modified.
