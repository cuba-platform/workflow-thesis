--#Description : add missing scripts for ms sql

----- WF_DESIGN_PROCESS_VARIABLE creation -----
create table WF_DESIGN_PROCESS_VARIABLE (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    NAME varchar(100),
    META_CLASS_NAME varchar(255),
    ALIAS varchar(100),
    PROPERTY_NAME varchar(100),
    VALUE varchar(max),
    MODULE_NAME varchar(255),
    ATTRIBUTE_TYPE varchar(25),
    OVERRIDDEN tinyint default 0,
    VARIABLE_COMMENT varchar(max),
    SHOULD_BE_OVERRIDDEN tinyint default 0,
    DESIGN_ID uniqueidentifier,
    primary key (ID)
)^

alter table WF_DESIGN_PROCESS_VARIABLE add constraint FK_WF_DESIGN_PROCESS_VARIABLE_TO_WF_DESIGN
foreign key (DESIGN_ID) references WF_DESIGN(ID)^

----- WF_PROC_VARIABLE creation -----
create table WF_PROC_VARIABLE (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    NAME varchar(100),
    META_CLASS_NAME varchar(255),
    ALIAS varchar(100),
    PROPERTY_NAME varchar(100),
    VALUE varchar(max),
    MODULE_NAME varchar(255),
    ATTRIBUTE_TYPE varchar(25),
    OVERRIDDEN tinyint default 0,
    VARIABLE_COMMENT varchar(max),
    PROC_ID uniqueidentifier,
    primary key (ID)
)^

alter table WF_PROC_VARIABLE add constraint FK_WF_PROC_VARIABLE_TO_WF_PROC
foreign key (PROC_ID) references WF_PROC(ID)^

----- WF_CARD_VARIABLE creation -----
create table WF_CARD_VARIABLE (
    ID uniqueidentifier,
    CREATE_TS datetime,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS datetime,
    UPDATED_BY varchar(50),
    DELETE_TS datetime,
    DELETED_BY varchar(50),
    NAME varchar(100),
    META_CLASS_NAME varchar(255),
    ALIAS varchar(100),
    PROPERTY_NAME varchar(100),
    VALUE varchar(max),
    MODULE_NAME varchar(255),
    ATTRIBUTE_TYPE varchar(25),
    OVERRIDDEN tinyint default 0,
    VARIABLE_COMMENT varchar(max),
    CARD_ID uniqueidentifier,
    primary key (ID)
)^

alter table WF_CARD_VARIABLE add constraint FK_WF_CARD_VARIABLE_TO_WF_CARD
foreign key (CARD_ID) references WF_CARD(ID)^

create index IDX_WF_CARD_VARIABLE on WF_CARD_VARIABLE(CARD_ID)^









