-- Description:

create table WF_PROC_STAGE (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    NAME varchar(255),
    DURATION numeric(3),
    TIME_UNIT varchar(1),
    PROC_ID uuid
);

alter table WF_PROC_STAGE add constraint FK_WF_PROC_STAGE_PROC foreign key (PROC_ID) references WF_PROC (ID);