CREATE TABLE organizations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(160) NOT NULL,
  jurisdiction_profile VARCHAR(16) NOT NULL DEFAULT 'PH',
  owner_user_id UUID NOT NULL REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  CONSTRAINT organizations_name_not_blank CHECK (length(trim(name)) > 0),
  CONSTRAINT organizations_jurisdiction_profile_check
    CHECK (jurisdiction_profile = 'PH')
);

CREATE INDEX idx_organizations_owner_user_id ON organizations(owner_user_id);

CREATE TABLE workspaces (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  organization_id UUID NOT NULL REFERENCES organizations(id),
  name VARCHAR(160) NOT NULL,
  default_workspace BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  CONSTRAINT workspaces_organization_name_not_blank CHECK (length(trim(name)) > 0)
);

CREATE UNIQUE INDEX workspaces_organization_name_unique
  ON workspaces(organization_id, lower(name));

CREATE UNIQUE INDEX workspaces_one_default_per_organization
  ON workspaces(organization_id)
  WHERE default_workspace = TRUE;

CREATE INDEX idx_workspaces_organization_id ON workspaces(organization_id);

CREATE TABLE organization_memberships (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  organization_id UUID NOT NULL REFERENCES organizations(id),
  user_id UUID NOT NULL REFERENCES users(id),
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  CONSTRAINT organization_memberships_status_check
    CHECK (status IN ('ACTIVE', 'INVITED', 'SUSPENDED', 'REVOKED')),
  CONSTRAINT organization_memberships_organization_user_unique
    UNIQUE (organization_id, user_id)
);

CREATE INDEX idx_organization_memberships_user_id
  ON organization_memberships(user_id);
CREATE INDEX idx_organization_memberships_organization_id
  ON organization_memberships(organization_id);

CREATE TABLE workspace_memberships (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id UUID NOT NULL REFERENCES workspaces(id),
  user_id UUID NOT NULL REFERENCES users(id),
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  CONSTRAINT workspace_memberships_status_check
    CHECK (status IN ('ACTIVE', 'INVITED', 'SUSPENDED', 'REVOKED')),
  CONSTRAINT workspace_memberships_workspace_user_unique
    UNIQUE (workspace_id, user_id)
);

CREATE INDEX idx_workspace_memberships_user_id
  ON workspace_memberships(user_id);
CREATE INDEX idx_workspace_memberships_workspace_id
  ON workspace_memberships(workspace_id);
