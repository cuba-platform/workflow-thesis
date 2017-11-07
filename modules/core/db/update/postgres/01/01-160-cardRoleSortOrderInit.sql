-- Description: initialize column WF_CARD_ROLE.SORT_ORDER

create or replace function wf_init_cr_sort_order() returns void as $$
declare
    card record;
    cr record;
    idx integer;
begin
    for card in select * from wf_card
    loop
        idx = 0;
        for cr in select * from wf_card_role where card_id = card.id order by create_ts
        loop
            idx = idx + 1;
            update wf_card_role set sort_order = idx where id = cr.id;
        end loop;
    end loop;
end;
$$ language plpgsql;
^

select wf_init_cr_sort_order()
^