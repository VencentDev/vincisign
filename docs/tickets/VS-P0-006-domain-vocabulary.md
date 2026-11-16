# VS-P0-006 — Define Core Domain Vocabulary and Ownership

**Status:** Done for current system-build scope  
**Phase:** Phase 0 — Product, legal, and architecture foundation  
**Window:** S03 / 2026-12-14 – 2026-12-25  
**Owner:** Product + Backend  
**Estimate:** 4 days  
**Dependencies:** VS-P0-001

## Goal

Create one authoritative vocabulary and ownership map for VinciSign so product copy, API contracts, database entities, events, first-party clients, and future agents use consistent concepts and authorization boundaries.

## Deliverable

- [`docs/domain-vocabulary.md`](../domain-vocabulary.md)

## Acceptance checklist

- [x] Account, Organization, Workspace, Membership, Role, Project, Document, DocumentVersion, Participant, Field, ConsentRecord, EvidenceEvent, Certificate, Verification, Passport, Vault, APIClient, WebhookEndpoint, CreditAccount, CreditLedgerEntry, PolicyVersion, IdentityEvidence, ENP, ENF, IEN, REN, and ENB are defined.
- [x] Domain hierarchy and ownership relationships are documented.
- [x] Customer owner, system owner/module, mutability, and authorization scope are mapped.
- [x] Project and document-version lifecycle vocabulary is defined.
- [x] Ordinary signing, verification, certificates, credits, API clients, and notarization records are separated.
- [x] Evidence events are distinguished from mutable status fields.
- [x] API naming examples follow organization/workspace/resource boundaries.
- [x] Anti-confusion rules prevent common template/domain mistakes.
- [x] No mobile files were changed.

## Notes and rulings

- **Ruling:** Organization is the default customer ownership boundary; workspace and project scope narrow access but do not replace organization authorization.
- **Ruling:** Document is the logical identity and DocumentVersion contains bytes/metadata; this prevents file replacement from mutating signing history.
- **Ruling:** ConsentRecord, IdentityEvidence, EvidenceEvent, and Certificate remain separate because they answer different trust questions.
- **Ruling:** APIClient and CreditAccount/CreditLedgerEntry remain separate; API credentials are never usage credits.

## Verification

- `git diff --check`
- Confirm acceptance items against `docs/domain-vocabulary.md`.
- Confirm no `apps/mobile` files are modified.
