-- Description:

DECLARE @max_dbid integer;
IF (NOT EXISTS (SELECT *
                 FROM INFORMATION_SCHEMA.TABLES
                 WHERE TABLE_SCHEMA = 'dbo'
                 AND TABLE_NAME = 'seq_jbpm_id_gen'))
BEGIN
    CREATE TABLE seq_jbpm_id_gen(ID bigint identity(1,1000), CREATE_TS datetime);
    SET IDENTITY_INSERT seq_jbpm_id_gen ON;
	select @max_dbid = (select max(dbid_) from (select dbid_ from JBPM4_DEPLOYMENT
                                                                    UNION select dbid_ from JBPM4_DEPLOYPROP
                                                                    UNION select dbid_ from JBPM4_EXECUTION
                                                                    UNION select dbid_ from JBPM4_HIST_ACTINST
                                                                    UNION select dbid_ from JBPM4_HIST_DETAIL
                                                                    UNION select dbid_ from JBPM4_HIST_PROCINST
                                                                    UNION select dbid_ from JBPM4_HIST_TASK
                                                                    UNION select dbid_ from JBPM4_HIST_VAR
                                                                    UNION select dbid_ from JBPM4_ID_GROUP
                                                                    UNION select dbid_ from JBPM4_ID_MEMBERSHIP
                                                                    UNION select dbid_ from JBPM4_ID_USER
                                                                    UNION select dbid_ from JBPM4_JOB
                                                                    UNION select dbid_ from JBPM4_LOB
                                                                    UNION select dbid_ from JBPM4_PARTICIPATION
                                                                    UNION select dbid_ from JBPM4_SWIMLANE
                                                                    UNION select dbid_ from JBPM4_TASK
                                                                    UNION select dbid_ from JBPM4_VARIABLE) unionTables);
    INSERT INTO seq_jbpm_id_gen(ID, CREATE_TS) VALUES (@max_dbid, CURRENT_TIMESTAMP);
    SET IDENTITY_INSERT seq_jbpm_id_gen OFF;
END









