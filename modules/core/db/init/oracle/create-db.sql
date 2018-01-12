create table WF_ASSIGNMENT (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    USER_ID varchar2(32),
    CARD_ID varchar2(32),
    PROC_ID varchar2(32),
    MASTER_ASSIGNMENT_ID varchar2(32),
    NAME varchar2(255),
    DESCRIPTION varchar2(1000),
    JBPM_PROCESS_ID varchar2(255),
    DUE_DATE timestamp,
    FINISHED timestamp,
    FINISHED_BY varchar2(32),
    OUTCOME varchar2(255),
    ASSIGNMENT_COMMENT clob,
    ITERATION integer,
    SUBPROC_CARD_ID varchar2(32),
    FAMILY_ASSIGNMENT_ID varchar2(32),
    CARD_ROLE_ID varchar2(32),
    primary key(ID)
)^
create index IDX_WF_ASSIGNMENT_CARD on WF_ASSIGNMENT(CARD_ID)^
create index IDX_WF_ASSIGNMENT_USER on WF_ASSIGNMENT(USER_ID)^
create index IDX_WF_ASSIGNMENT_CARD_ROLE on WF_ASSIGNMENT(CARD_ROLE_ID)^
create index IDX_WF_ASSIGNMENT_USER_FIN on WF_ASSIGNMENT(USER_ID, FINISHED)^
create index IDX_WF_ASSIGNMENT_MASTER on WF_ASSIGNMENT(MASTER_ASSIGNMENT_ID)^

create table WF_ATTACHMENT (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    ATTACHMENT_TYPE char(1),
    FILE_ID varchar2(32),
    TYPE_ID varchar2(32),
    NAME varchar2(500),
    ATTACHMENT_COMMENT varchar2(1000),
    SIGNATURES clob,
    CARD_ID varchar2(32),
    ASSIGNMENT_ID varchar2(32),
    VERSION_OF_ID varchar2(32),
    RECOGNIZED_FILE_ID varchar2(32),
    VERSION_NUM integer,
    SUBSTITUTED_CREATOR_ID varchar2(32),
    primary key(ID)
)^
create index IDX_WF_ATTACHMENT_ASSIGNMENT on WF_ATTACHMENT(ASSIGNMENT_ID)^
create index IDX_WF_ATTACHMENT_CARD on WF_ATTACHMENT(CARD_ID)^

create table WF_ATTACHMENTTYPE (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    VERSION integer,
    NAME varchar2(500),
    ATTACHMENTTYPE_COMMENT varchar2(1000),
    ISDEFAULT char(1),
    CODE varchar2(200),
    ISSYSTEM char(1),
    primary key(ID)
)^
create unique index IDX_WF_ATTACHMENTTYPE_UNIQ_COD on WF_ATTACHMENTTYPE(CODE, DELETE_TS)^

create table WF_CALENDAR (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    WORK_DAY date,
    WORK_DAY_OF_WEEK numeric,
    WORK_START_TIME timestamp,
    WORK_END_TIME timestamp,
    CALENDAR_COMMENT varchar2(500),
    primary key(ID)
)^
create index IDX_WF_CALENDAR_WORK_DAY on WF_CALENDAR(WORK_DAY)^

create table WF_CARD (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    VERSION integer,
    CARD_TYPE integer,
    PROC_ID varchar2(32),
    JBPM_PROCESS_ID varchar2(255),
    STATE varchar2(255),
    DESCRIPTION varchar2(1000),
    CREATOR_ID varchar2(32),
    SUBSTITUTED_CREATOR_ID varchar2(32),
    PARENT_CARD_ID varchar2(32),
    HAS_ATTACHMENTS char(1),
    HAS_ATTRIBUTES char(1),
    CATEGORY_ID varchar2(32),
    FAMILY_CARD_ID varchar2(32),
    FAMILY_JBPM_PROCESS_ID varchar2(255),
    PARENT_CARD_ACCESS char(1),
    primary key(ID)
)^
create index IDX_WF_CARD_FAMILY_CARD on WF_CARD(FAMILY_CARD_ID)^

create table WF_CARD_COMMENT (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    CARD_COMMENT clob,
    USER_ID varchar2(32),
    SUBSTITUTED_USER_ID varchar2(32),
    CARD_ID varchar2(32),
    PARENT_ID varchar2(32),
    primary key(ID)
)^
create index IDX_WF_CARD_COMMENT_CARD on WF_CARD_COMMENT(CARD_ID)^

create table WF_CARD_COMMENT_USER (
    CARD_COMMENT_ID varchar2(32) not null,
    USER_ID varchar2(32) not null,
    primary key(CARD_COMMENT_ID, USER_ID)
)^

create table WF_CARD_INFO (
    ID varchar2(32) not null,
    NAME varchar2(50),
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    CARD_ID varchar2(32),
    CARD_INFO_TYPE integer,
    USER_ID varchar2(32),
    JBPM_EXECUTION_ID varchar2(255),
    ACTIVITY varchar2(255),
    DESCRIPTION clob,
    primary key(ID)
)^
create index IDX_WF_CARD_INFO_CARD on WF_CARD_INFO(CARD_ID)^
create index IDX_WF_CARD_INFO_USER on WF_CARD_INFO(USER_ID, DELETE_TS)^

create table WF_CARD_PROC (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    CARD_ID varchar2(32),
    PROC_ID varchar2(32),
    IS_ACTIVE char(1),
    START_COUNT integer,
    STATE varchar2(255),
    SORT_ORDER integer,
    JBPM_PROCESS_ID varchar2(255),
    primary key(ID)
)^
create index IDX_WF_CARD_PROC_CARD on WF_CARD_PROC(CARD_ID)^

create table WF_CARD_RELATION (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    CARD_ID varchar2(32),
    RELATED_CARD_ID varchar2(32),
    primary key(ID)
)^
create index IDX_WF_CARD_RELATION_CARD on WF_CARD_RELATION(CARD_ID)^
create index IDX_WF_CARD_RELATION_REL_CAR on WF_CARD_RELATION(RELATED_CARD_ID)^

create table WF_CARD_ROLE (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    CARD_ID varchar2(32),
    PROC_ROLE_ID varchar2(32),
    CODE varchar2(50),
    USER_ID varchar2(32),
    NOTIFY_BY_EMAIL char(1),
    NOTIFY_BY_CARD_INFO char(1),
    SORT_ORDER integer,
    DURATION integer,
    TIME_UNIT varchar2(1),
    primary key(ID)
)^
create index IDX_WF_CARD_ROLE_CARD on WF_CARD_ROLE(CARD_ID)^
create index IDX_WF_CARD_ROLE_USER_CODE on WF_CARD_ROLE(USER_ID, CODE)^

create table WF_DEFAULT_PROC_ACTOR (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    PROC_ROLE_ID varchar2(32),
    USER_ID varchar2(32),
    NOTIFY_BY_EMAIL char(1),
    SORT_ORDER integer,
    primary key(ID)
)^
create index IDX_WF_DEFAULT_PRO_ACT_PRO_ROL on WF_DEFAULT_PROC_ACTOR(PROC_ROLE_ID)^

create table WF_DESIGN (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    NAME varchar2(100),
    SRC clob,
    NOTIFICATION_MATRIX blob,
    NOTIFICATION_MATRIX_UPLOADED char(1),
    LOCALIZATION clob,
    COMPILE_TS timestamp,
    DESIGN_TYPE varchar2(50),
    primary key(ID)
)^

create table WF_DESIGN_FILE (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    DESIGN_ID varchar2(32),
    NAME varchar2(100),
    DESIGN_FILE_TYPE varchar2(20),
    CONTENT clob,
    BINARY_CONTENT blob,
    primary key(ID)
)^
create index IDX_WF_DESIGN_FILE_DESIGN on WF_DESIGN_FILE(DESIGN_ID)^

create table WF_DESIGN_SCRIPT (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    DESIGN_ID varchar2(32),
    NAME varchar2(100),
    CONTENT clob,
    primary key(ID)
)^
create index IDX_WF_DESIGN_SCRIPT_DESIGN on WF_DESIGN_SCRIPT(DESIGN_ID)^

create table WF_PROC (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    NAME varchar2(255),
    JBPM_PROCESS_KEY varchar2(255),
    CODE varchar2(255),
    MESSAGES_PACK varchar2(200),
    CARD_TYPES varchar2(500),
    STATES varchar2(1500),
    PERMISSIONS_ENABLED char(1),
    DESIGN_ID varchar2(32),
    AVAILABLE_ROLE_ID varchar2(32),
    COMBINED_STAGES_ENABLED char(1),
    DURATION_ENABLED char(1),
    primary key(ID)
)^
create unique index IDX_WF_PROC_UNIQ_CODE on WF_PROC(CODE, DELETE_TS)^

create table WF_PROC_APP_FOLDER (
    FOLDER_ID varchar2(32) not null,
    PROC_CONDITIONS_XML clob,
    primary key(FOLDER_ID)
)^

create table WF_PROC_ROLE (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    PROC_ID varchar2(32),
    CODE varchar2(50),
    NAME varchar2(100),
    IS_MULTI_USER char(1),
    INVISIBLE char(1),
    ROLE_ID varchar2(32),
    ASSIGN_TO_CREATOR char(1),
    SORT_ORDER integer,
    ORDER_FILLING_TYPE varchar2(1),
    primary key(ID)
)^
create index IDX_WF_PROC_ROLE_PROC on WF_PROC_ROLE(PROC_ID)^

create table WF_PROC_STATE (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    NAME varchar2(200),
    PROC_ID varchar2(32),
    primary key(ID)
)^

create table WF_SENDING_SMS (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    SMS_ID varchar2(255),
    PHONE varchar2(50),
    MESSAGE varchar2(255),
    ERROR_CODE integer,
    STATUS integer,
    LAST_CHANGE_DATE timestamp,
    ATTEMPTS_COUNT integer,
    ADDRESSEE varchar2(200),
    START_SENDING_DATE timestamp,
    primary key(ID)
)^

create table WF_TIMER (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    DUE_DATE timestamp,
    CARD_ID varchar2(32),
    JBPM_EXECUTION_ID varchar2(255),
    ACTIVITY varchar2(255),
    ACTION_CLASS varchar2(200),
    ACTION_PARAMS varchar2(2000),
    FACTORY_CLASS clob,
    primary key(ID)
)^
create index IDX_WF_TIMER_CARD on WF_TIMER(CARD_ID)^
create index IDX_WF_TIMER_DUE_DATE on WF_TIMER(DUE_DATE)^
create index IDX_WF_TIMER_EXECUTION_ACT on WF_TIMER(JBPM_EXECUTION_ID, ACTIVITY)^

create table WF_USER_GROUP (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    VERSION integer,
    NAME varchar2(255),
    GLOBAL char(1),
    SUBSTITUTED_CREATOR_ID varchar2(32),
    primary key(ID)
)^

create table WF_USER_GROUP_USER (
    USER_GROUP_ID varchar2(32) not null,
    USER_ID varchar2(32) not null,
    primary key(USER_GROUP_ID, USER_ID)
)^

create table WF_USER_NOTIFIED_BY_SMS (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    USER_ID varchar2(32) not null,
    primary key(ID)
)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_FINISHED_BY foreign key (FINISHED_BY) references SEC_USER(ID)^
alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_USER foreign key (USER_ID) references SEC_USER(ID)^
alter table WF_ASSIGNMENT add constraint WF_ASSIGNMENT_FAMILY_ASS foreign key (FAMILY_ASSIGNMENT_ID) references WF_ASSIGNMENT(ID)^
alter table WF_ASSIGNMENT add constraint WF_ASSIGNMENT_SUBPROC_CARD foreign key (SUBPROC_CARD_ID) references WF_CARD(ID)^
alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_CARD foreign key (CARD_ID) references WF_CARD(ID)^
alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_PROC foreign key (PROC_ID) references WF_PROC(ID)^
alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_CARD_ROLE foreign key (CARD_ROLE_ID) references WF_CARD_ROLE(ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_FILE foreign key (FILE_ID) references SYS_FILE(ID)^
alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_ASSIGNMENT foreign key (ASSIGNMENT_ID) references WF_ASSIGNMENT(ID)^
alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_ATTACHMENT foreign key (VERSION_OF_ID) references WF_ATTACHMENT(ID)^
alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_TYPE foreign key (TYPE_ID) references WF_ATTACHMENTTYPE(ID)^
alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_CARD foreign key (CARD_ID) references WF_CARD(ID)^
alter table WF_ATTACHMENT add constraint FK_WF_RECOGNIZED_FILE foreign key (RECOGNIZED_FILE_ID) references SYS_FILE(ID)^
alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_USER foreign key (SUBSTITUTED_CREATOR_ID) references SEC_USER (ID)^

alter table WF_CARD add constraint FK_WF_CARD_SUBSTITUTED_CREATOR foreign key (SUBSTITUTED_CREATOR_ID) references SEC_USER(ID)^
alter table WF_CARD add constraint FK_WF_CARD_USER foreign key (CREATOR_ID) references SEC_USER(ID)^
alter table WF_CARD add constraint FK_WF_CARD_CATEGORY_ID foreign key (CATEGORY_ID) references SYS_CATEGORY(ID)^
alter table WF_CARD add constraint WF_CARD_FAMILY_CARD foreign key (FAMILY_CARD_ID) references WF_CARD(ID)^
alter table WF_CARD add constraint FK_WF_CARD_CARD foreign key (PARENT_CARD_ID) references WF_CARD(ID)^
alter table WF_CARD add constraint FK_WF_CARD_PROC foreign key (PROC_ID) references WF_PROC(ID)^

alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_USER foreign key (USER_ID) references SEC_USER(ID)^
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_CARD foreign key (CARD_ID) references WF_CARD(ID)^
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_PARENT foreign key (PARENT_ID) references WF_CARD_COMMENT(ID)^
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_SUB_USER foreign key (SUBSTITUTED_USER_ID) references SEC_USER (ID)^

alter table WF_CARD_COMMENT_USER add constraint FK_WF_CCU_USER foreign key (USER_ID) references SEC_USER(ID)^
alter table WF_CARD_COMMENT_USER add constraint FK_WF_CCU_CARD_COMMENT foreign key (CARD_COMMENT_ID) references WF_CARD_COMMENT(ID)^

alter table WF_CARD_INFO add constraint FK_WF_CARD_INFO_USER foreign key (USER_ID) references SEC_USER(ID)^
alter table WF_CARD_INFO add constraint FK_WF_CARD_INFO_CARD foreign key (CARD_ID) references WF_CARD(ID)^

alter table WF_CARD_PROC add constraint FK_WF_CARD_PROC_CARD foreign key (CARD_ID) references WF_CARD(ID)^
alter table WF_CARD_PROC add constraint FK_WF_CARD_PROC_PROC foreign key (PROC_ID) references WF_PROC(ID)^

alter table WF_CARD_RELATION add constraint FK_WF_CC_CARD_RELATED foreign key (RELATED_CARD_ID) references WF_CARD(ID)^
alter table WF_CARD_RELATION add constraint FK_WF_CC_CARD foreign key (CARD_ID) references WF_CARD(ID)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_USER foreign key (USER_ID) references SEC_USER(ID)^
alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_CARD foreign key (CARD_ID) references WF_CARD(ID)^
alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_ROLE foreign key (PROC_ROLE_ID) references WF_PROC_ROLE(ID)^

alter table WF_DEFAULT_PROC_ACTOR add constraint FK_WF_DEFAULT_PROC_ACTOR_USER foreign key (USER_ID) references SEC_USER(ID)^
alter table WF_DEFAULT_PROC_ACTOR add constraint FK_WF_DEFAULT_PROC_ACT_PRO_ROL foreign key (PROC_ROLE_ID) references WF_PROC_ROLE(ID)^

alter table WF_DESIGN_FILE add constraint FK_WF_DESIGN_FILE_DESIGN foreign key (DESIGN_ID) references WF_DESIGN(ID)^

alter table WF_DESIGN_SCRIPT add constraint FK_WF_DESIGN_SCRIPT_DESIGN foreign key (DESIGN_ID) references WF_DESIGN(ID)^

alter table WF_PROC add constraint WF_PROC_AVAILABLE_ROLE_ID foreign key (AVAILABLE_ROLE_ID) references SEC_ROLE(ID)^
alter table WF_PROC add constraint FK_WF_PROC_DESIGN foreign key (DESIGN_ID) references WF_DESIGN(ID)^

alter table WF_PROC_APP_FOLDER add constraint FK_WF_PROC_APP_FOLDER_APP_FOL foreign key (FOLDER_ID) references SYS_APP_FOLDER(FOLDER_ID)^

alter table WF_PROC_ROLE add constraint FK_WF_PROC_ROLE_ROLE foreign key (ROLE_ID) references SEC_ROLE(ID)^
alter table WF_PROC_ROLE add constraint FK_WF_PROC_ROLE_PROC foreign key (PROC_ID) references WF_PROC(ID)^

alter table WF_PROC_STATE add constraint FK_WF_PROC_STATE_PROC foreign key (PROC_ID) references WF_PROC(ID)^

alter table WF_TIMER add constraint FK_WF_TIMER_CARD foreign key (CARD_ID) references WF_CARD(ID)^

alter table WF_USER_GROUP add constraint WF_USER_GROUP_SEC_USER foreign key (SUBSTITUTED_CREATOR_ID) references SEC_USER(ID)^

alter table WF_USER_GROUP_USER add constraint FK_WF_UGU_USER foreign key (USER_ID) references SEC_USER(ID)^
alter table WF_USER_GROUP_USER add constraint FK_WF_UGU_USER_GROUP foreign key (USER_GROUP_ID) references WF_USER_GROUP(ID)^

alter table WF_USER_NOTIFIED_BY_SMS add constraint FK_WF_USER_NOTIFIED_BY_SMS_USE foreign key (USER_ID) references SEC_USER(ID)^

------------------------------------------------------------------------------------------------------------

insert into WF_ATTACHMENTTYPE (ID, CODE, ISDEFAULT)
values ('6c9c8ccce76111df94cb6f884bc56e70', 'AttachmentType.attachment', 1)^

----- WF_DESIGN_PROCESS_VARIABLE creation -----
create table WF_DESIGN_PROCESS_VARIABLE (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    DESIGN_ID varchar2(32),
    NAME varchar2(100),
    META_CLASS_NAME varchar2(255),
    ALIAS varchar2(100),
    PROPERTY_NAME varchar2(100),
    VALUE clob,
    MODULE_NAME varchar2(255),
    SHOULD_BE_OVERRIDDEN char(1),
    ATTRIBUTE_TYPE varchar2(25),
    OVERRIDDEN char(1),
    VARIABLE_COMMENT clob,
    primary key(ID)
)^

alter table WF_DESIGN_PROCESS_VARIABLE add constraint FK_WF_DES_PROC_VAR_TO_WF_DES
foreign key (DESIGN_ID) references WF_DESIGN(ID)^

----- WF_PROC_VARIABLE creation -----
create table WF_PROC_VARIABLE (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    PROC_ID varchar2(32),
    NAME varchar2(100),
    META_CLASS_NAME varchar2(255),
    ALIAS varchar2(100),
    PROPERTY_NAME varchar2(100),
    VALUE clob,
    MODULE_NAME varchar2(255),
    ATTRIBUTE_TYPE varchar2(25),
    OVERRIDDEN char(1),
    VARIABLE_COMMENT clob,
    primary key (ID)
)^

alter table WF_PROC_VARIABLE add constraint FK_WF_PROC_VARIABLE_TO_WF_PROC
foreign key (PROC_ID) references WF_PROC(ID)^

----- WF_CARD_VARIABLE creation -----
create table WF_CARD_VARIABLE (
    ID varchar2(32) not null,
    CREATE_TS timestamp,
    CREATED_BY varchar2(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar2(50),
    DELETE_TS timestamp,
    DELETED_BY varchar2(50),
    CARD_ID varchar2(32),
    NAME varchar2(100),
    META_CLASS_NAME varchar2(255),
    ALIAS varchar2(100),
    PROPERTY_NAME varchar2(100),
    VALUE clob,
    MODULE_NAME varchar2(255),
    ATTRIBUTE_TYPE varchar2(25),
    OVERRIDDEN char(1),
    VARIABLE_COMMENT clob,
    primary key (ID)
)^

alter table WF_CARD_VARIABLE add constraint FK_WF_CARD_VARIABLE_TO_WF_CARD
foreign key (CARD_ID) references WF_CARD(ID)^

create index IDX_WF_CARD_VARIABLE on WF_CARD_VARIABLE(CARD_ID)^
