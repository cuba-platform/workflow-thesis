-- Description:

CREATE OR REPLACE FUNCTION createSequence()
RETURNS void
AS $$
DECLARE
c integer = 0;
BEGIN
c = (select count(*) from pg_class where relname = 'seq_jbpm_id_gen');
if(c = 0)
  then create sequence seq_jbpm_id_gen start with 1 increment by 1000;
end if;
END;
$$
LANGUAGE plpgsql^
select createSequence()^
drop function createSequence()^

select setval('seq_jbpm_id_gen', (select max(dbid_) from (select dbid_ from JBPM4_DEPLOYMENT
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
                      UNION select dbid_ from JBPM4_VARIABLE) unionTables));


