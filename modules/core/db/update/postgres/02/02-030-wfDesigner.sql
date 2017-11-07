-- Description: WfDesigner support

create table WF_DESIGN (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    NAME varchar(100),
    SRC text,
    NOTIFICATION_MATRIX bytea,
    NOTIFICATION_MATRIX_UPLOADED boolean,
    LOCALIZATION text,
    COMPILE_TS timestamp,
    primary key (ID)
)^

create table WF_DESIGN_SCRIPT (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    DESIGN_ID uuid,
    NAME varchar(100),
    CONTENT text,
    primary key (ID)
)^

alter table WF_DESIGN_SCRIPT add constraint FK_WF_DESIGN_SCRIPT_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID)^

create table WF_DESIGN_FILE (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    DESIGN_ID uuid,
    NAME varchar(100),
    TYPE varchar(20),
    CONTENT text,
    BINARY_CONTENT bytea,
    primary key (ID)
)^

alter table WF_DESIGN_FILE add constraint FK_WF_DESIGN_FILE_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID)^

alter table WF_PROC add DESIGN_ID uuid^

alter table WF_PROC add constraint FK_WF_PROC_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID)^

