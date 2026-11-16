# VS-P0-012 — Make Repository Delivery Checks Product-Aware

**Status:** Done for current system-build scope  
**Phase:** Phase 0 — Product, legal, and architecture foundation  
**Window:** S04 / 2026-12-28 – 2027-01-08  
**Owner:** Platform + QA  
**Estimate:** 3 days  
**Dependencies:** VS-P0-001

## Goal

Make the repository’s delivery checks enforce VinciSign’s current web/API/document scope while keeping mobile out of the delivery path and avoiding new infrastructure or external services.

## Deliverables

- Updated [CI workflow](../../.github/workflows/ci.yml).
- Product-aware root scripts in [`package.json`](../../package.json).
- Reproducible API-type generation check in [`packages/api-types/package.json`](../../packages/api-types/package.json).

## Acceptance checklist

- [x] Backend CI checks Spotless formatting.
- [x] Backend CI runs Maven verification with the existing migration-backed Testcontainers tests.
- [x] Frontend CI installs dependencies, generates API types, lints, typechecks, and runs the configured frontend test command.
- [x] CI checks product documentation, workflow YAML, frontend source, API-type source, and package configuration formatting.
- [x] CI regenerates API types from the tracked OpenAPI fallback and fails if the generated declaration is missing.
- [x] Mobile CI is removed from the product delivery workflow and mobile files remain untouched.
- [x] Image publishing remains gated behind backend, frontend, and repository checks on pushes to `main`.
- [x] No Redis, ClamAV, worker, conversion, notification, webhook, or other external runtime dependency was added.

## Notes and rulings

- **Ruling:** The current CI contract covers web, backend, API types, product docs, migrations, formatting, and tests only.
- **Ruling:** `packages/api-types/openapi.json` is the tracked fallback contract used for deterministic type generation until the backend exposes a running contract-generation step.
- **Ruling:** Existing backend Testcontainers remain the only container-backed test dependency; no new runtime infrastructure was introduced.
- **Ruling:** Mobile remains outside the current delivery program. Removing its CI job does not modify `apps/mobile`.

## Verification

- `git diff --check`
- Parse edited JSON files with Node.
- Validate the workflow structure with the repository’s available YAML tooling or CI itself.
- Confirm no `apps/mobile` files are modified.
- Local Prettier execution is pending dependency installation if `node_modules` is absent.
