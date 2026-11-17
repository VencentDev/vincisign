CREATE TABLE evidence_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  sequence_number BIGSERIAL NOT NULL UNIQUE,
  event_type VARCHAR(120) NOT NULL,
  event_version INTEGER NOT NULL DEFAULT 1,
  occurred_at TIMESTAMPTZ NOT NULL,
  recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Manila',
  organization_id UUID NOT NULL REFERENCES organizations(id),
  workspace_id UUID,
  project_id UUID NOT NULL,
  document_id UUID,
  document_version_id UUID,
  participant_id UUID,
  actor_type VARCHAR(32) NOT NULL,
  actor_user_id UUID REFERENCES users(id),
  actor_role VARCHAR(64),
  identity_level VARCHAR(16),
  source VARCHAR(32) NOT NULL,
  request_id VARCHAR(255),
  correlation_id VARCHAR(255),
  idempotency_key VARCHAR(255),
  aggregate_version BIGINT,
  policy_id VARCHAR(120),
  policy_version VARCHAR(64),
  reason_code VARCHAR(120),
  device_context_ref VARCHAR(255),
  previous_event_hash VARCHAR(64),
  event_hash VARCHAR(64) NOT NULL,
  payload JSONB NOT NULL DEFAULT '{}'::jsonb,
  CONSTRAINT evidence_events_project_organization_fk
    FOREIGN KEY (project_id, organization_id)
    REFERENCES projects(id, organization_id),
  CONSTRAINT evidence_events_workspace_organization_fk
    FOREIGN KEY (workspace_id, organization_id)
    REFERENCES workspaces(id, organization_id),
  CONSTRAINT evidence_events_document_organization_fk
    FOREIGN KEY (document_id, organization_id)
    REFERENCES documents(id, organization_id),
  CONSTRAINT evidence_events_version_organization_fk
    FOREIGN KEY (document_version_id, organization_id)
    REFERENCES document_versions(id, organization_id),
  CONSTRAINT evidence_events_event_version_positive CHECK (event_version > 0),
  CONSTRAINT evidence_events_actor_type_check CHECK (actor_type IN (
    'ACCOUNT',
    'GUEST_PARTICIPANT',
    'API_CLIENT',
    'SYSTEM',
    'SCHEDULER',
    'PROVIDER',
    'ADMIN',
    'WEBHOOK',
    'ENP'
  )),
  CONSTRAINT evidence_events_source_check CHECK (source IN (
    'USER',
    'API_CLIENT',
    'SYSTEM',
    'SCHEDULER',
    'PROVIDER',
    'ADMIN',
    'WEBHOOK'
  )),
  CONSTRAINT evidence_events_hash_format_check CHECK (
    event_hash ~ '^[0-9a-f]{64}$'
    AND (previous_event_hash IS NULL OR previous_event_hash ~ '^[0-9a-f]{64}$')
  )
);

CREATE INDEX idx_evidence_events_project_sequence
  ON evidence_events(project_id, sequence_number ASC);
CREATE INDEX idx_evidence_events_organization_recorded
  ON evidence_events(organization_id, recorded_at DESC);
CREATE INDEX idx_evidence_events_document_version
  ON evidence_events(document_version_id, sequence_number ASC);

CREATE OR REPLACE FUNCTION reject_evidence_event_mutation()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  RAISE EXCEPTION 'evidence events are append-only'
    USING ERRCODE = '55000';
END;
$$;

CREATE TRIGGER evidence_events_append_only_guard
BEFORE UPDATE OR DELETE ON evidence_events
FOR EACH ROW
EXECUTE FUNCTION reject_evidence_event_mutation();
