# VS-FND-003 — Implement Roles, Permissions, and Membership Invitations

**Status:** Done for current system-build scope
**Phase:** Phase 1 — Accounts, organizations, projects, documents, storage, and evidence
**Window:** S06 / 2027-02-01 – 2027-02-12
**Owner:** Backend first, then Web
**Estimate:** 8 days
**Dependencies:** VS-FND-002, VS-P0-006, VS-P0-007, VS-P0-010

## Goal

Define the role and permission vocabulary required by VinciSign and provide a secure organization membership lifecycle: invite, accept, list, change role, revoke, and expire. All authorization decisions must be derived from the authenticated account and server-side organization/workspace membership.

## Current API delivery

- Added organization roles:
  - `ORGANIZATION_OWNER`
  - `ORGANIZATION_ADMINISTRATOR`
  - `ORGANIZATION_MEMBER`
  - `AUDITOR`
  - `API_OPERATOR`
- Added platform role catalog entries:
  - `PLATFORM_OWNER`
  - `COMPLIANCE_ADMINISTRATOR`
- Added project role catalog entries:
  - `SENDER`
  - `SIGNER`
  - `WITNESS`
  - `APPROVER`
  - `VIEWER`
  - `NOTARY`
  - `NOTARY_SUPERVISOR`
  - `GUEST_RECIPIENT`
- Added role-to-permission catalog endpoint at `GET /api/v1/roles`.
- Added organization membership listing, role update, and revocation endpoints.
- Added invitation creation, listing, revocation, and token acceptance endpoints.
- Invitation tokens are returned only once on creation; only a SHA-256 token hash is persisted.
- Invitations expire after seven days, reject expired/revoked/accepted tokens, and require the authenticated account email to match the invited email.
- Organization owners are created automatically and cannot be invited, reassigned, or revoked through these endpoints.
- Organization administrators can manage ordinary invitations and members but cannot change or revoke organization administrators.
- Workspace scope is validated against the invitation’s organization.
- Added Flyway migration `V6__roles_and_membership_invitations.sql`.
- Updated `packages/api-types/openapi.json` with the API paths and schemas.
- Kept the implementation synchronous and local to Spring Boot/PostgreSQL. No mobile, Redis, worker, email provider, or other infrastructure was added.

## API surface

- `GET /api/v1/roles`
- `GET /api/v1/organizations/{organizationId}/memberships`
- `PATCH /api/v1/organizations/{organizationId}/memberships/{membershipId}`
- `DELETE /api/v1/organizations/{organizationId}/memberships/{membershipId}`
- `POST /api/v1/organizations/{organizationId}/invitations`
- `GET /api/v1/organizations/{organizationId}/invitations`
- `DELETE /api/v1/organizations/{organizationId}/invitations/{invitationId}`
- `POST /api/v1/invitations/accept`

## Web delivery

- Added protected `/organizations` management page.
- Added organization creation, organization selection, membership listing, role changes, and membership revocation controls.
- Added invitation creation with one-time token display and copy action.
- Added invitation listing and revocation controls.
- Added protected `/invitations/accept` page supporting a token query parameter or manual token entry.
- Added authenticated Organizations navigation entry.
- Kept business API calls in feature-level TanStack Query hooks using the typed Spring Boot client helper.
- Added loading, empty, error, confirmation, and toast feedback states.

## API acceptance checklist

- [x] Organization role values are persisted and owner memberships are identified as `ORGANIZATION_OWNER`.
- [x] Platform and project role permission mappings are available from the API.
- [x] An authorized owner/administrator can create an invitation.
- [x] An authenticated invitee can accept a matching invitation token.
- [x] Expired invitations are rejected and marked `EXPIRED`.
- [x] Revoked invitations cannot be accepted.
- [x] Duplicate active membership and duplicate pending invitation creation are rejected.
- [x] Owner protection prevents inviting, reassigning, or revoking the organization owner.
- [x] Resource authorization is derived from server-side membership, not client-supplied ownership claims.
- [x] Focused organization integration tests pass: 16 tests.
- [x] Full backend test suite passes: 39 tests.
- [x] Java 21 Spotless check passes.
- [x] OpenAPI JSON parsing passes.
- [x] `git diff --check` passes.
- [x] Web organization membership and invitation management screens are implemented.
- [x] Frontend typecheck passes.
- [x] Frontend production build passes and includes `/organizations` and `/invitations/accept`.
- [x] OpenAPI types regenerate successfully with `openapi-typescript`.

## Manual API validation sequence

1. Create an organization as an authenticated account.
2. Use the returned organization ID to create an invitation.
3. Save the one-time `token` from the invitation response.
4. Authenticate as the invited email and accept the token.
5. List memberships and invitations as the owner/administrator.
6. Change the accepted membership role and revoke it.
7. Create another invitation, revoke it, and verify acceptance returns a conflict.

## Operational follow-up

Manual Postman validation remains useful against the configured local/API environment. It is not required to compile or ship this implementation; the endpoint sequence is documented in the API section of this ticket and the backend integration suite covers the lifecycle behavior.
