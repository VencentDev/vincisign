# VS-P0-011 — Create Threat Model and Privacy Impact Baseline

**Status:** Done for current system-build scope  
**Phase:** Phase 0 — Product, legal, and architecture foundation  
**Window:** S03–S04 / 2026-12-14 – 2027-01-08  
**Owner:** Security + Backend  
**Estimate:** 7 days  
**Dependencies:** VS-P0-002, VS-P0-006, VS-P0-008

## Goal

Define the security boundaries, privacy-sensitive data, attacker scenarios, implementation controls, and unresolved production prerequisites for the simple PDF/PDF-A document flow without adding infrastructure or requiring legal approval to continue system construction.

## Deliverables

- [`docs/security/threat-model-and-privacy-baseline.md`](../security/threat-model-and-privacy-baseline.md)
- Persisted security artifact: `threatmodel.md` in the Codex Security artifact collection for this repository.

## Acceptance checklist

- [x] Current API, web, authentication, PostgreSQL, and planned Supabase Storage boundaries are documented.
- [x] Account, document, fingerprint, evidence, token, request metadata, and future sensitive-data assets are inventoried.
- [x] Account takeover, stolen links/tokens, replay, malicious upload, unauthorized download, admin abuse, forged certificates, deleted evidence, insider access, data-subject requests, and government requests have controls or explicit gaps.
- [x] Cross-organization authorization and private object-storage access are identified as P0 controls.
- [x] PDF/PDF-A size, type, readability, password/encryption, and synchronous-flow limits are captured.
- [x] No malware scanner, Redis worker, conversion service, notification provider, webhook, or external synchronization dependency was added.
- [x] Privacy minimization, access, retention, data-subject request, and government-request baselines are documented without claiming legal approval.
- [x] Security scenarios are labeled hypotheses unless verified by implementation evidence.
- [x] No mobile files were changed.

## Notes and rulings

- **Ruling:** The simple first flow is the security scope: authenticated API request → PDF/PDF-A validation → Supabase Storage → SHA-256 → PostgreSQL metadata/evidence → synchronous response.
- **Ruling:** Organization/project authorization, private storage, opaque keys, immutable versions, append-oriented evidence, and token/log hygiene are required implementation controls.
- **Ruling:** The current repository is still a template. Existing todo ownership checks and OAuth behavior are evidence about the template only; they are not VinciSign project/document authorization.
- **Ruling:** Formal legal/privacy review, ENP/ENF accreditation, retention schedules, government-request procedure, and production incident commitments remain tracked prerequisites rather than approval gates for the current build.
- **Ruling:** Supabase server credentials and storage-bucket settings will be introduced with the storage implementation ticket; no secrets are committed in this baseline.

## Verification

- `git diff --check`
- Confirm threat-model citations against the current repository source.
- Confirm the persisted `threatmodel.md` artifact matches the repository document content.
- Confirm no `apps/mobile` files are modified.
