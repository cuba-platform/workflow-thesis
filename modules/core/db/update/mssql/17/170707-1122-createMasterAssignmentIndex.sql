-- $Id$
-- Description:

IF NOT EXISTS(SELECT * FROM sys.indexes WHERE name = 'idx_wf_assignment_master' AND object_id = OBJECT_ID('wf_assignment'))
    BEGIN
       CREATE INDEX idx_wf_assignment_master ON wf_assignment(master_assignment_id);
    END