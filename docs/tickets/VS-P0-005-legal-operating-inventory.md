# VS-P0-005 — Draft Legal and Operating Document Inventory

**Status:** Done for current system-build scope  
**Phase:** Phase 0 — Product, legal, and architecture foundation  
**Window:** S02–S04 / 2026-11-30 – 2027-01-08  
**Owner:** Product  
**Estimate:** 8 days  
**Dependencies:** VS-P0-002, VS-P0-003, VS-P0-004

## Goal

Identify every legal, consent, policy, compliance, and operating document VinciSign will need to draft, version, present, store, export, or use for investigations and regulated workflows.

## Deliverable

- [`docs/legal-operating-document-inventory.md`](../legal-operating-document-inventory.md)

## Acceptance checklist

- [x] Terms of service, privacy, electronic-record consent, electronic-signature consent, notarization terms, acceptable use, retention, refunds/credits, enterprise, DPA, ENP, ENF, incident, revocation, and legal-request documents are listed.
- [x] ENP/ENF, IEN, REN, Electronic Notarial Book, data-subject request, legal-hold, provider, access-control, disaster-recovery, support, and release documents are listed.
- [x] Every item has a purpose, owner, status, and release gate.
- [x] Status vocabulary distinguishes engineering drafts from approved legal text.
- [x] Policy-version and historical-record requirements are defined.
- [x] System integration points are mapped to onboarding, signing, verification, Vault, API/credits, and notarization.
- [x] Formal legal/compliance work is explicitly deferred from the current system-build scope.
- [x] No mobile files were changed.

## Notes and rulings

- **Ruling:** This ticket creates an implementation inventory, not final legal agreements or legal advice.
- **Ruling:** Customer-facing consent and policy content can be represented as versioned engineering drafts during system construction, but production release gates must distinguish drafts from approved text.
- **Ruling:** Electronic-notarization documents remain in the inventory as required first-class capabilities, with accreditation and counsel review recorded as release prerequisites rather than omitted.
- **Ruling:** Historical policy versions are part of the data model so later wording changes cannot rewrite past consent or project decisions.

## Verification

- `git diff --check`
- Confirm acceptance items against `docs/legal-operating-document-inventory.md`.
- Confirm no `apps/mobile` files are modified.
