ALTER TABLE organization_memberships
  ADD COLUMN role VARCHAR(40) NOT NULL DEFAULT 'ORGANIZATION_MEMBER';

UPDATE organization_memberships membership
SET role = 'ORGANIZATION_OWNER'
FROM organizations organization
WHERE membership.organization_id = organization.id
  AND membership.user_id = organization.owner_user_id;

ALTER TABLE organization_memberships
  ADD CONSTRAINT organization_memberships_role_check
  CHECK (role IN (
    'ORGANIZATION_OWNER',
    'ORGANIZATION_ADMINISTRATOR',
    'ORGANIZATION_MEMBER',
    'AUDITOR',
    'API_OPERATOR'
  ));

CREATE INDEX idx_organization_memberships_organization_role
  ON organization_memberships(organization_id, role);

CREATE TABLE membership_invitations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  organization_id UUID NOT NULL REFERENCES organizations(id),
  workspace_id UUID REFERENCES workspaces(id),
  email VARCHAR(255) NOT NULL,
  role VARCHAR(40) NOT NULL,
  token_hash VARCHAR(64) NOT NULL UNIQUE,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  expires_at TIMESTAMPTZ NOT NULL,
  invited_by_user_id UUID NOT NULL REFERENCES users(id),
  accepted_by_user_id UUID REFERENCES users(id),
  accepted_at TIMESTAMPTZ,
  revoked_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  CONSTRAINT membership_invitations_role_check
    CHECK (role IN (
      'ORGANIZATION_ADMINISTRATOR',
      'ORGANIZATION_MEMBER',
      'AUDITOR',
      'API_OPERATOR'
    )),
  CONSTRAINT membership_invitations_status_check
    CHECK (status IN ('PENDING', 'ACCEPTED', 'REVOKED', 'EXPIRED')),
  CONSTRAINT membership_invitations_email_not_blank
    CHECK (length(trim(email)) > 0)
);

CREATE INDEX idx_membership_invitations_organization_status
  ON membership_invitations(organization_id, status);
CREATE INDEX idx_membership_invitations_email
  ON membership_invitations(lower(email));
