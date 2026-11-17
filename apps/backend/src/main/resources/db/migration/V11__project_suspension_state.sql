ALTER TABLE projects
  ADD COLUMN suspended_from_status VARCHAR(32);

ALTER TABLE projects
  ADD CONSTRAINT projects_suspended_from_status_check CHECK (
    (status = 'SUSPENDED' AND suspended_from_status IS NOT NULL)
    OR (status <> 'SUSPENDED' AND suspended_from_status IS NULL)
  );
