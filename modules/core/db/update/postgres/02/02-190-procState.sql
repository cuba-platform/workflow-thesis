-- Description:

create table WF_PROC_STATE (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),

    NAME varchar(200),
    PROC_ID uuid,

    primary key (ID)
)^

alter table WF_PROC_STATE add constraint FK_WF_PROC_STATE_PROC foreign key (PROC_ID) references WF_PROC (ID);
