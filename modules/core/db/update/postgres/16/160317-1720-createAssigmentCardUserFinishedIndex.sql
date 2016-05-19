create or replace function insertIndexCurrency()
returns void
as $$
begin
   IF NOT EXISTS (
    SELECT 1
    FROM   pg_class c
    JOIN   pg_namespace n ON n.oid = c.relnamespace
    WHERE  c.relname = 'idx_wf_assignment_card_user_finished'
    ) THEN

    create index IDX_WF_ASSIGNMENT_CARD_USER_FINISHED on WF_ASSIGNMENT  (CARD_ID, USER_ID, FINISHED)  ;
       end if;
end
$$ language plpgsql^

select insertIndexCurrency()^
drop function insertIndexCurrency()^
