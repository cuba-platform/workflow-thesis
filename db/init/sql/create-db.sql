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
    CARD_TYPES varchar(500),
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
    CREATOR_ID varchar(36),
    PARENT_CARD_ID varchar(36),
    primary key (ID)
);

alter table WF_CARD add constraint FK_WF_CARD_PROC foreign key (PROC_ID) references WF_PROC (ID);
alter table WF_CARD add constraint FK_WF_CARD_USER foreign key (CREATOR_ID) references SEC_USER (ID);
alter table WF_CARD add constraint FK_WF_CARD_CARD foreign key (PARENT_CARD_ID) references WF_CARD (ID);

------------------------------------------------------------------------------------------------------------

create table WF_CARD_RELATION (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_ID varchar(36),
    RELATED_CARD_ID varchar(36),
    primary key (ID)
);

alter table WF_CARD_RELATION add constraint FK_WF_CC_CARD foreign key (CARD_ID) references WF_CARD (ID);
alter table WF_CARD_RELATION add constraint FK_WF_CC_CARD_RELATED foreign key (RELATED_CARD_ID) references WF_CARD (ID);

------------------------------------------------------------------------------------------------------------
create table WF_CARD_INFO (
    ID varchar(36),
    NAME varchar(50),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    CARD_ID varchar(36),
    TYPE integer,
    USER_ID varchar(36),
    JBPM_EXECUTION_ID varchar(255),
    ACTIVITY varchar(255),
    primary key (ID)
);

alter table WF_CARD_INFO add constraint FK_WF_CARD_INFO_CARD foreign key (CARD_ID) references WF_CARD(ID);
alter table WF_CARD_INFO add constraint FK_WF_CARD_INFO_USER foreign key (USER_ID) references SEC_USER(ID);

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
    PROC_ID varchar(36),
    MASTER_ASSIGNMENT_ID varchar(36),
    NAME varchar(255),
    DESCRIPTION varchar(1000),
    JBPM_PROCESS_ID varchar(255),
    DUE_DATE timestamp,
    FINISHED timestamp,
    FINISHED_BY varchar(36),
    OUTCOME varchar(255),
    COMMENT varchar(2000),
    ITERATION integer,
    primary key (ID)
);

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_USER foreign key (USER_ID) references SEC_USER (ID);

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_FINISHED_BY foreign key (FINISHED_BY) references SEC_USER (ID);

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_CARD foreign key (CARD_ID) references WF_CARD (ID);

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_PROC foreign key (PROC_ID) references WF_PROC (ID);

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
    TYPE char(1),
    FILE_ID varchar(36),
    NAME varchar(500),
    CARD_ID varchar(36),
    ASSIGNMENT_ID varchar(36),
    primary key (ID)
);

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_FILE foreign key (FILE_ID) references SYS_FILE (ID);

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_CARD foreign key (CARD_ID) references WF_CARD (ID);

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_ASSIGNMENT foreign key (ASSIGNMENT_ID) references WF_ASSIGNMENT (ID);

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
    ROLE_ID varchar(36),
    ASSIGN_TO_CREATOR smallint,
    primary key (ID)
);

alter table WF_PROC_ROLE add constraint FK_WF_PROC_ROLE_PROC foreign key (PROC_ID) references WF_PROC (ID);
alter table WF_PROC_ROLE add constraint FK_WF_PROC_ROLE_ROLE foreign key (ROLE_ID) references SEC_ROLE (ID);

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
    CODE varchar(50),
    USER_ID varchar(36),
    NOTIFY_BY_EMAIL smallint,
    primary key (ID)
);

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_CARD foreign key (CARD_ID) references WF_CARD (ID);

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_PROC_ROLE foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID);

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_USER foreign key (USER_ID) references SEC_USER (ID);

------------------------------------------------------------------------------------------------------------

create table WF_CARD_PROC (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_ID varchar(36),
    PROC_ID varchar(36),
    IS_ACTIVE smallint,
    START_COUNT integer,
    STATE varchar(255),
    SORT_ORDER integer,
    primary key (ID)
);

alter table WF_CARD_PROC add constraint FK_WF_CARD_PROC_CARD foreign key (CARD_ID) references WF_CARD (ID);

alter table WF_CARD_PROC add constraint FK_WF_CARD_PROC_PROC foreign key (PROC_ID) references WF_PROC (ID);

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

------------------------------------------------------------------------------------------------------------

create table WF_TIMER (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    DUE_DATE timestamp,
    CARD_ID varchar(36),
    JBPM_EXECUTION_ID varchar(255),
    ACTIVITY varchar(255),
    TASK_CLASS varchar(200),
    TASK_PARAMS varchar(2000),
    primary key (ID)
);

alter table WF_TIMER add constraint FK_WF_TIMER_CARD foreign key (CARD_ID) references WF_CARD (ID);

------------------------------------------------------------------------------------------------------------

create table WF_CALENDAR (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    WORK_DAY date,
    WORK_DAY_OF_WEEK smallint,
    WORK_START char(5),
    WORK_END char(5),
    COMMENT varchar(500),
    primary key (ID)
);
