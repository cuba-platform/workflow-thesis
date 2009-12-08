create table WF_PROC (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    NAME varchar(255),
    JBPM_PROCESS_KEY varchar(255),
    MESSAGES_PACK varchar(200),
    primary key (ID)
);

------------------------------------------------------------------------------------------------------------

create table WF_CARD (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    TYPE integer,
    PROC_ID varchar(36),
    JBPM_PROCESS_ID varchar(255),
    STATE varchar(255),
    DESCRIPTION varchar(1000),
    primary key (ID)
);

alter table WF_CARD add constraint FK_WF_CARD_PROC foreign key (PROC_ID) references WF_PROC (ID);

------------------------------------------------------------------------------------------------------------

create table WF_ATTACHMENT (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_ID varchar(36),
    FILE_ID varchar(36),
    NAME varchar(500),
    primary key (ID)
);

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_CARD foreign key (CARD_ID) references WF_CARD (ID);

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_FILE foreign key (FILE_ID) references SYS_FILE (ID);

------------------------------------------------------------------------------------------------------------

create table WF_ASSIGNMENT (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    USER_ID varchar(36),
    CARD_ID varchar(36),
    MASTER_ASSIGNMENT_ID varchar(36),
    NAME varchar(255),
    DESCRIPTION varchar(1000),
    JBPM_PROCESS_ID varchar(255),
    FINISHED timestamp,
    OUTCOME varchar(255),
    COMMENT varchar(2000),
    primary key (ID)
);

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_USER foreign key (USER_ID) references SEC_USER (ID);

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_CARD foreign key (CARD_ID) references WF_CARD (ID);

------------------------------------------------------------------------------------------------------------

create table WF_PROC_ROLE (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    PROC_ID varchar(36),
    CODE varchar(50),
    NAME varchar(100),
    IS_MULTI_USER smallint,
    primary key (ID)
);

alter table WF_PROC_ROLE add constraint FK_WF_PROC_ROLE_PROC foreign key (PROC_ID) references WF_PROC (ID);

------------------------------------------------------------------------------------------------------------

create table WF_CARD_ROLE (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_ID varchar(36),
    PROC_ROLE_ID varchar(36),
    USER_ID varchar(36),
    NOTIFY_BY_EMAIL smallint,
    primary key (ID)
);

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_CARD foreign key (CARD_ID) references WF_CARD (ID);

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_PROC_ROLE foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID);

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_USER foreign key (USER_ID) references SEC_USER (ID);

------------------------------------------------------------------------------------------------------------

create table WF_DEFAULT_PROC_ACTOR (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    PROC_ROLE_ID varchar(36),
    USER_ID varchar(36),
    NOTIFY_BY_EMAIL smallint,
    primary key (ID)
);

alter table WF_DEFAULT_PROC_ACTOR add constraint FK_WF_DEFAULT_PROC_ACTOR_PROC_ROLE foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID);

alter table WF_DEFAULT_PROC_ACTOR add constraint FK_WF_DEFAULT_PROC_ACTOR_USER foreign key (USER_ID) references SEC_USER (ID);
