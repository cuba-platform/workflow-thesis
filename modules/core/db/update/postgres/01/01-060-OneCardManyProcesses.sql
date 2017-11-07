-- Description: One Card - Many Processes enhancement

create table WF_CARD_PROC (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_ID uuid,
    PROC_ID uuid,
    IS_ACTIVE boolean,
    SORT_ORDER integer,
    primary key (ID)
)
^

alter table WF_CARD_PROC add constraint FK_WF_CARD_PROC_CARD foreign key (CARD_ID) references WF_CARD (ID)
^
alter table WF_CARD_PROC add constraint FK_WF_CARD_PROC_PROC foreign key (PROC_ID) references WF_PROC (ID)
^

alter table WF_PROC add CARD_TYPES varchar(500)
^
update wf_proc set card_types = ',tm$Task,' where jbpm_process_key = 'TaskManagement'
^
update wf_proc set card_types = ',df$SimpleDoc,df$Contract,' where jbpm_process_key in ('SimpleDocument1', 'Endorsement', 'KrsoEndorsement')
^

--insert into wf_card_proc (id, create_ts, created_by, version, card_id, proc_id, is_active, sort_order)
--select newid(), current_timestamp, 'admin', 0, d.card_id, c.proc_id, case when c.state is null then false else true end, 0
--from df_doc d join wf_card c on c.id = d.card_id
--where c.proc_id is not null
--^

alter table WF_ASSIGNMENT add PROC_ID uuid
^
alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_PROC foreign key (PROC_ID) references WF_PROC (ID)
^

update wf_assignment set proc_id = c.proc_id
from wf_card c
where c.id = wf_assignment.card_id
^

