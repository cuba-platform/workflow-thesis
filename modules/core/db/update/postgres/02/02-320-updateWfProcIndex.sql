--
CREATE OR REPLACE FUNCTION create_constraint_if_not_exists (
    t_name text, c_name text, constraint_sql text)
returns void AS
$$
begin
    -- Look for our constraint
    if exists (select constraint_name
                   from information_schema.constraint_column_usage
                   where table_name = t_name  and constraint_name = c_name) then
        execute constraint_sql;
    end if;
end;
$$
LANGUAGE plpgsql^

DROP INDEX IF EXISTS IDX_WF_PROC_UNIQ_CODE^
SELECT create_constraint_if_not_exists(
        'wf_proc',
        'wf_proc_uniq_code',
        'ALTER TABLE wf_proc DROP CONSTRAINT wf_proc_uniq_code;')^
CREATE UNIQUE INDEX IDX_WF_PROC_UNIQ_CODE ON WF_PROC (CODE) WHERE DELETE_TS IS NULL^

DROP FUNCTION IF EXISTS create_constraint_if_not_exists()^