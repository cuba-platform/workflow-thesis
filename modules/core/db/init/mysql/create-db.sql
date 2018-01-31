create table WF_DESIGN (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    NAME varchar(100),
    SRC text,
    NOTIFICATION_MATRIX blob,
    NOTIFICATION_MATRIX_UPLOADED boolean,
    LOCALIZATION text,
    DESIGN_TYPE varchar(50),
    COMPILE_TS datetime(3),
    primary key (ID)
)^

/**********************************************************************************************/

create table WF_DESIGN_SCRIPT (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    DESIGN_ID varchar(32),
    NAME varchar(100),
    CONTENT text,
    primary key (ID)
)^

alter table WF_DESIGN_SCRIPT add constraint FK_WF_DESIGN_SCRIPT_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID)^

create index IDX_WF_DESIGN_SCRIPT_DESIGN on WF_DESIGN_SCRIPT (DESIGN_ID)^

/**********************************************************************************************/

create table WF_DESIGN_FILE (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    DESIGN_ID varchar(32),
    NAME varchar(100),
    DESIGN_FILE_TYPE varchar(20),
    CONTENT text,
    BINARY_CONTENT blob,
    primary key (ID)
)^

alter table WF_DESIGN_FILE add constraint FK_WF_DESIGN_FILE_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID)^

create index IDX_WF_DESIGN_FILE_DESIGN on WF_DESIGN_FILE (DESIGN_ID)^

/**********************************************************************************************/

create table WF_PROC (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    DELETE_TS_NN datetime(3) not null default '1000-01-01 00:00:00.000',
    NAME varchar(255),
    JBPM_PROCESS_KEY varchar(255),
    CODE varchar(190) ,
    MESSAGES_PACK varchar(200),
    CARD_TYPES varchar(500),
    STATES varchar(1500),
    PERMISSIONS_ENABLED boolean,
    DESIGN_ID varchar(32),
    AVAILABLE_ROLE_ID varchar(32),
    COMBINED_STAGES_ENABLED boolean,
    DURATION_ENABLED boolean,
    primary key (ID)
)^

alter table WF_PROC add constraint FK_WF_PROC_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID)^
alter table WF_PROC add constraint WF_PROC_AVAILABLE_ROLE_ID foreign key (AVAILABLE_ROLE_ID) references SEC_ROLE(ID)^

create unique index IDX_WF_PROC_UNIQ_CODE on WF_PROC (CODE, DELETE_TS_NN)^

create trigger WF_PROC_DELETE_TS_NN_TRIGGER before update on WF_PROC
for each row
	if not(NEW.DELETE_TS <=> OLD.DELETE_TS) then
		set NEW.DELETE_TS_NN = if (NEW.DELETE_TS is null, '1000-01-01 00:00:00.000', NEW.DELETE_TS);
	end if^

/**********************************************************************************************/

create table WF_CARD (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    VERSION integer,
    CARD_TYPE integer,
    PROC_ID varchar(32),
    JBPM_PROCESS_ID varchar(255),
    STATE varchar(255),
    DESCRIPTION varchar(1000),
    CREATOR_ID varchar(32),
    SUBSTITUTED_CREATOR_ID varchar(32),
    PARENT_CARD_ID varchar(32),
    HAS_ATTACHMENTS boolean,
    HAS_ATTRIBUTES boolean,
    CATEGORY_ID varchar(32),
    FAMILY_CARD_ID varchar(32),
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

/**********************************************************************************************/

create table WF_CARD_COMMENT (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    CARD_COMMENT text,
    USER_ID varchar(32),
    SUBSTITUTED_USER_ID varchar(32),
    CARD_ID varchar(32),
    PARENT_ID varchar(32),
    primary key (ID)
)^

alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_USER foreign key (USER_ID) references SEC_USER (ID)^
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_CARD foreign key (CARD_ID) references WF_CARD (ID)^
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_PARENT foreign key (PARENT_ID) references WF_CARD_COMMENT (ID)^
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_SUB_USER foreign key (SUBSTITUTED_USER_ID) references SEC_USER (ID)^

create index IDX_WF_CARD_COMMENT_CARD on WF_CARD_COMMENT (CARD_ID)^

/**********************************************************************************************/

create table WF_CARD_COMMENT_USER (
    CARD_COMMENT_ID varchar(32),
    USER_ID varchar(32),
    primary key (CARD_COMMENT_ID, USER_ID)
)^

alter table WF_CARD_COMMENT_USER add constraint FK_WF_CCU_CARD_COMMENT foreign key (CARD_COMMENT_ID) references WF_CARD_COMMENT (ID)^
alter table WF_CARD_COMMENT_USER add constraint FK_WF_CCU_USER foreign key (USER_ID) references SEC_USER (ID)^

/**********************************************************************************************/

create table WF_CARD_RELATION (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    CARD_ID varchar(32),
    RELATED_CARD_ID varchar(32),
    primary key (ID)
)^

alter table WF_CARD_RELATION add constraint FK_WF_CC_CARD foreign key (CARD_ID) references WF_CARD (ID)^
alter table WF_CARD_RELATION add constraint FK_WF_CC_CARD_RELATED foreign key (RELATED_CARD_ID) references WF_CARD (ID)^

create index IDX_WF_CARD_RELATION_CARD on WF_CARD_RELATION (CARD_ID)^

create index IDX_WF_CARD_RELATION_RELATED_CARD on WF_CARD_RELATION (RELATED_CARD_ID)^

/**********************************************************************************************/

create table WF_CARD_INFO (
    ID varchar(32),
    NAME varchar(50),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    CARD_ID varchar(32),
    CARD_INFO_TYPE integer,
    USER_ID varchar(32),
    JBPM_EXECUTION_ID varchar(255),
    ACTIVITY varchar(255),
    DESCRIPTION text,
    primary key (ID)
)^

alter table WF_CARD_INFO add constraint FK_WF_CARD_INFO_CARD foreign key (CARD_ID) references WF_CARD(ID)^
alter table WF_CARD_INFO add constraint FK_WF_CARD_INFO_USER foreign key (USER_ID) references SEC_USER(ID)^

create index IDX_WF_CARD_INFO_CARD on WF_CARD_INFO (CARD_ID)^

create index IDX_WF_CARD_INFO_USER on WF_CARD_INFO (USER_ID, DELETE_TS)^

/**********************************************************************************************/

create table WF_ASSIGNMENT (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    USER_ID varchar(32),
    CARD_ID varchar(32),
    PROC_ID varchar(32),
    MASTER_ASSIGNMENT_ID varchar(32),
    NAME varchar(255),
    DESCRIPTION varchar(1000),
    JBPM_PROCESS_ID varchar(255),
    DUE_DATE datetime(3),
    FINISHED datetime(3),
    FINISHED_BY varchar(36),
    OUTCOME varchar(255),
    ASSIGNMENT_COMMENT text,
    ITERATION integer,
    SUBPROC_CARD_ID varchar(32),
    FAMILY_ASSIGNMENT_ID varchar(32),
    CARD_ROLE_ID varchar(32),
    primary key (ID)
)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_USER foreign key (USER_ID) references SEC_USER (ID)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_FINISHED_BY foreign key (FINISHED_BY) references SEC_USER (ID)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_PROC foreign key (PROC_ID) references WF_PROC (ID)^

create index IDX_WF_ASSIGNMENT_CARD on WF_ASSIGNMENT (CARD_ID)^

create index IDX_WF_ASSIGNMENT_USER on WF_ASSIGNMENT (USER_ID)^

create index IDX_WF_ASSIGNMENT_USER_FINISHED on WF_ASSIGNMENT (USER_ID, FINISHED)^

create index IDX_WF_ASSIGNMENT_MASTER on WF_ASSIGNMENT(MASTER_ASSIGNMENT_ID)^

alter table WF_ASSIGNMENT add constraint WF_ASSIGNMENT_FAMILY_ASS foreign key (FAMILY_ASSIGNMENT_ID) references WF_ASSIGNMENT(ID)^

alter table WF_ASSIGNMENT add constraint WF_ASSIGNMENT_SUBPROC_CARD foreign key (SUBPROC_CARD_ID) references WF_CARD(ID)^

/**********************************************************************************************/

create table WF_ATTACHMENTTYPE (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    DELETE_TS_NN datetime(3) not null default '1000-01-01 00:00:00.000',
    VERSION integer,
    NAME varchar(500),
    ATTACHMENTTYPE_COMMENT varchar(1000),
    ISDEFAULT boolean,
    CODE varchar(190),
    ISSYSTEM boolean,
    primary key (ID)
)^

create unique index IDX_WF_ATTACHMENTTYPE_UNIQ_CODE on WF_ATTACHMENTTYPE (CODE, DELETE_TS_NN)^

create trigger WF_ATTACHMENTTYPE_DELETE_TS_NN_TRIGGER before update on WF_ATTACHMENTTYPE
for each row
	if not(NEW.DELETE_TS <=> OLD.DELETE_TS) then
		set NEW.DELETE_TS_NN = if (NEW.DELETE_TS is null, '1000-01-01 00:00:00.000', NEW.DELETE_TS);
	end if^

/**********************************************************************************************/

create table WF_ATTACHMENT (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    ATTACHMENT_TYPE char(1),
    FILE_ID varchar(32),
    TYPE_ID varchar(32),
    NAME varchar(500),
    ATTACHMENT_COMMENT varchar(1000),
    SIGNATURES text,
    CARD_ID varchar(32),
    ASSIGNMENT_ID varchar(32),
    VERSION_OF_ID varchar(32),
    VERSION_NUM integer,
    RECOGNIZED_FILE_ID varchar(32),
    SUBSTITUTED_CREATOR_ID varchar(32),
    primary key (ID)
)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_FILE foreign key (FILE_ID) references SYS_FILE (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_ASSIGNMENT foreign key (ASSIGNMENT_ID) references WF_ASSIGNMENT (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_TYPE foreign key (TYPE_ID) references WF_ATTACHMENTTYPE (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_ATTACHMENT foreign key (VERSION_OF_ID) references WF_ATTACHMENT (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_RECOGNIZED_FILE foreign key (RECOGNIZED_FILE_ID) references SYS_FILE (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_USER foreign key (SUBSTITUTED_CREATOR_ID) references SEC_USER (ID)^

create index IDX_WF_ATTACHMENT_CARD on WF_ATTACHMENT (CARD_ID)^

create index IDX_WF_ATTACHMENT_ASSIGNMENT on WF_ATTACHMENT (ASSIGNMENT_ID)^

/**********************************************************************************************/

create table WF_PROC_ROLE (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    PROC_ID varchar(32),
    CODE varchar(50),
    NAME varchar(100),
    IS_MULTI_USER boolean,
    INVISIBLE boolean,
    ROLE_ID varchar(32),
    ASSIGN_TO_CREATOR boolean,
    SORT_ORDER integer,
    ORDER_FILLING_TYPE varchar(1),
    primary key (ID)
)^

alter table WF_PROC_ROLE add constraint FK_WF_PROC_ROLE_PROC foreign key (PROC_ID) references WF_PROC (ID)^
alter table WF_PROC_ROLE add constraint FK_WF_PROC_ROLE_ROLE foreign key (ROLE_ID) references SEC_ROLE (ID)^

create index IDX_WF_PROC_ROLE_PROC on WF_PROC_ROLE (PROC_ID)^

/**********************************************************************************************/

create table WF_CARD_ROLE (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    CARD_ID varchar(32),
    PROC_ROLE_ID varchar(32),
    CODE varchar(50),
    USER_ID varchar(32),
    NOTIFY_BY_EMAIL boolean,
    NOTIFY_BY_CARD_INFO boolean,
    SORT_ORDER integer,
    DURATION integer,
    TIME_UNIT varchar(1),
    primary key (ID)
)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_ROLE foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_USER foreign key (USER_ID) references SEC_USER (ID)^

create index IDX_WF_CARD_ROLE_CARD on WF_CARD_ROLE (CARD_ID)^

create index IDX_WF_CARD_ROLE_USER_CODE on WF_CARD_ROLE (USER_ID, CODE)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_CARD_ROLE foreign key (USER_ID) references WF_CARD_ROLE (ID)^
create index IDX_WF_ASSIGNMENT_CARD_ROLE  on WF_ASSIGNMENT (CARD_ROLE_ID)^

/**********************************************************************************************/

create table WF_CARD_PROC (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    CARD_ID varchar(32),
    PROC_ID varchar(32),
    IS_ACTIVE boolean,
    START_COUNT integer,
    STATE varchar(255),
    SORT_ORDER integer,
    JBPM_PROCESS_ID varchar(255),
    primary key (ID)
)^

alter table WF_CARD_PROC add constraint FK_WF_CARD_PROC_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_CARD_PROC add constraint FK_WF_CARD_PROC_PROC foreign key (PROC_ID) references WF_PROC (ID)^

create index IDX_WF_CARD_PROC_CARD on WF_CARD_PROC (CARD_ID)^

/**********************************************************************************************/

create table WF_DEFAULT_PROC_ACTOR (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    PROC_ROLE_ID varchar(32),
    USER_ID varchar(32),
    NOTIFY_BY_EMAIL boolean,
    SORT_ORDER integer,
    primary key (ID)
)^

alter table WF_DEFAULT_PROC_ACTOR add constraint FK_WF_DEFAULT_PROC_ACT_PRO_ROL foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID)^

alter table WF_DEFAULT_PROC_ACTOR add constraint FK_WF_DEFAULT_PROC_ACTOR_USER foreign key (USER_ID) references SEC_USER (ID)^

create index IDX_WF_DEFAULT_PROC_ACTOR_PROC_ROLE on WF_DEFAULT_PROC_ACTOR (PROC_ROLE_ID)^

/**********************************************************************************************/

create table WF_TIMER (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    DUE_DATE datetime(3),
    CARD_ID varchar(32),
    JBPM_EXECUTION_ID varchar(190),
    ACTIVITY varchar(190),
    ACTION_CLASS varchar(200),
    ACTION_PARAMS varchar(2000),
    FACTORY_CLASS text,
    primary key (ID)
)^

alter table WF_TIMER add constraint FK_WF_TIMER_CARD foreign key (CARD_ID) references WF_CARD (ID)^

create index IDX_WF_TIMER_DUE_DATE on WF_TIMER (DUE_DATE)^

create index IDX_WF_TIMER_CARD on WF_TIMER (CARD_ID)^

-- create index IDX_WF_TIMER_EXECUTION_ACTIVITY on WF_TIMER (JBPM_EXECUTION_ID, ACTIVITY)^

/**********************************************************************************************/

create table WF_CALENDAR (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    WORK_DAY date,
    WORK_DAY_OF_WEEK numeric(1),
    WORK_START_TIME time,
    WORK_END_TIME time,
    CALENDAR_COMMENT varchar(500),
    primary key (ID)
)^

create index IDX_WF_CALENDAR_WORK_DAY on WF_CALENDAR (WORK_DAY)^

/**********************************************************************************************/

create table WF_USER_GROUP (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    VERSION integer,
    NAME varchar(255),
    GLOBAL boolean,
    SUBSTITUTED_CREATOR_ID varchar(32),
    primary key (ID)
)^

alter table WF_USER_GROUP add constraint WF_USER_GROUP_SEC_USER foreign key (SUBSTITUTED_CREATOR_ID) references SEC_USER(ID)^

/**********************************************************************************************/

create table WF_USER_GROUP_USER (
    USER_GROUP_ID varchar(32),
    USER_ID varchar(32),
    primary key (USER_GROUP_ID, USER_ID)
)^

alter table WF_USER_GROUP_USER add constraint FK_WF_UGU_USER_GROUP foreign key (USER_GROUP_ID) references WF_USER_GROUP (ID)^
alter table WF_USER_GROUP_USER add constraint FK_WF_UGU_USER foreign key (USER_ID) references SEC_USER (ID)^

/**********************************************************************************************/

create table WF_PROC_STATE (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),

    NAME varchar(200),
    PROC_ID varchar(32),

    primary key (ID)
)^

alter table WF_PROC_STATE add constraint FK_WF_PROC_STATE_PROC foreign key (PROC_ID) references WF_PROC (ID)^

/**********************************************************************************************/

create table WF_PROC_APP_FOLDER
(
    FOLDER_ID varchar(32) not null,
    PROC_CONDITIONS_XML text,

    primary key (FOLDER_ID)
)^

alter table WF_PROC_APP_FOLDER add constraint FK_WF_PROC_APP_FOLDER_APP_FOL foreign key (FOLDER_ID) references SYS_APP_FOLDER (FOLDER_ID)^

/**********************************************************************************************/

create table WF_SENDING_SMS
(
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),

    SMS_ID varchar(255),
    PHONE varchar(50),
    MESSAGE varchar(255),
    ERROR_CODE integer,
    STATUS integer,
    LAST_CHANGE_DATE datetime(3),
    ATTEMPTS_COUNT integer,
    ADDRESSEE varchar(200),
    START_SENDING_DATE datetime(3),
    primary key (ID)
)^
/**********************************************************************************************/

create table WF_USER_NOTIFIED_BY_SMS
(
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),

    USER_ID varchar(32) not null,
    primary key (ID)
)^
alter table WF_USER_NOTIFIED_BY_SMS add constraint FK_WF_USER_NOTIFIED_BY_SMS_USE foreign key (USER_ID) references SEC_USER (ID)^

/**********************************************************************************************/

insert into WF_ATTACHMENTTYPE (ID, CODE, ISDEFAULT)
values ('6c9c8ccce76111df94cb6f884bc56e70', 'AttachmentType.attachment', true)^

/**********************************************************************************************/
create table WF_DESIGN_PROCESS_VARIABLE (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    DESIGN_ID varchar(32),
    NAME varchar(100),
    META_CLASS_NAME varchar(255),
    ALIAS varchar(100),
    PROPERTY_NAME varchar(100),
    VALUE text,
    MODULE_NAME varchar(255),
    SHOULD_BE_OVERRIDDEN boolean default false,
    ATTRIBUTE_TYPE varchar(25),
    OVERRIDDEN boolean default false,
    VARIABLE_COMMENT text,
    primary key (ID)
)^

alter table WF_DESIGN_PROCESS_VARIABLE add constraint FK_WF_DES_PROC_VAR_TO_WF_DES
foreign key (DESIGN_ID) references WF_DESIGN(ID)^

/**********************************************************************************************/
create table WF_PROC_VARIABLE (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    NAME varchar(100),
    ALIAS varchar(100),
    PROPERTY_NAME varchar(100),
    VALUE text,
    MODULE_NAME varchar(255),
    ATTRIBUTE_TYPE varchar(25),
    META_CLASS_NAME varchar(255),
    PROC_ID varchar(32),
    OVERRIDDEN boolean default false,
    VARIABLE_COMMENT text,
    primary key (ID)
)^

alter table WF_PROC_VARIABLE add constraint FK_WF_PROC_VARIABLE_TO_WF_PROC
foreign key (PROC_ID) references WF_PROC(ID)^

/**********************************************************************************************/
create table WF_CARD_VARIABLE (
    ID varchar(32),
    CREATE_TS datetime(3),
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime(3),
    UPDATED_BY varchar(50),
    DELETE_TS datetime(3),
    DELETED_BY varchar(50),
    NAME varchar(100),
    MODULE_NAME varchar(255),
    PROPERTY_NAME varchar(100),
    ALIAS varchar(100),
    VALUE text,
    ATTRIBUTE_TYPE varchar(25),
    META_CLASS_NAME varchar(255),
    OVERRIDDEN boolean default false,
    VARIABLE_COMMENT text,
    CARD_ID varchar(32),
    primary key (ID)
)^

alter table WF_CARD_VARIABLE add constraint FK_WF_CARD_VARIABLE_TO_WF_CARD
foreign key (CARD_ID) references WF_CARD(ID)^

create index IDX_WF_CARD_VARIABLE on WF_CARD_VARIABLE(CARD_ID)^