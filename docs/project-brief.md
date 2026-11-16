# VinciSign Project Brief

The authoritative product charter is [`product-charter.md`](product-charter.md). This brief is the concise orientation document for agents and contributors.

## Vision

Make trustworthy document signing and verification embeddable in any product through a clear, secure API.

## Users

- Businesses sending agreements to customers, employees, vendors, or partners.
- Platforms embedding signing into onboarding, lending, HR, procurement, and workflow products.
- Signers who need a simple, accessible signing experience.
- Auditors and recipients who need to verify a document and its evidence.
- Qualified electronic notaries and accredited facilities, if the regulated product line is launched.

## Product promise

VinciSign preserves the document, its versions, the participants’ actions, the consent record, and the evidence needed to understand how a completed artifact was produced.

## Non-goals for the first release

- Claiming universal legal enforceability.
- Calling ordinary electronic signatures notarization.
- Blockchain anchoring as a prerequisite for document integrity.
- Building every enterprise policy before the core signing flow works.
- Adding Redis, background workers, document-conversion services, malware scanning, notification providers, webhooks, or external synchronization to the initial document flow.

## Initial implementation boundary

The first vertical slice uses the Spring Boot API, PostgreSQL, and Supabase Storage. It accepts ready PDF/PDF-A files, validates them synchronously, stores them, calculates SHA-256, records metadata and evidence, and returns the result in the same request.

## Initial domain vocabulary

The authoritative definitions are in [`domain-vocabulary.md`](domain-vocabulary.md): `Account` → `Organization` → `Workspace` → `Project` → `Document` → `DocumentVersion` → `Participant` → `Field` → `ConsentRecord` → `EvidenceEvent` → `Certificate` → `Verification` → `Passport` → `Vault`.

## Definition of ready for public API work

- Versioned endpoint and OpenAPI schema.
- Organization and permission scope defined.
- Idempotency behavior defined for side-effecting writes.
- Evidence events defined for success and failure paths.
- Error response and retry semantics documented.
- Integration and security tests included.
