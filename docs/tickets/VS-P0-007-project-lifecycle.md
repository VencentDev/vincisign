# VS-P0-007 — Specify Project Lifecycle and Transition Rules

**Status:** Done for current system-build scope  
**Phase:** Phase 0 — Product, legal, and architecture foundation  
**Window:** S03–S04 / 2026-12-14 – 2027-01-08  
**Owner:** Product + Backend  
**Estimate:** 5 days  
**Dependencies:** VS-P0-006

## Goal

Define one controlled Project lifecycle with explicit states, allowed transitions, server-side guards, exceptional-state behavior, concurrency rules, and evidence requirements.

## Deliverable

- [`docs/project-lifecycle-policy.md`](../project-lifecycle-policy.md)

## Acceptance checklist

- [x] Draft through Archived active states are defined.
- [x] Expired, Cancelled, Declined, Rejected, Suspended, and Revoked exceptional states are defined.
- [x] Allowed transitions and trigger conditions are defined.
- [x] Actor, previous/new state, time, version, role, device/session, reason, and trigger evidence fields are defined.
- [x] Completed documents cannot return to draft or accept signatures.
- [x] Notarized versions cannot be edited and notarial records remain locked.
- [x] Cancelled/expired/declined/rejected projects cannot accept new signatures.
- [x] Replacements create new versions and evidence history.
- [x] Concurrency, idempotency, scheduler, provider callback, and webhook behavior are defined.
- [x] Arbitrary client status patching is prohibited.
- [x] No mobile files were changed.

## Notes and rulings

- **Ruling:** Project state is the workflow source of truth; participant, document-version, certificate, notarization, and webhook states remain related state machines.
- **Ruling:** `REVOKED` is retained as a visible project/result state, while certificate status remains the authoritative certificate-level revocation record.
- **Ruling:** Suspension stores the prior eligible state and can resume only after server-side revalidation.
- **Ruling:** Named transition commands are required; clients cannot patch an arbitrary project status.

## Verification

- `git diff --check`
- Confirm acceptance items against `docs/project-lifecycle-policy.md`.
- Confirm terminology agrees with [`docs/domain-vocabulary.md`](../domain-vocabulary.md).
- Confirm no `apps/mobile` files are modified.
