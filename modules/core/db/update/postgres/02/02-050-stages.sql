-- Description:

alter table WF_PROC_STAGE
    add START_ACTIVITY varchar(200),
    add END_ACTIVITY varchar(200),
    add END_TRANSITION varchar(200),
    add NOTIFICATION_SCRIPT varchar(200),
    add NOTIFY_CURRENT_ACTOR boolean,
    add PROC_STAGE_TYPE_ID uuid;

alter table WF_PROC_STAGE add constraint WF_PROC_STAGE_PK primary key (ID);

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
);

alter table WF_CARD_STAGE add constraint FK_WF_CARD_STAGE_PROC_STAGE foreign key (PROC_STAGE_ID) references WF_PROC_STAGE (ID);
alter table WF_CARD_STAGE add constraint FK_WF_CARD_STAGE_CARD foreign key (CARD_ID) references WF_CARD (ID);

create table WF_PROC_STAGE_PROC_ROLE (
    PROC_STAGE_ID uuid,
    PROC_ROLE_ID uuid,
    primary key (PROC_STAGE_ID, PROC_ROLE_ID)
);

alter table WF_PROC_STAGE_PROC_ROLE add constraint FK_WF_PSPR_PROC_STAGE foreign key (PROC_STAGE_ID) references WF_PROC_STAGE (ID);
alter table WF_PROC_STAGE_PROC_ROLE add constraint FK_WF_PSPR_USER foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID);

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
    primary key (ID)
);

alter table WF_PROC_STAGE add constraint FK_WF_PROC_STAGE_TYPE foreign key (PROC_STAGE_TYPE_ID) references WF_PROC_STAGE_TYPE (ID);
delete from WF_TIMER where ACTION_CLASS = 'com.haulmont.workflow.core.timer.MainAssignmentTimerAction';

insert into WF_PROC_STAGE_TYPE (ID, CREATED_BY, CREATE_TS, VERSION, CODE, NAME) values (newid(), USER, now(), 1, 'stage', 'Этап');
insert into WF_PROC_STAGE_TYPE (ID, CREATED_BY, CREATE_TS, VERSION, CODE, NAME) values (newid(), USER, now(), 1, 'substage', 'Подэтап');
