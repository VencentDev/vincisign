# VS-P0-001 — Establish VinciSign Product Charter

**Status:** Done for current system-build scope  
**Phase:** Phase 0 — Product, legal, and architecture foundation  
**Window:** S01 / 2026-11-16 – 2026-11-27  
**Owner:** Product  
**Estimate:** 3 days  
**Dependencies:** None

## Objective

Create the authoritative product charter for VinciSign so product, engineering, legal/compliance, security, operations, and future agents share the same purpose, users, scope, non-goals, product boundaries, and proposed success measures.

## Deliverables

- [`docs/product-charter.md`](../product-charter.md)
- Product identity and scope reference in [`AGENTS.md`](../../AGENTS.md)
- Roadmap reference in [`docs/vincisign-gantt-timeline.md`](../vincisign-gantt-timeline.md)

## Acceptance checklist

- [x] Product purpose and API-first boundary are explicit.
- [x] Vision and product promise are explicit.
- [x] Target users and their jobs are listed.
- [x] Sign, Protect, Verify, Passport, and Vault are defined.
- [x] Core signing, verification, and regulated-notarization journeys are described.
- [x] In-scope capabilities and first-release exclusions are listed.
- [x] Anti-goals identify unacceptable product behavior.
- [x] Proposed success measures cover customer outcomes, trust, reliability, and compliance.
- [x] G0 decisions requiring product/legal approval are listed.
- [x] Product-owner direction recorded: proceed with system construction.
- [x] Formal legal/compliance review explicitly deferred from the current system-build scope.

## Notes and rulings

- **Ruling:** Use the existing `docs/project-brief.md` as a concise orientation document and create a separate charter for the authoritative product decision record. This keeps agent onboarding short while giving the first ticket a durable approval artifact.
- **Ruling:** Treat success metrics as proposed baselines pending G0 review because the repository has no validated customer, traffic, legal, or reliability baseline yet.
- **Ruling:** Keep electronic notarization in the charter as a gated capability rather than removing it, because the source plan treats it as a major product line but explicitly requires accreditation and legal separation.
- **Ruling:** Mark this ticket complete without formal legal/compliance approval because the user’s current objective is to build the system. Legal/compliance work remains documented as a future production-readiness constraint, not a blocker for implementation.

## Verification

- `git diff --check`
- Confirm all acceptance checklist items are represented in `docs/product-charter.md`.
- Confirm no `apps/mobile` files are modified.
