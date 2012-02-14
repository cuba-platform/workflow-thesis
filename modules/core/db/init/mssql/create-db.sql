create table WF_DESIGN (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    NAME varchar(100),
    SRC text,
    NOTIFICATION_MATRIX image,
    NOTIFICATION_MATRIX_UPLOADED tinyint,
    LOCALIZATION text,
    COMPILE_TS datetime,
    primary key (ID)
)^

------------------------------------------------------------------------------------------------------------

create table WF_DESIGN_SCRIPT (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    DESIGN_ID uniqueidentifier,
    NAME varchar(100),
    CONTENT text,
    primary key nonclustered (ID)
)^

alter table WF_DESIGN_SCRIPT add constraint FK_WF_DESIGN_SCRIPT_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID)^

create clustered index IDX_WF_DESIGN_SCRIPT_DESIGN on WF_DESIGN_SCRIPT (DESIGN_ID)^

------------------------------------------------------------------------------------------------------------

create table WF_DESIGN_FILE (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    DESIGN_ID uniqueidentifier,
    NAME varchar(100),
    TYPE varchar(20),
    CONTENT text,
    BINARY_CONTENT image,
    primary key nonclustered (ID)
)^

alter table WF_DESIGN_FILE add constraint FK_WF_DESIGN_FILE_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID)^

create clustered index IDX_WF_DESIGN_FILE_DESIGN on WF_DESIGN_FILE (DESIGN_ID)^

------------------------------------------------------------------------------------------------------------

create table WF_PROC (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    NAME varchar(255),
    JBPM_PROCESS_KEY varchar(255),
    CODE varchar(255) ,
    MESSAGES_PACK varchar(200),
    CARD_TYPES varchar(500),
    STATES varchar(500),
    PERMISSIONS_ENABLED tinyint,
    DESIGN_ID uniqueidentifier,
    AVAILABLE_ROLE_ID uniqueidentifier,
    primary key (ID)
)^

alter table WF_PROC add constraint FK_WF_PROC_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID)^
alter table WF_PROC add constraint WF_PROC_AVAILABLE_ROLE_ID foreign key (AVAILABLE_ROLE_ID) references SEC_ROLE(ID)^

create unique index IDX_WF_PROC_UNIQ_CODE on WF_PROC (CODE) where DELETE_TS is null^

------------------------------------------------------------------------------------------------------------

create table WF_CARD (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    TYPE integer,
    PROC_ID uniqueidentifier,
    JBPM_PROCESS_ID varchar(255),
    STATE varchar(255),
    DESCRIPTION varchar(1000),
    CREATOR_ID uniqueidentifier,
    SUBSTITUTED_CREATOR_ID uniqueidentifier,
    PARENT_CARD_ID uniqueidentifier,
    HAS_ATTACHMENTS tinyint,
    HAS_ATTRIBUTES tinyint,
    CATEGORY_ID uniqueidentifier,
    primary key nonclustered (ID)
)^

alter table WF_CARD add constraint FK_WF_CARD_PROC foreign key (PROC_ID) references WF_PROC (ID)^
alter table WF_CARD add constraint FK_WF_CARD_USER foreign key (CREATOR_ID) references SEC_USER (ID)^
alter table WF_CARD add constraint FK_WF_CARD_CARD foreign key (PARENT_CARD_ID) references WF_CARD (ID)^
alter table WF_CARD add constraint FK_WF_CARD_SUBSTITUTED_CREATOR foreign key (SUBSTITUTED_CREATOR_ID) references SEC_USER (ID)^
alter table WF_CARD add constraint FK_WF_CARD_CATEGORY_ID foreign key (CATEGORY_ID) references SYS_CATEGORY(ID)^

create clustered index IDX_WF_CARD_CREATE_TS on WF_CARD (CREATE_TS)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_COMMENT (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    COMMENT text,
    USER_ID uniqueidentifier,
    CARD_ID uniqueidentifier,
    PARENT_ID uniqueidentifier,
    primary key nonclustered (ID)
)^

alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_USER foreign key (USER_ID) references SEC_USER (ID)^
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_CARD foreign key (CARD_ID) references WF_CARD (ID)^
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_PARENT foreign key (PARENT_ID) references WF_CARD_COMMENT (ID)^

create clustered index IDX_WF_CARD_COMMENT_CARD on WF_CARD_COMMENT (CARD_ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_COMMENT_USER (
    CARD_COMMENT_ID uniqueidentifier,
    USER_ID uniqueidentifier,
    primary key (CARD_COMMENT_ID, USER_ID)
)^

alter table WF_CARD_COMMENT_USER add constraint FK_WF_CCU_CARD_COMMENT foreign key (CARD_COMMENT_ID) references WF_CARD_COMMENT (ID)^
alter table WF_CARD_COMMENT_USER add constraint FK_WF_CCU_USER foreign key (USER_ID) references SEC_USER (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_RELATION (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    CARD_ID uniqueidentifier,
    RELATED_CARD_ID uniqueidentifier,
    primary key nonclustered (ID)
)^

alter table WF_CARD_RELATION add constraint FK_WF_CC_CARD foreign key (CARD_ID) references WF_CARD (ID)^
alter table WF_CARD_RELATION add constraint FK_WF_CC_CARD_RELATED foreign key (RELATED_CARD_ID) references WF_CARD (ID)^

create clustered index IDX_WF_CARD_RELATION_CARD on WF_CARD_RELATION (CARD_ID)^

create index IDX_WF_CARD_RELATION_RELATED_CARD on WF_CARD_RELATION (RELATED_CARD_ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_INFO (
    ID uniqueidentifier,
    NAME varchar(50),
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    CARD_ID uniqueidentifier,
    TYPE integer,
    USER_ID uniqueidentifier,
    JBPM_EXECUTION_ID varchar(255),
    ACTIVITY varchar(255),
    DESCRIPTION text,
    primary key nonclustered (ID)
)^

alter table WF_CARD_INFO add constraint FK_WF_CARD_INFO_CARD foreign key (CARD_ID) references WF_CARD(ID)^
alter table WF_CARD_INFO add constraint FK_WF_CARD_INFO_USER foreign key (USER_ID) references SEC_USER(ID)^

create clustered index IDX_WF_CARD_INFO_CARD on WF_CARD_INFO (CARD_ID)^

create index IDX_WF_CARD_INFO_USER on WF_CARD_INFO (USER_ID, DELETE_TS)^

------------------------------------------------------------------------------------------------------------

create table WF_ASSIGNMENT (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    USER_ID uniqueidentifier,
    CARD_ID uniqueidentifier,
    PROC_ID uniqueidentifier,
    MASTER_ASSIGNMENT_ID uniqueidentifier,
    NAME varchar(255),
    DESCRIPTION varchar(1000),
    JBPM_PROCESS_ID varchar(255),
    DUE_DATE datetime,
    FINISHED datetime,
    FINISHED_BY uniqueidentifier,
    OUTCOME varchar(255),
    COMMENT text,
    ITERATION integer,
    primary key nonclustered (ID)
)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_USER foreign key (USER_ID) references SEC_USER (ID)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_FINISHED_BY foreign key (FINISHED_BY) references SEC_USER (ID)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_PROC foreign key (PROC_ID) references WF_PROC (ID)^

create clustered index IDX_WF_ASSIGNMENT_CARD on WF_ASSIGNMENT (CARD_ID)^

create index IDX_WF_ASSIGNMENT_USER on WF_ASSIGNMENT (USER_ID)^

create index IDX_WF_ASSIGNMENT_USER_FINISHED on WF_ASSIGNMENT (USER_ID, FINISHED)^

------------------------------------------------------------------------------------------------------------

create table WF_ATTACHMENTTYPE (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    VERSION integer,
    NAME varchar(500),
    COMMENT varchar(1000),
    ISDEFAULT tinyint,
    CODE varchar(200),
    ISSYSTEM tinyint,
    primary key (ID)
)^

create unique index IDX_WF_ATTACHMENTTYPE_UNIQ_CODE on WF_ATTACHMENTTYPE (CODE) where DELETE_TS is null^

------------------------------------------------------------------------------------------------------------

create table WF_ATTACHMENT (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    TYPE char(1),
    FILE_ID uniqueidentifier,
    TYPE_ID uniqueidentifier,
    NAME varchar(500),
    COMMENT varchar(1000),
    SIGNATURES text,
    CARD_ID uniqueidentifier,
    ASSIGNMENT_ID uniqueidentifier,
    VERSION_OF_ID uniqueidentifier,
    VERSION_NUM integer,
    primary key nonclustered (ID)
)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_FILE foreign key (FILE_ID) references SYS_FILE (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_ASSIGNMENT foreign key (ASSIGNMENT_ID) references WF_ASSIGNMENT (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_TYPE foreign key (TYPE_ID) references WF_ATTACHMENTTYPE (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_ATTACHMENT foreign key (VERSION_OF_ID) references WF_ATTACHMENT (ID)^

create clustered index IDX_WF_ATTACHMENT_CARD on WF_ATTACHMENT (CARD_ID)^

create index IDX_WF_ATTACHMENT_ASSIGNMENT on WF_ATTACHMENT (ASSIGNMENT_ID)^

------------------------------------------------------------------------------------------------------------

create table WF_PROC_ROLE (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    PROC_ID uniqueidentifier,
    CODE varchar(50),
    NAME varchar(100),
    IS_MULTI_USER tinyint,
    INVISIBLE tinyint,
    ROLE_ID uniqueidentifier,
    ASSIGN_TO_CREATOR tinyint,
    SORT_ORDER integer,
    ORDER_FILLING_TYPE varchar(1),
    primary key nonclustered (ID)
)^

alter table WF_PROC_ROLE add constraint FK_WF_PROC_ROLE_PROC foreign key (PROC_ID) references WF_PROC (ID)^
alter table WF_PROC_ROLE add constraint FK_WF_PROC_ROLE_ROLE foreign key (ROLE_ID) references SEC_ROLE (ID)^

create clustered index IDX_WF_PROC_ROLE_PROC on WF_PROC_ROLE (PROC_ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_ROLE (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    CARD_ID uniqueidentifier,
    PROC_ROLE_ID uniqueidentifier,
    CODE varchar(50),
    USER_ID uniqueidentifier,
    NOTIFY_BY_EMAIL tinyint,
    NOTIFY_BY_CARD_INFO tinyint,
    SORT_ORDER integer,
    primary key nonclustered (ID)
)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_ROLE foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_USER foreign key (USER_ID) references SEC_USER (ID)^

create clustered index IDX_WF_CARD_ROLE_CARD on WF_CARD_ROLE (CARD_ID)^

create index IDX_WF_CARD_ROLE_USER_CODE on WF_CARD_ROLE (USER_ID, CODE)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_PROC (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    CARD_ID uniqueidentifier,
    PROC_ID uniqueidentifier,
    IS_ACTIVE tinyint,
    START_COUNT integer,
    STATE varchar(255),
    SORT_ORDER integer,
    primary key nonclustered (ID)
)^

alter table WF_CARD_PROC add constraint FK_WF_CARD_PROC_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_CARD_PROC add constraint FK_WF_CARD_PROC_PROC foreign key (PROC_ID) references WF_PROC (ID)^

create clustered index IDX_WF_CARD_PROC_CARD on WF_CARD_PROC (CARD_ID)^

------------------------------------------------------------------------------------------------------------

create table WF_DEFAULT_PROC_ACTOR (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    PROC_ROLE_ID uniqueidentifier,
    USER_ID uniqueidentifier,
    NOTIFY_BY_EMAIL tinyint,
    primary key nonclustered (ID)
)^

alter table WF_DEFAULT_PROC_ACTOR add constraint FK_WF_DEFAULT_PROC_ACTOR_PROC_ROLE foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID)^

alter table WF_DEFAULT_PROC_ACTOR add constraint FK_WF_DEFAULT_PROC_ACTOR_USER foreign key (USER_ID) references SEC_USER (ID)^

create clustered index IDX_WF_DEFAULT_PROC_ACTOR_PROC_ROLE on WF_DEFAULT_PROC_ACTOR (PROC_ROLE_ID)^

------------------------------------------------------------------------------------------------------------

create table WF_TIMER (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    DUE_DATE datetime,
    CARD_ID uniqueidentifier,
    JBPM_EXECUTION_ID varchar(255),
    ACTIVITY varchar(255),
    ACTION_CLASS varchar(200),
    ACTION_PARAMS varchar(2000),
    primary key nonclustered (ID)
)^

alter table WF_TIMER add constraint FK_WF_TIMER_CARD foreign key (CARD_ID) references WF_CARD (ID)^

create clustered index IDX_WF_TIMER_DUE_DATE on WF_TIMER (DUE_DATE)^

create index IDX_WF_TIMER_CARD on WF_TIMER (CARD_ID)^

create index IDX_WF_TIMER_EXECUTION_ACTIVITY on WF_TIMER (JBPM_EXECUTION_ID, ACTIVITY)^

------------------------------------------------------------------------------------------------------------

create table WF_CALENDAR (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    WORK_DAY date,
    WORK_DAY_OF_WEEK numeric(1),
    WORK_START_TIME time,
    WORK_END_TIME time,
    COMMENT varchar(500),
    primary key nonclustered (ID)
);

create clustered index IDX_WF_CALENDAR_WORK_DAY on WF_CALENDAR (WORK_DAY)^

------------------------------------------------------------------------------------------------------------

create table WF_PROC_ROLE_PERMISSION (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    PROC_ROLE_FROM_ID uniqueidentifier,
    PROC_ROLE_TO_ID uniqueidentifier,
    STATE varchar(255),
    value numeric(2),
    type numeric(2),
    primary key (ID)
)^

alter table WF_PROC_ROLE_PERMISSION add constraint FK_WF_PROC_ROLE_PERMISSION_TO_PROC_ROLE foreign key (PROC_ROLE_TO_ID) references WF_PROC_ROLE (ID)^
alter table WF_PROC_ROLE_PERMISSION add constraint FK_WF_PROC_ROLE_PERMISSION_FROM_PROC_ROLE foreign key (PROC_ROLE_FROM_ID) references WF_PROC_ROLE (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_USER_GROUP (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    VERSION integer,
    NAME varchar(255),
    primary key (ID)
)^

------------------------------------------------------------------------------------------------------------

create table WF_USER_GROUP_USER (
    USER_GROUP_ID uniqueidentifier,
    USER_ID uniqueidentifier,
    primary key (USER_GROUP_ID, USER_ID)
)^

alter table WF_USER_GROUP_USER add constraint FK_WF_UGU_USER_GROUP foreign key (USER_GROUP_ID) references WF_USER_GROUP (ID)^
alter table WF_USER_GROUP_USER add constraint FK_WF_UGU_USER foreign key (USER_ID) references SEC_USER (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_PROC_STAGE (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    --
    NAME varchar(255),
    DURATION numeric(3),
    TIME_UNIT varchar(1),
    PROC_ID uniqueidentifier,
    START_ACTIVITY varchar(200),
    END_ACTIVITY varchar(200),
    END_TRANSITION varchar(200),
    NOTIFICATION_SCRIPT varchar(200),
    NOTIFY_CURRENT_ACTOR tinyint,
    PROC_STAGE_TYPE_ID uniqueidentifier,
    DURATION_SCRIPT_ENABLED tinyint,
    DURATION_SCRIPT text,
    --
    primary key nonclustered (ID)
)^

alter table WF_PROC_STAGE add constraint FK_WF_PROC_STAGE_PROC foreign key (PROC_ID) references WF_PROC (ID)^

create clustered index IDX_WF_PROC_STAGE_PROC on WF_PROC_STAGE (PROC_ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_STAGE (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    START_DATE datetime,
    END_DATE_PLAN datetime,
    END_DATE_FACT datetime,
    NOTIFIED tinyint default 0,
    CARD_ID uniqueidentifier,
    PROC_STAGE_ID uniqueidentifier,
    primary key nonclustered (ID)
)^

alter table WF_CARD_STAGE add constraint FK_WF_CARD_STAGE_PROC_STAGE foreign key (PROC_STAGE_ID) references WF_PROC_STAGE (ID)^
alter table WF_CARD_STAGE add constraint FK_WF_CARD_STAGE_CARD foreign key (CARD_ID) references WF_CARD (ID)^

create clustered index IDX_WF_CARD_STAGE_CARD on WF_CARD_STAGE (CARD_ID)^

------------------------------------------------------------------------------------------------------------

create table WF_PROC_STAGE_PROC_ROLE (
    PROC_STAGE_ID uniqueidentifier,
    PROC_ROLE_ID uniqueidentifier,
    primary key (PROC_STAGE_ID, PROC_ROLE_ID)
)^

alter table WF_PROC_STAGE_PROC_ROLE add constraint FK_WF_PSPR_PROC_STAGE foreign key (PROC_STAGE_ID) references WF_PROC_STAGE (ID)^
alter table WF_PROC_STAGE_PROC_ROLE add constraint FK_WF_PSPR_USER foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_PROC_STAGE_TYPE (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    --
    NAME varchar(200),
    CODE varchar(200),
    DURATION_SCRIPT_ENABLED tinyint,
    DURATION_SCRIPT text,
    --
    primary key (ID)
)^

alter table WF_PROC_STAGE add constraint FK_WF_PROC_STAGE_TYPE foreign key (PROC_STAGE_TYPE_ID) references WF_PROC_STAGE_TYPE (ID)^

------------------------------------------------------------------------------------------------------------

insert into WF_ATTACHMENTTYPE (ID, CODE, ISDEFAULT)
values ('6c9c8ccc-e761-11df-94cb-6f884bc56e70', 'AttachmentType.attachment', 1)^