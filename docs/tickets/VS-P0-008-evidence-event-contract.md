# VS-P0-008 — Define Evidence Event Contract

**Status:** Done for current system-build scope  
**Phase:** Phase 0 — Product, legal, and architecture foundation  
**Window:** S03–S04 / 2026-12-14 – 2027-01-08  
**Owner:** Backend + Security  
**Estimate:** 5 days  
**Dependencies:** VS-P0-006, VS-P0-007

## Goal

Define the append-oriented EvidenceEvent contract used to explain, verify, export, and operate VinciSign project history across ordinary signing, verification, electronic notarization, billing, integrations, and administration.

## Deliverable

- [`docs/evidence-event-contract.md`](../evidence-event-contract.md)

## Acceptance checklist

- [x] Event envelope and required fields are defined.
- [x] Actor and source types are defined.
- [x] Event taxonomy covers account, organization, project, document, participant, consent, signature, certificate, verification, notarization, Vault, administration, API, webhook, and credit actions.
- [x] Document version, policy version, actor, role, device/session, request, correlation, and reason references are defined.
- [x] Ordering, sequence, event hashing, chain integrity, duplicate, idempotency, and out-of-order callback rules are defined.
- [x] Append-only, correction, restricted access, export, and operational requirements are defined.
- [x] Events are explicitly separated from mutable resource status.
- [x] No mobile files were changed.

## Notes and rulings

- **Ruling:** EvidenceEvent is the product evidence record; infrastructure logs remain complementary operational records and cannot replace it.
- **Ruling:** Business event IDs remain stable across webhook delivery retries; delivery attempts are separate records.
- **Ruling:** Corrections and reversals create new events rather than editing historical events.
- **Ruling:** Sensitive identity, location, video, payment, and document content use restricted references where possible instead of unrestricted event payload duplication.

## Verification

- `git diff --check`
- Confirm acceptance items against `docs/evidence-event-contract.md`.
- Confirm lifecycle transition events align with [`docs/project-lifecycle-policy.md`](../project-lifecycle-policy.md).
- Confirm no `apps/mobile` files are modified.
