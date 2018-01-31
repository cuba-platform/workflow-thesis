create table WF_DESIGN (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    NAME varchar(100),
    SRC longvarchar,
    DESIGN_TYPE varchar(50),
    NOTIFICATION_MATRIX longvarbinary,
    NOTIFICATION_MATRIX_UPLOADED boolean,
    LOCALIZATION longvarchar,
    COMPILE_TS timestamp,
    primary key (ID)
)^

------------------------------------------------------------------------------------------------------------

create table WF_DESIGN_SCRIPT (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    DESIGN_ID varchar(36),
    NAME varchar(100),
    CONTENT longvarchar,
    primary key (ID)
)^

alter table WF_DESIGN_SCRIPT add constraint FK_WF_DESIGN_SCRIPT_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_DESIGN_FILE (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    DESIGN_ID varchar(36),
    NAME varchar(100),
    DESIGN_FILE_TYPE varchar(20),
    CONTENT longvarchar,
    BINARY_CONTENT longvarbinary,
    primary key (ID)
)^

alter table WF_DESIGN_FILE add constraint FK_WF_DESIGN_FILE_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_PROC (
    ID varchar(36) not null,
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
    STATES varchar(1500),
    PERMISSIONS_ENABLED boolean,
    DESIGN_ID varchar(36),
    AVAILABLE_ROLE_ID varchar(36),
    COMBINED_STAGES_ENABLED boolean,
    DURATION_ENABLED boolean,
    primary key (ID)
)^

alter table WF_PROC add constraint FK_WF_PROC_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID)^
alter table WF_PROC add constraint WF_PROC_AVAILABLE_ROLE_ID foreign key (AVAILABLE_ROLE_ID) references SEC_ROLE(ID)^
alter table WF_PROC add constraint WF_PROC_UNIQ_CODE unique (CODE)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    VERSION integer,
    CARD_TYPE integer,
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
    PARENT_CARD_ACCESS boolean,
    primary key (ID)
)^

alter table WF_CARD add constraint FK_WF_CARD_PROC foreign key (PROC_ID) references WF_PROC (ID)^
alter table WF_CARD add constraint FK_WF_CARD_USER foreign key (CREATOR_ID) references SEC_USER (ID)^
alter table WF_CARD add constraint FK_WF_CARD_CARD foreign key (PARENT_CARD_ID) references WF_CARD (ID)^
alter table WF_CARD add constraint FK_WF_CARD_SUBSTITUTED_CREATOR foreign key (SUBSTITUTED_CREATOR_ID) references SEC_USER (ID)^
alter table WF_CARD add constraint FK_WF_CARD_CATEGORY_ID foreign key (CATEGORY_ID) references SYS_CATEGORY(ID)^
alter table WF_CARD add constraint WF_CARD_FAMILY_CARD foreign key (FAMILY_CARD_ID) references WF_CARD(ID)^
create index IDX_WF_CARD_FAMILY_CARD on WF_CARD(FAMILY_CARD_ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_COMMENT (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_COMMENT longvarchar,
    USER_ID varchar(36),
    SUBSTITUTED_USER_ID varchar(36),
    CARD_ID varchar(36),
    PARENT_ID varchar(36),
    primary key (ID)
)^

alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_USER foreign key (USER_ID) references SEC_USER (ID)^
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_CARD foreign key (CARD_ID) references WF_CARD (ID)^
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_PARENT foreign key (PARENT_ID) references WF_CARD_COMMENT (ID)^
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_SUB_USER foreign key (SUBSTITUTED_USER_ID) references SEC_USER (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_COMMENT_USER (
    CARD_COMMENT_ID varchar(36),
    USER_ID varchar(36),
    primary key (CARD_COMMENT_ID, USER_ID)
)^

alter table WF_CARD_COMMENT_USER add constraint FK_WF_CCU_CARD_COMMENT foreign key (CARD_COMMENT_ID) references WF_CARD_COMMENT (ID)^
alter table WF_CARD_COMMENT_USER add constraint FK_WF_CCU_USER foreign key (USER_ID) references SEC_USER (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_RELATION (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_ID varchar(36),
    RELATED_CARD_ID varchar(36),
    primary key (ID)
)^

alter table WF_CARD_RELATION add constraint FK_WF_CC_CARD foreign key (CARD_ID) references WF_CARD (ID)^
alter table WF_CARD_RELATION add constraint FK_WF_CC_CARD_RELATED foreign key (RELATED_CARD_ID) references WF_CARD (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_INFO (
    ID varchar(36) not null,
    NAME varchar(50),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_ID varchar(36),
    CARD_INFO_TYPE integer,
    USER_ID varchar(36),
    JBPM_EXECUTION_ID varchar(255),
    ACTIVITY varchar(255),
    DESCRIPTION longvarchar,
    primary key (ID)
)^

alter table WF_CARD_INFO add constraint FK_WF_CARD_INFO_CARD foreign key (CARD_ID) references WF_CARD(ID)^
alter table WF_CARD_INFO add constraint FK_WF_CARD_INFO_USER foreign key (USER_ID) references SEC_USER(ID)^

------------------------------------------------------------------------------------------------------------

create table WF_ASSIGNMENT (
    ID varchar(36) not null,
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
    ASSIGNMENT_COMMENT longvarchar,
    ITERATION integer,
    SUBPROC_CARD_ID varchar(36),
    FAMILY_ASSIGNMENT_ID varchar(36),
    CARD_ROLE_ID varchar(36),
    primary key (ID)
)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_USER foreign key (USER_ID) references SEC_USER (ID)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_FINISHED_BY foreign key (FINISHED_BY) references SEC_USER (ID)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_PROC foreign key (PROC_ID) references WF_PROC (ID)^

alter table WF_ASSIGNMENT add constraint WF_ASSIGNMENT_FAMILY_ASS foreign key (FAMILY_ASSIGNMENT_ID) references WF_ASSIGNMENT(ID)^

alter table WF_ASSIGNMENT add constraint WF_ASSIGNMENT_SUBPROC_CARD foreign key (SUBPROC_CARD_ID) references WF_CARD(ID)^

------------------------------------------------------------------------------------------------------------

create table WF_ATTACHMENTTYPE (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    VERSION integer,
    NAME varchar(500),
    ATTACHMENTTYPE_COMMENT varchar(1000),
    ISDEFAULT boolean,
    CODE varchar(200),
    ISSYSTEM boolean,
    primary key (ID)
)^

------------------------------------------------------------------------------------------------------------

create table WF_ATTACHMENT (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    ATTACHMENT_TYPE char(1),
    FILE_ID varchar(36),
    TYPE_ID varchar(36),
    NAME varchar(500),
    ATTACHMENT_COMMENT longvarchar,
    SIGNATURES longvarchar,
    CARD_ID varchar(36),
    ASSIGNMENT_ID varchar(36),
    VERSION_OF_ID varchar(36),
    VERSION_NUM integer,
    RECOGNIZED_FILE_ID varchar(36),
    SUBSTITUTED_CREATOR_ID varchar(36),
    primary key (ID)
)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_FILE foreign key (FILE_ID) references SYS_FILE (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_ASSIGNMENT foreign key (ASSIGNMENT_ID) references WF_ASSIGNMENT (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_TYPE foreign key (TYPE_ID) references WF_ATTACHMENTTYPE (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_ATTACHMENT foreign key (VERSION_OF_ID) references WF_ATTACHMENT (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_RECOGNIZED_FILE foreign key (RECOGNIZED_FILE_ID) references SYS_FILE (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_USER foreign key (SUBSTITUTED_CREATOR_ID) references SEC_USER (ID)^

insert into WF_ATTACHMENTTYPE (ID,CODE,ISDEFAULT)
values ('6c9c8ccc-e761-11df-94cb-6f884bc56e70','AttachmentType.attachment',true)^

------------------------------------------------------------------------------------------------------------

create table WF_PROC_ROLE (
    ID varchar(36) not null,
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
    IS_MULTI_USER boolean,
    INVISIBLE boolean,
    ROLE_ID varchar(36),
    ASSIGN_TO_CREATOR boolean,
    SORT_ORDER integer,
    ORDER_FILLING_TYPE varchar(1),
    primary key (ID)
)^

alter table WF_PROC_ROLE add constraint FK_WF_PROC_ROLE_PROC foreign key (PROC_ID) references WF_PROC (ID)^
alter table WF_PROC_ROLE add constraint FK_WF_PROC_ROLE_ROLE foreign key (ROLE_ID) references SEC_ROLE (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_ROLE (
    ID varchar(36) not null,
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
    NOTIFY_BY_EMAIL boolean,
    NOTIFY_BY_CARD_INFO boolean,
    SORT_ORDER integer,
    DURATION integer,
    TIME_UNIT varchar(1),
    primary key (ID)
)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_PROC_ROLE foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_USER foreign key (USER_ID) references SEC_USER (ID)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_CARD_ROLE foreign key (CARD_ROLE_ID) references WF_CARD_ROLE (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_PROC (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_ID varchar(36),
    PROC_ID varchar(36),
    IS_ACTIVE boolean,
    START_COUNT integer,
    STATE varchar(255),
    SORT_ORDER integer,
    JBPM_PROCESS_ID varchar(255),
    primary key (ID)
)^

alter table WF_CARD_PROC add constraint FK_WF_CARD_PROC_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_CARD_PROC add constraint FK_WF_CARD_PROC_PROC foreign key (PROC_ID) references WF_PROC (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_DEFAULT_PROC_ACTOR (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    PROC_ROLE_ID varchar(36),
    USER_ID varchar(36),
    NOTIFY_BY_EMAIL boolean,
    SORT_ORDER integer,
    primary key (ID)
)^

alter table WF_DEFAULT_PROC_ACTOR add constraint FK_WF_DEFAULT_PROC_ACT_PRO_ROL foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID)^

alter table WF_DEFAULT_PROC_ACTOR add constraint FK_WF_DEFAULT_PROC_ACTOR_USER foreign key (USER_ID) references SEC_USER (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_TIMER (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    DUE_DATE timestamp,
    CARD_ID varchar(36),
    JBPM_EXECUTION_ID varchar(255),
    ACTIVITY varchar(255),
    ACTION_CLASS varchar(200),
    ACTION_PARAMS varchar(2000),
    FACTORY_CLASS longvarchar,
    primary key (ID)
)^

alter table WF_TIMER add constraint FK_WF_TIMER_CARD foreign key (CARD_ID) references WF_CARD (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CALENDAR (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    WORK_DAY date,
    WORK_DAY_OF_WEEK numeric(1),
    WORK_START_TIME time,
    WORK_END_TIME time,
    CALENDAR_COMMENT varchar(500),
    primary key (ID)
)^

------------------------------------------------------------------------------------------------------------

create table WF_USER_GROUP (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    VERSION integer,
    NAME varchar(255),
    GLOBAL boolean,
    SUBSTITUTED_CREATOR_ID varchar(36),
    primary key (ID)
)^

alter table WF_USER_GROUP add constraint WF_USER_GROUP_SEC_USER foreign key (SUBSTITUTED_CREATOR_ID) references SEC_USER(ID)^

------------------------------------------------------------------------------------------------------------

create table WF_USER_GROUP_USER (
    USER_GROUP_ID varchar(36) not null,
    USER_ID varchar(36),
    primary key (USER_GROUP_ID, USER_ID)
)^

alter table WF_USER_GROUP_USER add constraint FK_WF_UGU_USER_GROUP foreign key (USER_GROUP_ID) references WF_USER_GROUP (ID)^
alter table WF_USER_GROUP_USER add constraint FK_WF_UGU_USER foreign key (USER_ID) references SEC_USER (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_PROC_STATE (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),

    NAME varchar(200),
    PROC_ID varchar(36),

    primary key (ID)
)^

alter table WF_PROC_STATE add constraint FK_WF_PROC_STATE_PROC foreign key (PROC_ID) references WF_PROC (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_PROC_APP_FOLDER (
    FOLDER_ID varchar(36) not null,
    PROC_CONDITIONS_XML longvarchar,

    primary key (FOLDER_ID)
)^

alter table WF_PROC_APP_FOLDER add constraint FK_WF_PROC_APP_FOLDER_APP_FOL foreign key (FOLDER_ID) references SYS_APP_FOLDER (FOLDER_ID)^

------------------------------------------------------------------------------------------------------------

create table WF_SENDING_SMS
(
    ID varchar(36) not null,
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
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),

    USER_ID varchar(36),
    primary key (ID)
)^
alter table WF_USER_NOTIFIED_BY_SMS add constraint FK_WF_USER_NOTIFIED_BY_SMS_USE foreign key (USER_ID) references SEC_USER (ID)^

----- WF_DESIGN_PROCESS_VARIABLE creation -----
create table WF_DESIGN_PROCESS_VARIABLE (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    NAME varchar(100),
    META_CLASS_NAME varchar(255),
    ALIAS varchar(100),
    PROPERTY_NAME varchar(100),
    VALUE longvarchar,
    MODULE_NAME varchar(255),
    ATTRIBUTE_TYPE varchar(25),
    OVERRIDDEN boolean default false,
    VARIABLE_COMMENT longvarchar,
    SHOULD_BE_OVERRIDDEN boolean default false,
    DESIGN_ID varchar(36),
    primary key (ID)
)^

alter table WF_DESIGN_PROCESS_VARIABLE add constraint FK_WF_DES_PROC_VAR_TO_WF_DES
foreign key (DESIGN_ID) references WF_DESIGN(ID)^

----- WF_PROC_VARIABLE creation -----
create table WF_PROC_VARIABLE (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    NAME varchar(100),
    META_CLASS_NAME varchar(255),
    ALIAS varchar(100),
    PROPERTY_NAME varchar(100),
    VALUE longvarchar,
    MODULE_NAME varchar(255),
    ATTRIBUTE_TYPE varchar(25),
    OVERRIDDEN boolean default false,
    VARIABLE_COMMENT longvarchar,
    PROC_ID varchar(36),
    primary key (ID)
)^

alter table WF_PROC_VARIABLE add constraint FK_WF_PROC_VARIABLE_TO_WF_PROC
foreign key (PROC_ID) references WF_PROC(ID)^

----- WF_CARD_VARIABLE creation -----
create table WF_CARD_VARIABLE (
    ID varchar(36) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    NAME varchar(100),
    META_CLASS_NAME varchar(255),
    ALIAS varchar(100),
    PROPERTY_NAME varchar(100),
    VALUE longvarchar,
    MODULE_NAME varchar(255),
    ATTRIBUTE_TYPE varchar(25),
    OVERRIDDEN boolean default false,
    VARIABLE_COMMENT longvarchar,
    CARD_ID varchar(36),
    primary key (ID)
)^

alter table WF_CARD_VARIABLE add constraint FK_WF_CARD_VARIABLE_TO_WF_CARD
foreign key (CARD_ID) references WF_CARD(ID)^

create index IDX_WF_CARD_VARIABLE on WF_CARD_VARIABLE(CARD_ID)^

