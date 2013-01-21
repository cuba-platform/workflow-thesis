create table WF_DESIGN (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    NAME varchar(100),
    SRC longvarchar,
    NOTIFICATION_MATRIX longvarbinary,
    NOTIFICATION_MATRIX_UPLOADED smallint,
    LOCALIZATION longvarchar,
    COMPILE_TS timestamp,
    primary key (ID)
);

------------------------------------------------------------------------------------------------------------

create table WF_DESIGN_SCRIPT (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    DESIGN_ID varchar(36),
    NAME varchar(100),
    CONTENT text,
    primary key (ID)
);

alter table WF_DESIGN_SCRIPT add constraint FK_WF_DESIGN_SCRIPT_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID);

------------------------------------------------------------------------------------------------------------

create table WF_DESIGN_FILE (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    DESIGN_ID varchar(36),
    NAME varchar(100),
    TYPE varchar(20),
    CONTENT longvarchar,
    BINARY_CONTENT longvarbinary,
    primary key (ID)
);

alter table WF_DESIGN_FILE add constraint FK_WF_DESIGN_FILE_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID);

------------------------------------------------------------------------------------------------------------

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
    CODE varchar(255),
    MESSAGES_PACK varchar(200),
    CARD_TYPES varchar(500),
    STATES varchar(500),
    PERMISSIONS_ENABLED boolean,
    DESIGN_ID varchar(36),
    AVAILABLE_ROLE_ID varchar(36),
    COMBINED_STAGES_ENABLED boolean,
    primary key (ID)
);

alter table WF_PROC add constraint FK_WF_PROC_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID);
alter table WF_PROC add constraint WF_PROC_AVAILABLE_ROLE_ID foreign key (AVAILABLE_ROLE_ID) references SEC_ROLE(ID);
alter table WF_PROC add constraint WF_PROC_UNIQ_CODE unique (CODE);

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
    SUBSTITUTED_CREATOR_ID varchar(36),
    HAS_ATTACHMENTS boolean,
    HAS_ATTRIBUTES boolean,
    CATEGORY_ID varchar(36),
    FAMILY_CARD_ID varchar(36),
    FAMILY_JBPM_PROCESS_ID varchar(255),
    primary key (ID)
);

alter table WF_CARD add constraint FK_WF_CARD_PROC foreign key (PROC_ID) references WF_PROC (ID);
alter table WF_CARD add constraint FK_WF_CARD_USER foreign key (CREATOR_ID) references SEC_USER (ID);
alter table WF_CARD add constraint FK_WF_CARD_CARD foreign key (PARENT_CARD_ID) references WF_CARD (ID);
alter table WF_CARD add constraint FK_WF_CARD_SUBSTITUTED_CREATOR foreign key (SUBSTITUTED_CREATOR_ID) references SEC_USER (ID);
alter table WF_CARD add constraint FK_WF_CARD_CATEGORY_ID foreign key (CATEGORY_ID) references SYS_CATEGORY(ID);
alter table WF_CARD add constraint WF_CARD_FAMILY_CARD foreign key (FAMILY_CARD_ID) references WF_CARD(ID);
create index IDX_WF_CARD_FAMILY_CARD on WF_CARD(FAMILY_CARD_ID);
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
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_ID varchar(36),
    TYPE integer,
    USER_ID varchar(36),
    JBPM_EXECUTION_ID varchar(255),
    ACTIVITY varchar(255),
    DESCRIPTION text,
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
    COMMENT text,
    ITERATION integer,
    SUBPROC_CARD_ID varchar(36),
    FAMILY_ASSIGNMENT_ID varchar(36),
    primary key (ID)
);

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_USER foreign key (USER_ID) references SEC_USER (ID);

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_FINISHED_BY foreign key (FINISHED_BY) references SEC_USER (ID);

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_CARD foreign key (CARD_ID) references WF_CARD (ID);

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_PROC foreign key (PROC_ID) references WF_PROC (ID);

alter table WF_ASSIGNMENT add constraint WF_ASSIGNMENT_FAMILY_ASSIGNMENT foreign key (FAMILY_ASSIGNMENT_ID) references WF_ASSIGNMENT(ID);

alter table WF_ASSIGNMENT add constraint WF_ASSIGNMENT_SUBPROC_CARD foreign key (SUBPROC_CARD_ID) references WF_CARD(ID);

------------------------------------------------------------------------------------------------------------

create table WF_ATTACHMENTTYPE (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    VERSION integer,
    NAME varchar(500),
    COMMENT varchar(1000),
    ISDEFAULT boolean,
    CODE varchar(200),
    primary key (ID)
);

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
    TYPE_ID varchar(36),
    NAME varchar(500),
    COMMENT text,
    SIGNATURES longvarchar,
    CARD_ID varchar(36),
    ASSIGNMENT_ID varchar(36),
    VERSION_OF_ID varchar(36),
    VERSION_NUM integer,
    primary key (ID)
);

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_FILE foreign key (FILE_ID) references SYS_FILE (ID);

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_CARD foreign key (CARD_ID) references WF_CARD (ID);

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_ASSIGNMENT foreign key (ASSIGNMENT_ID) references WF_ASSIGNMENT (ID);

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_TYPE foreign key (TYPE_ID) references WF_ATTACHMENTTYPE (ID);

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_ATTACHMENT foreign key (VERSION_OF_ID) references WF_ATTACHMENT (ID)^

insert into WF_ATTACHMENTTYPE (ID,CODE,ISDEFAULT)
values ('6c9c8ccc-e761-11df-94cb-6f884bc56e70','AttachmentType.attachment',true);

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
    INVISIBLE smallint,
    ROLE_ID varchar(36),
    ASSIGN_TO_CREATOR smallint,
    SORT_ORDER integer,
    ORDER_FILLING_TYPE varchar(1),
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
    NOTIFY_BY_CARD_INFO smallint,
    SORT_ORDER integer,
    DURATION integer,
    TIME_UNIT varchar(1),
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
    JBPM_PROCESS_ID varchar(255),
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
    SORT_ORDER integer,
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
    WORK_START_TIME timestamp,
    WORK_END_TIME timestamp,
    primary key (ID)
);

------------------------------------------------------------------------------------------------------------

create table WF_PROC_ROLE_PERMISSION (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    PROC_ROLE_FROM_ID varchar(36),
    PROC_ROLE_TO_ID varchar(36),
    STATE varchar(255),
    value numeric(2),
    type numeric(2),
    primary key (ID)
);

alter table WF_PROC_ROLE_PERMISSION add constraint FK_WF_PROC_ROLE_PERMISSION_TO_PROC_ROLE foreign key (PROC_ROLE_TO_ID) references WF_PROC_ROLE (ID);
alter table WF_PROC_ROLE_PERMISSION add constraint FK_WF_PROC_ROLE_PERMISSION_FROM_PROC_ROLE foreign key (PROC_ROLE_FROM_ID) references WF_PROC_ROLE (ID);

------------------------------------------------------------------------------------------------------------
create table WF_SENDING_SMS
(
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),

    SMS_ID varchar(255),
    PHONE varchar(50),
    MESSAGE varchar(255),
    ERROR_CODE integer,
    STATUS integer,
    LAST_CHANGE_DATE timestamp,
    ATTEMPTS_COUNT integer,
    ADDRESSEE varchar(200),
    START_SENDING_DATE timestamp,
    primary key (ID)
)^
------------------------------------------------------------------------------------------------------------

create table WF_USER_NOTIFIED_BY_SMS
(
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),

    USER_ID varchar(36),
    primary key (ID)
)^
alter table WF_USER_NOTIFIED_BY_SMS add constraint FK_WF_USER_NOTIFIED_BY_SMS_USER foreign key (USER_ID) references SEC_USER (ID)^

