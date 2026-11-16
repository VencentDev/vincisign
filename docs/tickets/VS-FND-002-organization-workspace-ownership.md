# VS-FND-002 — Implement Organization and Workspace Ownership

**Status:** Done for current system-build scope  
**Phase:** Phase 1 — Accounts, organizations, projects, documents, storage, and evidence  
**Window:** S05–S06 / 2027-01-25 – 2027-02-05  
**Owner:** Backend  
**Estimate:** 8 days  
**Dependencies:** VS-FND-001, VS-P0-006, VS-P0-007, VS-P0-010

## Goal

Create the organization and workspace ownership boundary that all later VinciSign resources will use. An authenticated account must be able to create an organization with a default workspace, view only organizations and workspaces it can access, and manage the initial active membership flow without allowing cross-organization access.

## Delivered

- Added PostgreSQL tables and constraints for:
  - `organizations`
  - `workspaces`
  - `organization_memberships`
  - `workspace_memberships`
- Added Flyway migration `V5__organization_workspace_membership.sql` with:
  - UUID primary keys and foreign keys to local accounts.
  - Philippines (`PH`) as the initial jurisdiction profile.
  - Nonblank names and case-insensitive workspace-name uniqueness per organization.
  - One default workspace per organization.
  - Active, invited, suspended, and revoked membership states.
- Added JPA entities and repositories for organization, workspace, and membership persistence.
- Added authenticated organization/workspace API endpoints:
  - `POST /api/v1/organizations`
  - `GET /api/v1/organizations`
  - `GET /api/v1/organizations/{organizationId}`
  - `POST /api/v1/organizations/{organizationId}/memberships`
  - `POST /api/v1/organizations/{organizationId}/workspaces`
  - `GET /api/v1/organizations/{organizationId}/workspaces`
  - `GET /api/v1/organizations/{organizationId}/workspaces/{workspaceId}`
  - `POST /api/v1/organizations/{organizationId}/workspaces/{workspaceId}/memberships`
- Creating an organization automatically creates its default workspace and active owner memberships at both scopes.
- Added owner-managed active membership flow for existing accounts, including default-workspace access for newly added organization members.
- Enforced server-derived ownership and membership checks; organization and workspace identifiers from a request cannot grant access by themselves.
- Returned inaccessible organization/workspace resources as not found to avoid cross-tenant resource disclosure.
- Added OpenAPI paths and schemas for the new API surface.
- Kept account roles, invitation workflows, revocation workflows, and project-level roles in VS-FND-003; this ticket uses ownership plus membership status only.
- Kept the implementation synchronous and local to Spring Boot/PostgreSQL; no mobile, Redis, worker, malware-scanning, or external-service work was added.

## Acceptance checklist

- [x] An authenticated account can create an organization.
- [x] New organizations default to the approved Philippines jurisdiction profile (`PH`).
- [x] Creating an organization creates exactly one default workspace.
- [x] The creating account is an active owner at organization and default-workspace scope.
- [x] An organization owner can create additional uniquely named workspaces.
- [x] An organization owner can add an existing account to the organization and default workspace.
- [x] An organization owner can grant an active organization member access to another workspace.
- [x] Non-members cannot read or list an organization’s workspaces.
- [x] Non-owners cannot add organization or workspace memberships.
- [x] Workspace identifiers from another organization cannot be used to bypass organization scoping.
- [x] Duplicate workspace names are rejected within one organization without affecting other organizations.
- [x] Membership persistence has database-level uniqueness and status constraints.
- [x] The OpenAPI contract includes the organization/workspace endpoints and response schemas.
- [x] Existing backend tests remain green.
- [x] No `apps/mobile` files were changed.

## Scope ruling for the next ticket

The current baseline uses direct owner-managed activation for an already existing account. It does not expose open self-join by organization UUID, email invitation delivery, membership role assignment, or membership revocation. Those workflows belong to VS-FND-003, where the organization roles and permission matrix will be implemented explicitly.

## Verification

- `OrganizationControllerIntegrationTest`: 10 tests passed.
- Full backend suite: 33 tests passed, 0 failures, 0 errors.
- Flyway applied migrations V1 through V5 successfully in Testcontainers PostgreSQL 16.
- Java 21 Spotless check passed.
- OpenAPI JSON parsing passed.
- `git diff --check` passed.
- The repository contract script could not regenerate TypeScript because this checkout has no installed JavaScript dependencies (`openapi-typescript` is unavailable); the updated OpenAPI source remains valid JSON and is ready for the normal dependency-installed CI check.
