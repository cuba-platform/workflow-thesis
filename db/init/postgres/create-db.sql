create table WF_PROC (
    ID uuid,
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
)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    TYPE integer,
    PROC_ID uuid,
    JBPM_PROCESS_ID varchar(255),
    STATE varchar(255),
    DESCRIPTION varchar(1000),
    primary key (ID)
)^

alter table WF_CARD add constraint FK_WF_CARD_PROC foreign key (PROC_ID) references WF_PROC (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_ASSIGNMENT (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    USER_ID uuid,
    CARD_ID uuid,
    MASTER_ASSIGNMENT_ID uuid,
    NAME varchar(255),
    DESCRIPTION varchar(1000),
    JBPM_PROCESS_ID varchar(255),
    DUE_DATE timestamp,
    FINISHED timestamp,
    OUTCOME varchar(255),
    COMMENT varchar(2000),
    primary key (ID)
)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_USER foreign key (USER_ID) references SEC_USER (ID)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_CARD foreign key (CARD_ID) references WF_CARD (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_ATTACHMENT (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    TYPE char(1),
    FILE_ID uuid,
    NAME varchar(500),
    CARD_ID uuid,
    ASSIGNMENT_ID uuid,
    primary key (ID)
)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_FILE foreign key (FILE_ID) references SYS_FILE (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_ASSIGNMENT foreign key (ASSIGNMENT_ID) references WF_ASSIGNMENT (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_PROC_ROLE (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    PROC_ID uuid,
    CODE varchar(50),
    NAME varchar(100),
    IS_MULTI_USER boolean,
    ROLE_ID uuid,
    ASSIGN_TO_CREATOR boolean,
    primary key (ID)
)^

alter table WF_PROC_ROLE add constraint FK_WF_PROC_ROLE_PROC foreign key (PROC_ID) references WF_PROC (ID)^
alter table WF_PROC_ROLE add constraint FK_WF_PROC_ROLE_ROLE foreign key (ROLE_ID) references SEC_ROLE (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_ROLE (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_ID uuid,
    PROC_ROLE_ID uuid,
    CODE varchar(50),
    USER_ID uuid,
    NOTIFY_BY_EMAIL boolean,
    primary key (ID)
)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_ROLE foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_USER foreign key (USER_ID) references SEC_USER (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_DEFAULT_PROC_ACTOR (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    PROC_ROLE_ID uuid,
    USER_ID uuid,
    NOTIFY_BY_EMAIL boolean,
    primary key (ID)
)^

alter table WF_DEFAULT_PROC_ACTOR add constraint FK_WF_DEFAULT_PROC_ACTOR_PROC_ROLE foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID)^

alter table WF_DEFAULT_PROC_ACTOR add constraint FK_WF_DEFAULT_PROC_ACTOR_USER foreign key (USER_ID) references SEC_USER (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_TIMER (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    DUE_DATE timestamp,
    CARD_ID uuid,
    JBPM_EXECUTION_ID varchar(255),
    ACTIVITY varchar(255),
    ACTION_CLASS varchar(200),
    ACTION_PARAMS varchar(2000),
    primary key (ID)
)^

alter table WF_TIMER add constraint FK_WF_TIMER_CARD foreign key (CARD_ID) references WF_CARD (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CALENDAR (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    WORK_DAY date,
    WORK_DAY_OF_WEEK numeric(1),
    WORK_START char(4),
    WORK_END char(4),
    primary key (ID)
);
