
----- WF_DESIGN_PROCESS_VARIABLE creation -----
create table WF_DESIGN_PROCESS_VARIABLE (
    ID uuid not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    DESIGN_ID uuid,
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

alter table WF_DESIGN_PROCESS_VARIABLE add constraint FK_WF_DESIGN_PROCESS_VARIABLE_TO_WF_DESIGN
foreign key (DESIGN_ID) references WF_DESIGN(ID)^

----- WF_PROC_VARIABLE creation -----
create table WF_PROC_VARIABLE (
    ID uuid not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    NAME varchar(100),
    ALIAS varchar(100),
    PROPERTY_NAME varchar(100),
    VALUE text,
    MODULE_NAME varchar(255),
    ATTRIBUTE_TYPE varchar(25),
    META_CLASS_NAME varchar(255),
    PROC_ID uuid,
    OVERRIDDEN boolean default false,
    VARIABLE_COMMENT text,
    primary key (ID)
)^

alter table WF_PROC_VARIABLE add constraint FK_WF_PROC_VARIABLE_TO_WF_PROC
foreign key (PROC_ID) references WF_PROC(ID)^

----- WF_CARD_VARIABLE creation -----
create table WF_CARD_VARIABLE (
    ID uuid not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
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
    CARD_ID uuid,
    primary key (ID)
)^

alter table WF_CARD_VARIABLE add constraint FK_WF_CARD_VARIABLE_TO_WF_CARD
foreign key (CARD_ID) references WF_CARD(ID)^

create index IDX_WF_CARD_VARIABLE on WF_CARD_VARIABLE(CARD_ID)^










