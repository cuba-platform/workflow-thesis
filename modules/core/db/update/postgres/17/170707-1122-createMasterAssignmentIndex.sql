-- $Id$
-- Description:
DO $$
BEGIN
IF NOT EXISTS (
    SELECT 1
    FROM   pg_class c
    JOIN   pg_namespace n ON n.oid = c.relnamespace
    WHERE  c.relname = 'idx_wf_assignment_master'
    AND    n.nspname = 'public' -- 'public' by default
    ) THEN
	CREATE INDEX idx_wf_assignment_master ON wf_assignment(master_assignment_id);
END IF;

END$$;