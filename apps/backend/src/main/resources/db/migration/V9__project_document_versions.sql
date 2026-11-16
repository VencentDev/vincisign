ALTER TABLE workspaces
  ADD CONSTRAINT workspaces_id_organization_unique UNIQUE (id, organization_id);

CREATE TABLE projects (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  organization_id UUID NOT NULL REFERENCES organizations(id),
  workspace_id UUID NOT NULL,
  owner_user_id UUID NOT NULL REFERENCES users(id),
  name VARCHAR(200) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  policy_id VARCHAR(120) NOT NULL DEFAULT 'DOC-POLICY',
  policy_version VARCHAR(64) NOT NULL DEFAULT '0.2',
  aggregate_version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  CONSTRAINT projects_id_organization_unique UNIQUE (id, organization_id),
  CONSTRAINT projects_workspace_organization_fk
    FOREIGN KEY (workspace_id, organization_id)
    REFERENCES workspaces(id, organization_id),
  CONSTRAINT projects_name_not_blank CHECK (length(trim(name)) > 0),
  CONSTRAINT projects_policy_id_not_blank CHECK (length(trim(policy_id)) > 0),
  CONSTRAINT projects_policy_version_not_blank CHECK (length(trim(policy_version)) > 0),
  CONSTRAINT projects_status_check CHECK (status IN (
    'DRAFT',
    'DOCUMENT_UPLOADED',
    'DOCUMENT_PREPARED',
    'PARTICIPANTS_ADDED',
    'READY_TO_SEND',
    'SENT',
    'VIEWED',
    'PARTIALLY_SIGNED',
    'AWAITING_APPROVAL',
    'AWAITING_NOTARIZATION',
    'NOTARIZATION_IN_PROGRESS',
    'COMPLETED',
    'ARCHIVED',
    'EXPIRED',
    'CANCELLED',
    'DECLINED',
    'REJECTED',
    'SUSPENDED',
    'REVOKED'
  ))
);

CREATE INDEX idx_projects_organization_created
  ON projects(organization_id, created_at DESC);
CREATE INDEX idx_projects_workspace_created
  ON projects(workspace_id, created_at DESC);
CREATE INDEX idx_projects_owner_user_id
  ON projects(owner_user_id);
CREATE INDEX idx_projects_organization_status
  ON projects(organization_id, status);

CREATE TABLE documents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  project_id UUID NOT NULL REFERENCES projects(id),
  organization_id UUID NOT NULL REFERENCES organizations(id),
  original_version_id UUID,
  working_version_id UUID,
  name VARCHAR(200) NOT NULL,
  original_file_name VARCHAR(255) NOT NULL,
  original_media_type VARCHAR(127) NOT NULL,
  created_by_user_id UUID NOT NULL REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  CONSTRAINT documents_id_organization_unique UNIQUE (id, organization_id),
  CONSTRAINT documents_project_organization_fk
    FOREIGN KEY (project_id, organization_id) REFERENCES projects(id, organization_id),
  CONSTRAINT documents_name_not_blank CHECK (length(trim(name)) > 0),
  CONSTRAINT documents_original_file_name_not_blank
    CHECK (length(trim(original_file_name)) > 0),
  CONSTRAINT documents_original_media_type_not_blank
    CHECK (length(trim(original_media_type)) > 0)
);

CREATE INDEX idx_documents_project_created
  ON documents(project_id, created_at ASC);
CREATE INDEX idx_documents_organization_created
  ON documents(organization_id, created_at ASC);
CREATE INDEX idx_documents_working_version_id
  ON documents(working_version_id);

CREATE TABLE document_versions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  document_id UUID NOT NULL REFERENCES documents(id),
  organization_id UUID NOT NULL REFERENCES organizations(id),
  predecessor_version_id UUID REFERENCES document_versions(id),
  version_number INTEGER NOT NULL,
  version_role VARCHAR(32) NOT NULL DEFAULT 'ORIGINAL',
  status VARCHAR(32) NOT NULL DEFAULT 'RECEIVED',
  immutable_version BOOLEAN NOT NULL DEFAULT FALSE,
  original_file_name VARCHAR(255) NOT NULL,
  media_type VARCHAR(127) NOT NULL,
  size_bytes BIGINT NOT NULL,
  storage_bucket VARCHAR(120),
  storage_object_key VARCHAR(1024),
  fingerprint_algorithm VARCHAR(32) NOT NULL DEFAULT 'SHA-256',
  fingerprint VARCHAR(64),
  locked_at TIMESTAMPTZ,
  created_by_user_id UUID NOT NULL REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  CONSTRAINT document_versions_id_organization_unique UNIQUE (id, organization_id),
  CONSTRAINT document_versions_document_organization_fk
    FOREIGN KEY (document_id, organization_id) REFERENCES documents(id, organization_id),
  CONSTRAINT document_versions_predecessor_organization_fk
    FOREIGN KEY (predecessor_version_id, organization_id)
    REFERENCES document_versions(id, organization_id),
  CONSTRAINT document_versions_document_number_unique
    UNIQUE (document_id, version_number),
  CONSTRAINT document_versions_version_number_positive CHECK (version_number > 0),
  CONSTRAINT document_versions_role_check CHECK (version_role IN (
    'ORIGINAL',
    'WORKING',
    'SIGNED_FINAL',
    'NOTARIZED_FINAL',
    'ARCHIVED'
  )),
  CONSTRAINT document_versions_role_status_check CHECK (
    (status IN (
      'RECEIVED', 'VALIDATING', 'SCAN_PENDING', 'NORMALIZING', 'READY',
      'QUARANTINED', 'REJECTED', 'FAILED', 'LOCKED_FOR_SEND'
    ) AND version_role IN ('ORIGINAL', 'WORKING'))
    OR (status = 'SIGNED_FINAL' AND version_role = 'SIGNED_FINAL')
    OR (status = 'NOTARIZED_FINAL' AND version_role = 'NOTARIZED_FINAL')
    OR (status = 'ARCHIVED' AND version_role = 'ARCHIVED')
  ),
  CONSTRAINT document_versions_file_name_not_blank
    CHECK (length(trim(original_file_name)) > 0),
  CONSTRAINT document_versions_media_type_not_blank
    CHECK (length(trim(media_type)) > 0),
  CONSTRAINT document_versions_size_non_negative CHECK (size_bytes >= 0),
  CONSTRAINT document_versions_status_check CHECK (status IN (
    'RECEIVED',
    'VALIDATING',
    'SCAN_PENDING',
    'NORMALIZING',
    'READY',
    'QUARANTINED',
    'SIGNED_FINAL',
    'NOTARIZED_FINAL',
    'ARCHIVED',
    'REJECTED',
    'FAILED',
    'LOCKED_FOR_SEND'
  )),
  CONSTRAINT document_versions_fingerprint_algorithm_check
    CHECK (fingerprint_algorithm = 'SHA-256'),
  CONSTRAINT document_versions_fingerprint_format_check
    CHECK (fingerprint IS NULL OR fingerprint ~ '^[0-9a-f]{64}$'),
  CONSTRAINT document_versions_lock_fields_check
    CHECK (
      (immutable_version = FALSE AND locked_at IS NULL)
      OR (immutable_version = TRUE AND locked_at IS NOT NULL AND fingerprint IS NOT NULL)
    ),
  CONSTRAINT document_versions_immutable_status_check
    CHECK (
      status NOT IN ('SIGNED_FINAL', 'NOTARIZED_FINAL', 'ARCHIVED')
      OR (immutable_version = TRUE AND locked_at IS NOT NULL AND fingerprint IS NOT NULL)
    ),
  CONSTRAINT document_versions_storage_reference_check CHECK (
    (storage_bucket IS NULL AND storage_object_key IS NULL)
    OR (storage_bucket IS NOT NULL AND length(trim(storage_bucket)) > 0
        AND storage_object_key IS NOT NULL AND length(trim(storage_object_key)) > 0)
    )
);

ALTER TABLE documents
  ADD CONSTRAINT documents_original_version_fk
    FOREIGN KEY (original_version_id, organization_id)
    REFERENCES document_versions(id, organization_id),
  ADD CONSTRAINT documents_working_version_fk
    FOREIGN KEY (working_version_id, organization_id)
    REFERENCES document_versions(id, organization_id);

CREATE INDEX idx_document_versions_document_number
  ON document_versions(document_id, version_number ASC);
CREATE INDEX idx_document_versions_organization_document
  ON document_versions(organization_id, document_id, version_number ASC);
CREATE INDEX idx_document_versions_predecessor
  ON document_versions(predecessor_version_id);
CREATE INDEX idx_document_versions_status
  ON document_versions(document_id, status);
CREATE INDEX idx_document_versions_fingerprint
  ON document_versions(fingerprint);

CREATE OR REPLACE FUNCTION reject_immutable_document_version_mutation()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  IF TG_OP = 'DELETE' THEN
    IF OLD.immutable_version
       OR OLD.status IN ('SIGNED_FINAL', 'NOTARIZED_FINAL', 'ARCHIVED') THEN
      RAISE EXCEPTION 'document version % is immutable', OLD.id
        USING ERRCODE = '55000';
    END IF;
    RETURN OLD;
  END IF;

  IF OLD.immutable_version
     OR OLD.status IN ('SIGNED_FINAL', 'NOTARIZED_FINAL', 'ARCHIVED') THEN
    RAISE EXCEPTION 'document version % is immutable', OLD.id
      USING ERRCODE = '55000';
  END IF;

  IF NEW.immutable_version
     OR NEW.status IN ('SIGNED_FINAL', 'NOTARIZED_FINAL', 'ARCHIVED') THEN
    NEW.immutable_version := TRUE;
    IF NEW.locked_at IS NULL THEN
      NEW.locked_at := now();
    END IF;
  END IF;

  RETURN NEW;
END;
$$;

CREATE TRIGGER document_versions_immutable_mutation_guard
BEFORE UPDATE OR DELETE ON document_versions
FOR EACH ROW
EXECUTE FUNCTION reject_immutable_document_version_mutation();
