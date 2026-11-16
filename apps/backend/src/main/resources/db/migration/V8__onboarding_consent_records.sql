ALTER TABLE users
  ADD COLUMN onboarding_completed_at TIMESTAMPTZ;

CREATE TABLE consent_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  consent_type VARCHAR(64) NOT NULL,
  policy_id VARCHAR(120) NOT NULL,
  policy_version VARCHAR(64) NOT NULL,
  policy_reference VARCHAR(255) NOT NULL,
  consent_text TEXT NOT NULL,
  locale VARCHAR(16) NOT NULL DEFAULT 'en-PH',
  status VARCHAR(16) NOT NULL,
  decided_at TIMESTAMPTZ NOT NULL,
  withdrawn_at TIMESTAMPTZ,
  actor_user_id UUID NOT NULL REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  CONSTRAINT consent_records_type_check
    CHECK (consent_type IN (
      'TERMS_OF_SERVICE',
      'PRIVACY_NOTICE',
      'ELECTRONIC_RECORDS',
      'PLATFORM_LIMITS_ACKNOWLEDGEMENT'
    )),
  CONSTRAINT consent_records_status_check
    CHECK (status IN ('ACCEPTED', 'DECLINED', 'WITHDRAWN')),
  CONSTRAINT consent_records_locale_not_blank
    CHECK (length(trim(locale)) > 0),
  CONSTRAINT consent_records_text_not_blank
    CHECK (length(trim(consent_text)) > 0)
);

CREATE INDEX idx_consent_records_user_type_created
  ON consent_records(user_id, consent_type, created_at DESC);
CREATE INDEX idx_consent_records_policy
  ON consent_records(policy_id, policy_version);
