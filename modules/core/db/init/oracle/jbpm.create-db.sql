create table JBPM4_DEPLOYMENT (
    DBID_ number not null,
    NAME_ clob,
    TIMESTAMP_ number,
    STATE_ varchar2(255),
    primary key(DBID_)
)^

create table JBPM4_DEPLOYPROP (
    DBID_ number not null,
    DEPLOYMENT_ number,
    OBJNAME_ varchar2(255),
    KEY_ varchar2(255),
    STRINGVAL_ varchar2(255),
    LONGVAL_ number,
    primary key(DBID_)
)^
create index IDX_DEPLPROP_DEPL on JBPM4_DEPLOYPROP(DEPLOYMENT_)^

create table JBPM4_EXECUTION (
    DBID_ number not null,
    CLASS_ varchar2(255) not null,
    DBVERSION_ integer not null,
    ACTIVITYNAME_ varchar2(255),
    PROCDEFID_ varchar2(255),
    HASVARS_ char(1),
    NAME_ varchar2(255),
    KEY_ varchar2(255),
    ID_ varchar2(255),
    STATE_ varchar2(255),
    SUSPHISTSTATE_ varchar2(255),
    PRIORITY_ integer,
    HISACTINST_ number,
    PARENT_ number,
    INSTANCE_ number,
    SUPEREXEC_ number,
    SUBPROCINST_ number,
    PARENT_IDX_ integer,
    primary key(DBID_)
)^
create unique index JBPM4_EXECUTION_ID__KEY on JBPM4_EXECUTION(ID_)^
create index IDX_EXEC_INSTANCE on JBPM4_EXECUTION(INSTANCE_)^
create index IDX_EXEC_PARENT on JBPM4_EXECUTION(PARENT_)^
create index IDX_EXEC_SUBPI on JBPM4_EXECUTION(SUBPROCINST_)^
create index IDX_EXEC_SUPEREXEC on JBPM4_EXECUTION(SUPEREXEC_)^

create table JBPM4_HIST_ACTINST (
    DBID_ number not null,
    CLASS_ varchar2(255) not null,
    DBVERSION_ integer not null,
    HPROCI_ number,
    TYPE_ varchar2(255),
    EXECUTION_ varchar2(255),
    ACTIVITY_NAME_ varchar2(255),
    START_ timestamp,
    END_ timestamp,
    DURATION_ number,
    TRANSITION_ varchar2(255),
    NEXTIDX_ integer,
    HTASK_ number,
    primary key(DBID_)
)^
create index IDX_HACTI_HPROCI on JBPM4_HIST_ACTINST(HPROCI_)^
create index IDX_HTI_HTASK on JBPM4_HIST_ACTINST(HTASK_)^

create table JBPM4_HIST_DETAIL (
    DBID_ number not null,
    CLASS_ varchar2(255) not null,
    DBVERSION_ integer not null,
    USERID_ varchar2(255),
    TIME_ timestamp,
    HPROCI_ number,
    HPROCIIDX_ integer,
    HACTI_ number,
    HACTIIDX_ integer,
    HTASK_ number,
    HTASKIDX_ integer,
    HVAR_ number,
    HVARIDX_ integer,
    MESSAGE_ clob,
    OLD_STR_ varchar2(255),
    NEW_STR_ varchar2(255),
    OLD_INT_ integer,
    NEW_INT_ integer,
    OLD_TIME_ timestamp,
    NEW_TIME_ timestamp,
    PARENT_ number,
    PARENT_IDX_ integer,
    primary key(DBID_)
)^
create index IDX_HDET_HACTI on JBPM4_HIST_DETAIL(HACTI_)^
create index IDX_HDET_HPROCI on JBPM4_HIST_DETAIL(HPROCI_)^
create index IDX_HDET_HTASK on JBPM4_HIST_DETAIL(HTASK_)^
create index IDX_HDET_HVAR on JBPM4_HIST_DETAIL(HVAR_)^

create table JBPM4_HIST_PROCINST (
    DBID_ number not null,
    DBVERSION_ integer not null,
    ID_ varchar2(255),
    PROCDEFID_ varchar2(255),
    KEY_ varchar2(255),
    START_ timestamp,
    END_ timestamp,
    DURATION_ number,
    STATE_ varchar2(255),
    ENDACTIVITY_ varchar2(255),
    NEXTIDX_ integer,
    primary key(DBID_)
)^

create table JBPM4_HIST_TASK (
    DBID_ number not null,
    DBVERSION_ integer not null,
    EXECUTION_ varchar2(255),
    OUTCOME_ varchar2(255),
    ASSIGNEE_ varchar2(255),
    PRIORITY_ integer,
    STATE_ varchar2(255),
    CREATE_ timestamp,
    END_ timestamp,
    DURATION_ number,
    NEXTIDX_ integer,
    SUPERTASK_ number,
    primary key(DBID_)
)^
create index IDX_HSUPERT_SUB on JBPM4_HIST_TASK(SUPERTASK_)^

create table JBPM4_HIST_VAR (
    DBID_ number not null,
    DBVERSION_ integer not null,
    PROCINSTID_ varchar2(255),
    EXECUTIONID_ varchar2(255),
    VARNAME_ varchar2(255),
    VALUE_ varchar2(255),
    HPROCI_ number,
    HTASK_ number,
    primary key(DBID_)
)^
create index IDX_HVAR_HPROCI on JBPM4_HIST_VAR(HPROCI_)^
create index IDX_HVAR_HTASK on JBPM4_HIST_VAR(HTASK_)^

create table JBPM4_ID_GROUP (
    DBID_ number not null,
    DBVERSION_ integer not null,
    ID_ varchar2(255),
    NAME_ varchar2(255),
    TYPE_ varchar2(255),
    PARENT_ number,
    primary key(DBID_)
)^
create index IDX_GROUP_PARENT on JBPM4_ID_GROUP(PARENT_)^

create table JBPM4_ID_MEMBERSHIP (
    DBID_ number not null,
    DBVERSION_ integer not null,
    USER_ number,
    GROUP_ number,
    NAME_ varchar2(255),
    primary key(DBID_)
)^
create index IDX_MEM_GROUP on JBPM4_ID_MEMBERSHIP(GROUP_)^
create index IDX_MEM_USER on JBPM4_ID_MEMBERSHIP(USER_)^

create table JBPM4_ID_USER (
    DBID_ number not null,
    DBVERSION_ integer not null,
    ID_ varchar2(255),
    PASSWORD_ varchar2(255),
    GIVENNAME_ varchar2(255),
    FAMILYNAME_ varchar2(255),
    BUSINESSEMAIL_ varchar2(255),
    primary key(DBID_)
)^

create table JBPM4_JOB (
    DBID_ number not null,
    CLASS_ varchar2(255) not null,
    DBVERSION_ integer not null,
    DUEDATE_ timestamp,
    STATE_ varchar2(255),
    ISEXCLUSIVE_ char(1),
    LOCKOWNER_ varchar2(255),
    LOCKEXPTIME_ timestamp,
    EXCEPTION_ clob,
    RETRIES_ integer,
    PROCESSINSTANCE_ number,
    EXECUTION_ number,
    CFG_ number,
    SIGNAL_ varchar2(255),
    EVENT_ varchar2(255),
    REPEAT_ varchar2(255),
    primary key(DBID_)
)^
create index IDX_JOB_CFG on JBPM4_JOB(CFG_)^
create index IDX_JOB_EXE on JBPM4_JOB(EXECUTION_)^
create index IDX_JOB_PRINST on JBPM4_JOB(PROCESSINSTANCE_)^
create index IDX_JOBDUEDATE on JBPM4_JOB(DUEDATE_)^
create index IDX_JOBLOCKEXP on JBPM4_JOB(LOCKEXPTIME_)^
create index IDX_JOBRETRIES on JBPM4_JOB(RETRIES_)^

create table JBPM4_LOB (
    DBID_ number not null,
    DBVERSION_ integer not null,
    BLOB_VALUE_ blob,
    DEPLOYMENT_ number,
    NAME_ clob,
    primary key(DBID_)
)^
create index IDX_LOB_DEPLOYMENT on JBPM4_LOB(DEPLOYMENT_)^

create table JBPM4_PARTICIPATION (
    DBID_ number not null,
    DBVERSION_ integer not null,
    GROUPID_ varchar2(255),
    USERID_ varchar2(255),
    TYPE_ varchar2(255),
    TASK_ number,
    SWIMLANE_ number,
    primary key(DBID_)
)^
create index IDX_PART_TASK on JBPM4_PARTICIPATION(TASK_)^

create table JBPM4_PROPERTY (
    KEY_ varchar2(255) not null,
    VERSION_ integer not null,
    VALUE_ varchar2(255),
    primary key(KEY_)
)^

create table JBPM4_SWIMLANE (
    DBID_ number not null,
    DBVERSION_ integer not null,
    NAME_ varchar2(255),
    ASSIGNEE_ varchar2(255),
    EXECUTION_ number,
    primary key(DBID_)
)^
create index IDX_SWIMLANE_EXEC on JBPM4_SWIMLANE(EXECUTION_)^

create table JBPM4_TASK (
    DBID_ number not null,
    CLASS_ char(1) not null,
    DBVERSION_ integer not null,
    NAME_ varchar2(255),
    DESCR_ clob,
    STATE_ varchar2(255),
    SUSPHISTSTATE_ varchar2(255),
    ASSIGNEE_ varchar2(255),
    FORM_ varchar2(255),
    PRIORITY_ integer,
    CREATE_ timestamp,
    DUEDATE_ timestamp,
    PROGRESS_ integer,
    SIGNALLING_ char(1),
    EXECUTION_ID_ varchar2(255),
    ACTIVITY_NAME_ varchar2(255),
    HASVARS_ char(1),
    SUPERTASK_ number,
    EXECUTION_ number,
    PROCINST_ number,
    SWIMLANE_ number,
    TASKDEFNAME_ varchar2(255),
    primary key(DBID_)
)^
create index IDX_TASK_SUPERTASK on JBPM4_TASK(SUPERTASK_)^

create table JBPM4_VARIABLE (
    DBID_ number not null,
    CLASS_ varchar2(255) not null,
    DBVERSION_ integer not null,
    KEY_ varchar2(255),
    CONVERTER_ varchar2(255),
    HIST_ char(1),
    EXECUTION_ number,
    TASK_ number,
    LOB_ number,
    DATE_VALUE_ timestamp,
    DOUBLE_VALUE_ float,
    CLASSNAME_ varchar2(255),
    LONG_VALUE_ number,
    STRING_VALUE_ clob,
    TEXT_VALUE_ clob,
    EXESYS_ number,
    primary key(DBID_)
)^
create index IDX_VAR_EXECUTION on JBPM4_VARIABLE(EXECUTION_)^
create index IDX_VAR_EXESYS on JBPM4_VARIABLE(EXESYS_)^
create index IDX_VAR_LOB on JBPM4_VARIABLE(LOB_)^
create index IDX_VAR_TASK on JBPM4_VARIABLE(TASK_)^

alter table JBPM4_DEPLOYPROP add constraint FK_DEPLPROP_DEPL foreign key (DEPLOYMENT_) references JBPM4_DEPLOYMENT(DBID_)^

alter table JBPM4_EXECUTION add constraint FK_EXEC_SUPEREXEC foreign key (SUPEREXEC_) references JBPM4_EXECUTION(DBID_)^
alter table JBPM4_EXECUTION add constraint FK_EXEC_INSTANCE foreign key (INSTANCE_) references JBPM4_EXECUTION(DBID_)^
alter table JBPM4_EXECUTION add constraint FK_EXEC_SUBPI foreign key (SUBPROCINST_) references JBPM4_EXECUTION(DBID_)^
alter table JBPM4_EXECUTION add constraint FK_EXEC_PARENT foreign key (PARENT_) references JBPM4_EXECUTION(DBID_)^

alter table JBPM4_HIST_ACTINST add constraint FK_HACTI_HPROCI foreign key (HPROCI_) references JBPM4_HIST_PROCINST(DBID_)^
alter table JBPM4_HIST_ACTINST add constraint FK_HTI_HTASK foreign key (HTASK_) references JBPM4_HIST_TASK(DBID_)^

alter table JBPM4_HIST_DETAIL add constraint FK_HDETAIL_HACTI foreign key (HACTI_) references JBPM4_HIST_ACTINST(DBID_)^
alter table JBPM4_HIST_DETAIL add constraint FK_HDETAIL_HPROCI foreign key (HPROCI_) references JBPM4_HIST_PROCINST(DBID_)^
alter table JBPM4_HIST_DETAIL add constraint FK_HDETAIL_HTASK foreign key (HTASK_) references JBPM4_HIST_TASK(DBID_)^
alter table JBPM4_HIST_DETAIL add constraint FK_HDETAIL_HVAR foreign key (HVAR_) references JBPM4_HIST_VAR(DBID_)^

alter table JBPM4_HIST_TASK add constraint FK_HSUPERT_SUB foreign key (SUPERTASK_) references JBPM4_HIST_TASK(DBID_)^

alter table JBPM4_HIST_VAR add constraint FK_HVAR_HPROCI foreign key (HPROCI_) references JBPM4_HIST_PROCINST(DBID_)^
alter table JBPM4_HIST_VAR add constraint FK_HVAR_HTASK foreign key (HTASK_) references JBPM4_HIST_TASK(DBID_)^

alter table JBPM4_ID_GROUP add constraint FK_GROUP_PARENT foreign key (PARENT_) references JBPM4_ID_GROUP(DBID_)^

alter table JBPM4_ID_MEMBERSHIP add constraint FK_MEM_GROUP foreign key (GROUP_) references JBPM4_ID_GROUP(DBID_)^
alter table JBPM4_ID_MEMBERSHIP add constraint FK_MEM_USER foreign key (USER_) references JBPM4_ID_USER(DBID_)^

alter table JBPM4_JOB add constraint FK_JOB_CFG foreign key (CFG_) references JBPM4_LOB(DBID_)^

alter table JBPM4_LOB add constraint FK_LOB_DEPLOYMENT foreign key (DEPLOYMENT_) references JBPM4_DEPLOYMENT(DBID_)^

alter table JBPM4_PARTICIPATION add constraint FK_PART_SWIMLANE foreign key (SWIMLANE_) references JBPM4_SWIMLANE(DBID_)^
alter table JBPM4_PARTICIPATION add constraint FK_PART_TASK foreign key (TASK_) references JBPM4_TASK(DBID_)^

alter table JBPM4_SWIMLANE add constraint FK_SWIMLANE_EXEC foreign key (EXECUTION_) references JBPM4_EXECUTION(DBID_)^

alter table JBPM4_TASK add constraint FK_TASK_SWIML foreign key (SWIMLANE_) references JBPM4_SWIMLANE(DBID_)^
alter table JBPM4_TASK add constraint FK_TASK_SUPERTASK foreign key (SUPERTASK_) references JBPM4_TASK(DBID_)^

alter table JBPM4_VARIABLE add constraint FK_VAR_EXESYS foreign key (EXESYS_) references JBPM4_EXECUTION(DBID_)^
alter table JBPM4_VARIABLE add constraint FK_VAR_EXECUTION foreign key (EXECUTION_) references JBPM4_EXECUTION(DBID_)^
alter table JBPM4_VARIABLE add constraint FK_VAR_LOB foreign key (LOB_) references JBPM4_LOB(DBID_)^
alter table JBPM4_VARIABLE add constraint FK_VAR_TASK foreign key (TASK_) references JBPM4_TASK(DBID_)^
