-- Description: create WF_PROC_ROLE_PERMISSION table

create table WF_PROC_ROLE_PERMISSION (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    PROC_ROLE_FROM_ID uuid,
    PROC_ROLE_TO_ID uuid,
    STATE varchar(255),
    value numeric(2),
    type numeric(2),
    primary key (ID)
);

alter table WF_PROC_ROLE_PERMISSION add constraint FK_WF_PROC_ROLE_PERMISSION_TO_PROC_ROLE foreign key (PROC_ROLE_TO_ID) references WF_PROC_ROLE (ID);
alter table WF_PROC_ROLE_PERMISSION add constraint FK_WF_PROC_ROLE_PERMISSION_FROM_PROC_ROLE foreign key (PROC_ROLE_FROM_ID) references WF_PROC_ROLE (ID);

alter table WF_PROC add PERMISSIONS_ENABLED boolean;
alter table WF_PROC add STATES varchar(500);
