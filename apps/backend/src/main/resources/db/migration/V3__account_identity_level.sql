ALTER TABLE users
  ADD COLUMN identity_level VARCHAR(2) NOT NULL DEFAULT 'I0',
  ADD COLUMN contact_verification_status VARCHAR(32) NOT NULL DEFAULT 'UNVERIFIED',
  ADD COLUMN contact_verified_at TIMESTAMPTZ;

ALTER TABLE users
  ADD CONSTRAINT users_identity_level_check
    CHECK (identity_level IN ('I0', 'I1', 'I2', 'I3', 'I4', 'I5', 'I6')),
  ADD CONSTRAINT users_contact_verification_status_check
    CHECK (contact_verification_status IN ('UNVERIFIED', 'PENDING', 'VERIFIED', 'EXPIRED', 'REVOKED'));
