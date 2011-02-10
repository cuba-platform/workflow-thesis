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

------------------------------------------------------------------------------------------------------------

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

------------------------------------------------------------------------------------------------------------

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

------------------------------------------------------------------------------------------------------------

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
    CARD_TYPES varchar(500),
    STATES varchar(500),
    PERMISSIONS_ENABLED boolean,
    DESIGN_ID uuid,
    primary key (ID)
)^

alter table WF_PROC add constraint FK_WF_PROC_DESIGN foreign key (DESIGN_ID) references WF_DESIGN (ID)^

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
    CREATOR_ID uuid,
    SUBSTITUTED_CREATOR_ID uuid,
    PARENT_CARD_ID uuid,
    primary key (ID)
)^

alter table WF_CARD add constraint FK_WF_CARD_PROC foreign key (PROC_ID) references WF_PROC (ID)^
alter table WF_CARD add constraint FK_WF_CARD_USER foreign key (CREATOR_ID) references SEC_USER (ID)^
alter table WF_CARD add constraint FK_WF_CARD_CARD foreign key (PARENT_CARD_ID) references WF_CARD (ID)^
alter table WF_CARD add constraint FK_WF_CARD_SUBSTITUTED_CREATOR foreign key (SUBSTITUTED_CREATOR_ID) references SEC_USER (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_COMMENT (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    COMMENT text,
    USER_ID uuid,
    CARD_ID uuid,
    PARENT_ID uuid,
    primary key (ID)
)^

alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_USER foreign key (USER_ID) references SEC_USER (ID)^
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_CARD foreign key (CARD_ID) references WF_CARD (ID)^
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_PARENT foreign key (PARENT_ID) references WF_CARD_COMMENT (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_COMMENT_USER (
    CARD_COMMENT_ID uuid,
    USER_ID uuid,
    primary key (CARD_COMMENT_ID, USER_ID)
)^

alter table WF_CARD_COMMENT_USER add constraint FK_WF_CCU_CARD_COMMENT foreign key (CARD_COMMENT_ID) references WF_CARD_COMMENT (ID)^
alter table WF_CARD_COMMENT_USER add constraint FK_WF_CCU_USER foreign key (USER_ID) references SEC_USER (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_RELATION (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_ID uuid,
    RELATED_CARD_ID uuid,
    primary key (ID)
)^

alter table WF_CARD_RELATION add constraint FK_WF_CC_CARD foreign key (CARD_ID) references WF_CARD (ID)^
alter table WF_CARD_RELATION add constraint FK_WF_CC_CARD_RELATED foreign key (RELATED_CARD_ID) references WF_CARD (ID)^

------------------------------------------------------------------------------------------------------------
create table WF_CARD_INFO (
    ID uuid,
    NAME varchar(50),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_ID uuid,
    TYPE integer,
    USER_ID uuid,
    JBPM_EXECUTION_ID varchar(255),
    ACTIVITY varchar(255),
    DESCRIPTION text,
    primary key (ID)
)^

alter table WF_CARD_INFO add constraint FK_WF_CARD_INFO_CARD foreign key (CARD_ID) references WF_CARD(ID)^
alter table WF_CARD_INFO add constraint FK_WF_CARD_INFO_USER foreign key (USER_ID) references SEC_USER(ID)^

create index IDX_WF_CARD_INFO_CARD on WF_CARD_INFO(card_id)^

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
    PROC_ID uuid,
    MASTER_ASSIGNMENT_ID uuid,
    NAME varchar(255),
    DESCRIPTION varchar(1000),
    JBPM_PROCESS_ID varchar(255),
    DUE_DATE timestamp,
    FINISHED timestamp,
    FINISHED_BY uuid,
    OUTCOME varchar(255),
    COMMENT varchar(2000),
    ITERATION integer,
    primary key (ID)
)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_USER foreign key (USER_ID) references SEC_USER (ID)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_FINISHED_BY foreign key (FINISHED_BY) references SEC_USER (ID)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_PROC foreign key (PROC_ID) references WF_PROC (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_ATTACHMENTTYPE (
    ID uuid,
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
)^

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
    TYPE_ID uuid,
    NAME varchar(500),
    COMMENT varchar(1000),
    SIGNATURES text,
    CARD_ID uuid,
    ASSIGNMENT_ID uuid,
    primary key (ID)
)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_FILE foreign key (FILE_ID) references SYS_FILE (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_ASSIGNMENT foreign key (ASSIGNMENT_ID) references WF_ASSIGNMENT (ID)^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_TYPE foreign key (TYPE_ID) references WF_ATTACHMENTTYPE (ID)^

insert into WF_ATTACHMENTTYPE (ID,CODE,ISDEFAULT)
values ('6c9c8ccc-e761-11df-94cb-6f884bc56e70','AttachmentType.attachment',true)^

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
    INVISIBLE boolean,
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
    NOTIFY_BY_CARD_INFO boolean,
    SORT_ORDER integer,
    primary key (ID)
)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_ROLE foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID)^

alter table WF_CARD_ROLE add constraint FK_WF_CARD_ROLE_USER foreign key (USER_ID) references SEC_USER (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_PROC (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_ID uuid,
    PROC_ID uuid,
    IS_ACTIVE boolean,
    START_COUNT integer,
    STATE varchar(255),
    SORT_ORDER integer,
    primary key (ID)
)^

alter table WF_CARD_PROC add constraint FK_WF_CARD_PROC_CARD foreign key (CARD_ID) references WF_CARD (ID)^

alter table WF_CARD_PROC add constraint FK_WF_CARD_PROC_PROC foreign key (PROC_ID) references WF_PROC (ID)^

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
    WORK_START char(5),
    WORK_END char(5),
    COMMENT varchar(500),
    primary key (ID)
);

------------------------------------------------------------------------------------------------------------

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
)^

alter table WF_PROC_ROLE_PERMISSION add constraint FK_WF_PROC_ROLE_PERMISSION_TO_PROC_ROLE foreign key (PROC_ROLE_TO_ID) references WF_PROC_ROLE (ID)^
alter table WF_PROC_ROLE_PERMISSION add constraint FK_WF_PROC_ROLE_PERMISSION_FROM_PROC_ROLE foreign key (PROC_ROLE_FROM_ID) references WF_PROC_ROLE (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_USER_GROUP (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    VERSION integer,
    NAME varchar(255),
    primary key (ID)
)^

------------------------------------------------------------------------------------------------------------

create table WF_USER_GROUP_USER (
    USER_GROUP_ID uuid,
    USER_ID uuid,
    primary key (USER_GROUP_ID, USER_ID)
)^

alter table WF_USER_GROUP_USER add constraint FK_WF_UGU_USER_GROUP foreign key (USER_GROUP_ID) references WF_USER_GROUP (ID)^
alter table WF_USER_GROUP_USER add constraint FK_WF_UGU_USER foreign key (USER_ID) references SEC_USER (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_PROC_STAGE (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    NAME varchar(255),
    DURATION numeric(3),
    TIME_UNIT varchar(1),
    PROC_ID uuid,
    START_ACTIVITY varchar(200),
    END_ACTIVITY varchar(200),
    END_TRANSITION varchar(200),
    NOTIFICATION_SCRIPT varchar(200),
    NOTIFY_CURRENT_ACTOR boolean,
    PROC_STAGE_TYPE_ID uuid,
    primary key (ID)
)^

alter table WF_PROC_STAGE add constraint FK_WF_PROC_STAGE_PROC foreign key (PROC_ID) references WF_PROC (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_CARD_STAGE (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    START_DATE timestamp,
    END_DATE_PLAN timestamp,
    END_DATE_FACT timestamp,
    NOTIFIED boolean default false,
    CARD_ID uuid,
    PROC_STAGE_ID uuid,
    primary key (ID)
)^

alter table WF_CARD_STAGE add constraint FK_WF_CARD_STAGE_PROC_STAGE foreign key (PROC_STAGE_ID) references WF_PROC_STAGE (ID)^
alter table WF_CARD_STAGE add constraint FK_WF_CARD_STAGE_CARD foreign key (CARD_ID) references WF_CARD (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_PROC_STAGE_PROC_ROLE (
    PROC_STAGE_ID uuid,
    PROC_ROLE_ID uuid,
    primary key (PROC_STAGE_ID, PROC_ROLE_ID)
)^

alter table WF_PROC_STAGE_PROC_ROLE add constraint FK_WF_PSPR_PROC_STAGE foreign key (PROC_STAGE_ID) references WF_PROC_STAGE (ID)^
alter table WF_PROC_STAGE_PROC_ROLE add constraint FK_WF_PSPR_USER foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID)^

------------------------------------------------------------------------------------------------------------

create table WF_PROC_STAGE_TYPE (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),

    NAME varchar(200),
    CODE varchar(200),
    DURATION_SCRIPT_ENABLED boolean,
    DURATION_SCRIPT text,

    primary key (ID)
)^
alter table WF_PROC_STAGE add constraint FK_WF_PROC_STAGE_TYPE foreign key (PROC_STAGE_TYPE_ID) references WF_PROC_STAGE_TYPE (ID)^

------------------------------------------------------------------------------------------------------------

create index idx_wf_attachment_card on wf_attachment (card_id)^

create index idx_wf_attachment_assignment on wf_attachment (assignment_id)^

create index idx_wf_assignment_card on wf_assignment (card_id)^

create index idx_wf_card_stage_card on wf_card_stage (card_id)^

create index idx_wf_card_role_card on wf_card_role (card_id)^

create index idx_wf_card_proc_card on wf_card_proc (card_id)^

create index idx_wf_assignment_user on wf_assignment (user_id)^

create index idx_wf_assignment_user_finished on wf_assignment (user_id, finished)^

create index idx_wf_card_role_user_code on wf_card_role (user_id, code)^

create index idx_wf_card_info_user on wf_card_info (user_id, delete_ts)^

create index idx_wf_timer_due_date on wf_timer (due_date)^

create index idx_wf_card_comment_card on wf_card_comment (card_id)^

create index idx_wf_card_relation_card on wf_card_relation (card_id)^

create index idx_wf_card_relation_related_card on wf_card_relation (related_card_id)^

create index idx_wf_proc_role_proc on wf_proc_role (proc_id)^
