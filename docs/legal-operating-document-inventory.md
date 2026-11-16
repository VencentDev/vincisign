# VinciSign Legal and Operating Document Inventory

**Inventory ID:** `LEGAL-OPS-INVENTORY-0.1`  
**Status:** System-build inventory complete; formal legal review deferred  
**Confirmed:** 2026-11-14  
**Initial market:** Philippines  
**Scope:** Implementation dependencies and release gates; not legal advice or approved legal text

## 1. Purpose

This inventory identifies the legal, consent, policy, compliance, and operational documents VinciSign must eventually draft, approve, version, present, store, export, or use during investigations. It prevents the system from hiding policy requirements inside UI copy and gives each document a product owner, engineering dependency, and release gate.

The inventory is not a substitute for qualified counsel, an accredited ENF process, an ENP agreement, or a completed compliance program. For the current system-build scope, it defines the records and integration points the system must support while formal review remains deferred.

## 2. Status vocabulary

| Status | Meaning |
|---|---|
| `inventory` | Required or potentially required; purpose and owner identified |
| `engineering-draft` | Product/system placeholder can be implemented, but wording is not approved legal text |
| `counsel-review` | Draft requires qualified legal/compliance review |
| `approved` | Authorized for the applicable jurisdiction, workflow, and release |
| `not-applicable` | Explicitly ruled out for the current product scope with a recorded reason |
| `superseded` | Replaced by a newer version; historical versions remain available |
| `blocked` | Cannot be finalized until a jurisdiction, provider, accreditation, or operating decision is made |

The current system-build baseline may use `engineering-draft` records and policy-version references. Production release must not represent an engineering draft as approved legal text.

## 3. Document inventory

| ID | Document | Purpose | Owner | Current status | Required gate |
|---|---|---|---|---|---|
| LEG-001 | Terms of Service | Governs account, organization, project, platform, API, storage, certificates, suspension, termination, and liability relationship | Product + Counsel | `inventory` | Before external production use |
| LEG-002 | Privacy Notice | Explains personal-data categories, purposes, providers, sharing, retention, rights, and contact process | Privacy/Compliance | `inventory` | Before collecting production personal data |
| LEG-003 | Electronic-Record Consent | Records consent to receive/access/use electronic records | Product + Counsel | `engineering-draft` | Before signing workflow |
| LEG-004 | Electronic-Signature Consent | Records intent and consent for an electronic signature or approval | Product + Counsel | `engineering-draft` | Before signature application |
| LEG-005 | Notarization Terms and Disclosure | Separates ordinary signing from electronic notarization and explains ENP/ENF responsibilities, IEN/REN, records, and limitations | Notary Operations + Counsel | `blocked` | Before notarization production operation |
| LEG-006 | Acceptable-Use Policy | Defines prohibited content, misuse, abuse, unlawful activity, upload restrictions, and enforcement | Product + Security + Counsel | `engineering-draft` | Before external upload access |
| LEG-007 | Data-Retention and Deletion Policy | Defines default retention, deletion requests, legal holds, exceptions, evidence preservation, and deletion proof | Operations + Privacy/Compliance | `inventory` | Before production retention configuration |
| LEG-008 | Refund and Credit Policy | Defines plans, usage credits, reservations, consumption, release, refunds, expiration, disputes, and payment failure | Product + Finance + Counsel | `inventory` | Before credits or billing release |
| LEG-009 | Enterprise Agreement | Defines organization access, SLA, support, security commitments, data processing, service limits, and custom terms | Product + Sales + Counsel | `inventory` | Before enterprise contracts |
| LEG-010 | Data-Processing Agreement | Defines controller/processor roles, subprocessors, security, assistance, transfer, breach notice, deletion, and audit obligations | Privacy/Compliance + Counsel | `inventory` | Before applicable enterprise processing |
| LEG-011 | Notary/ENP Agreement | Defines ENP responsibilities, commission evidence, fees, session duties, recordkeeping, confidentiality, suspension, and termination | Notary Operations + Counsel | `blocked` | Before ENP onboarding |
| LEG-012 | ENF Partner Agreement | Defines accredited-facility responsibilities, system boundary, identity/location/video controls, records, synchronization, incidents, and liability | Notary Operations + Counsel | `blocked` | Before partner-operated notarization |
| LEG-013 | Incident-Response Procedure | Defines detection, triage, containment, evidence preservation, notification, recovery, and post-incident review | Security + Operations | `engineering-draft` | Before production launch |
| LEG-014 | Certificate-Revocation Procedure | Defines valid/suspended/revoked/superseded/investigation states, authority, notice, evidence, and recovery | Product + Security + Compliance | `engineering-draft` | Before certificates are externally relied upon |
| LEG-015 | Legal-Request and Subpoena Procedure | Defines request authentication, preservation, scope, response authority, disclosure, and evidence trail | Operations + Counsel | `inventory` | Before production operation |
| LEG-016 | Account and Organization Verification Procedure | Defines organization review, identity levels, escalation, manual review, suspension, and re-verification | Compliance + Operations | `inventory` | Before organization/API-client production access |
| LEG-017 | Data-Subject Request Procedure | Defines access, correction, deletion, objection, restriction, identity verification, exceptions, and response evidence | Privacy/Compliance | `inventory` | Before production personal-data operation |
| LEG-018 | Legal-Hold Procedure | Defines hold authority, scope, notification, release, retention override, and audit record | Compliance + Operations | `inventory` | Before deletion/retention production operation |
| LEG-019 | ENF Accreditation Evidence Register | Tracks facility description, security controls, applications, approvals, renewals, and evidence expiry | Notary Operations | `blocked` | Before enabling accredited ENF operation |
| LEG-020 | ENP Commission Evidence Register | Tracks identity, professional evidence, commission, validity, affiliation, suspension, revocation, and recheck dates | Notary Operations | `blocked` | Before activating an ENP |
| LEG-021 | Electronic Notarial Book Procedure | Defines active/duplicate books, entry creation, inspection/certified-copy requests, corrections, retention, and authorized access | Notary Operations + Counsel | `blocked` | Before notarial act completion |
| LEG-022 | IEN Operating Procedure | Defines in-person appearance, identity, capacity, willingness, witnesses, signing, seal, certificate, and record sequence | Notary Operations + Counsel | `blocked` | Before IEN production operation |
| LEG-023 | REN Operating Procedure | Defines remote location, MFA, liveness, anti-spoofing, live sight-and-sound, interruptions, qualifying locations, signing, seal, and records | Notary Operations + Counsel | `blocked` | Before REN production operation |
| LEG-024 | Security and Access-Control Policy | Defines least privilege, administrator access, API credentials, signing links, MFA, logging, secrets, and review cadence | Security + Operations | `engineering-draft` | Before production launch |
| LEG-025 | Business-Continuity and Disaster-Recovery Procedure | Defines RPO/RTO, backups, restore, storage failure, provider outage, failover, evidence integrity, and exercises | Platform + Operations | `engineering-draft` | Before production launch |
| LEG-026 | Subprocessor and Provider Register | Tracks storage, processing, email, identity, video, payment, scanning, hosting, region, purpose, contract, and status | Privacy/Compliance + Platform | `inventory` | Before applicable production provider use |
| LEG-027 | Support and Escalation Policy | Defines customer support scope, severity, response targets, security escalation, notary escalation, and evidence requests | Operations + Product | `inventory` | Before pilot support |
| LEG-028 | Change and Release-Management Procedure | Defines approval, feature flags, migrations, policy-version changes, rollback, audit, and emergency changes | Engineering + Operations | `engineering-draft` | Before production launch |

## 4. Required policy-version record

Every user-facing consent, policy, disclosure, or agreement that affects a project should have:

- Stable document/policy ID.
- Version identifier.
- Jurisdiction profile.
- Workflow applicability: ordinary signing, verification, vault, IEN, REN, API, credits, or administration.
- Locale/language.
- Status and effective time.
- Superseded version reference.
- Approval/status metadata.
- Rendered content or immutable content hash.
- Presenter and acceptance requirements.
- Retention classification.

The project stores the policy versions shown or accepted at the relevant time. A later version cannot silently rewrite the historical consent record.

## 5. System integration points

### Account and organization onboarding

The system must be able to present and record:

- Terms of Service version.
- Privacy Notice version.
- Electronic-record consent version when required.
- Organization terms and owner acceptance.
- Data-processing role/configuration where applicable.
- Organization verification procedure result.

### Ordinary signing

The system must be able to present and record:

- Electronic-record consent.
- Electronic-signature consent.
- Project disclosure acknowledgement.
- Identity/security disclosure.
- Applicable acceptable-use and privacy versions.
- Sender confirmation that the document, participants, order, and settings are ready.

### Verification and certificates

The system must be able to expose, according to access policy:

- Certificate scope and disclaimer.
- Certificate status and revocation history.
- Verification explanation and version.
- Evidence-export manifest version.
- Public verification content version.

### Electronic notarization

The system must be able to present and record:

- Notarization terms and disclosure.
- IEN/REN operating disclosures.
- Identity, location, liveness, and session notices.
- ENP/ENF agreement references.
- Notarial certificate template/version.
- Electronic Notarial Book procedure/version.
- Official synchronization status and evidence.

### Credits and API integration

The system must be able to expose or record:

- Refund/credit policy version.
- Plan and usage terms.
- API client agreement or organization authorization.
- Rate-limit and acceptable-use version.
- Webhook processing and retry contract version.

## 6. Release gates

| Gate | Documents that must be at least engineering-draft | Documents that need approved status before the gate |
|---|---|---|
| Foundation alpha | Terms placeholder, privacy placeholder, electronic-record consent, acceptable-use, access-control, incident draft | None for internal synthetic data |
| Digital signing beta | Terms, privacy, electronic-record consent, electronic-signature consent, acceptable-use, retention/deletion, incident, revocation | Customer-facing consent and disclosure text for pilot users |
| Verify and Passport release | Certificate, verification, revocation, evidence-export, privacy, retention | Certificate scope/disclaimer and public verification wording |
| Vault and enterprise beta | Retention, legal hold, deletion, DPA, subprocessor, DR, support, enterprise policy | Enterprise customer terms where external contracts are signed |
| Integration platform release | API client terms, credits/refund, plan/usage, webhook, subprocessor, incident | Billing/refund terms and API customer-facing terms |
| Electronic-notarization readiness | Notarization terms, ENP/ENF agreement, IEN, REN, ENB, commission registers, incident, revocation | Required operational and accreditation documents before production notarial acts |
| Enterprise production readiness | Full security, privacy, retention, legal request, DSR, legal hold, DR, release, support set | All documents applicable to enabled features and jurisdictions |

## 7. Engineering backlog created by this inventory

- Versioned policy/document table and content reference.
- Policy applicability by jurisdiction, workflow, organization, project, and participant role.
- Consent presentation and acceptance record.
- Agreement acceptance at account and organization onboarding.
- Policy supersession and historical retrieval.
- Access-controlled policy export for audits and disputes.
- Certificate, verification, revocation, and evidence disclaimer references.
- Notarization terms, ENP/ENF, IEN/REN, and ENB document references.
- Document status and approval checks for release gates.
- Admin workflow for reason-required policy changes and emergency notices.

## 8. Ownership and next actions

| Owner | Next action |
|---|---|
| Product | Confirm product copy, user journeys, and customer-facing scope |
| Engineering | Implement policy-version and consent-reference primitives |
| Security | Draft access-control, incident, and provider-control requirements |
| Operations | Draft retention, deletion, legal-hold, support, and recovery procedures |
| Finance | Draft credit, refund, plan, and reconciliation requirements |
| Privacy/Compliance | Classify personal data and provider/subprocessor obligations |
| Notary Operations | Define ENP/ENF, IEN, REN, certificate, and ENB operating records |
| Counsel | Review applicable documents before external production use |

## 9. Explicitly deferred from current system-build scope

- Formal counsel approval of legal text.
- Formal privacy/compliance certification.
- ENF accreditation submission or approval.
- ENP commission verification for a real person.
- Binding enterprise, DPA, ENP, or ENF contract execution.
- Jurisdiction-specific legal conclusions beyond the Philippines-first product configuration.

These items are deferred, not forgotten. The system must retain the version/reference fields and release gates needed to add them without redesigning the domain model.

## 10. Inventory acceptance

This inventory is complete for the current system-build scope when:

- All source-plan legal, consent, compliance, and operating documents are listed.
- Each item has a purpose, owner, status, and release gate.
- Status values distinguish inventory, engineering draft, counsel review, approved, not applicable, superseded, and blocked.
- Policy-version and historical-record requirements are defined.
- Onboarding, ordinary signing, verification, vault, API/credits, and notarization integration points are listed.
- Formal legal/compliance work is explicitly deferred rather than misrepresented as complete.
