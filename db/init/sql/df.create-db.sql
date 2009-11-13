create table DF_ROLE (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CODE varchar(50),
    NAME varchar(100),
    primary key (ID)
);

------------------------------------------------------------------------------------------------------------

create table DF_USER_ROLE (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    USER_ID varchar(36),
    ROLE_ID varchar(36),
    primary key (ID)
);

alter table DF_USER_ROLE add constraint FK_DF_USER_ROLE_ROLE foreign key (ROLE_ID) references DF_ROLE (ID);

alter table DF_USER_ROLE add constraint FK_DF_USER_ROLE_USER foreign key (USER_ID) references SEC_USER (ID);

------------------------------------------------------------------------------------------------------------

create table DF_DOC (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    DOC_NUM varchar(50),
    DOC_DATE timestamp,
    primary key (ID)
);

------------------------------------------------------------------------------------------------------------

create table DF_DOC_ROLE (
    ID varchar(36),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    DOC_ID varchar(36),
    ROLE_ID varchar(36),
    USER_ID varchar(36),
    primary key (ID)
);

alter table DF_DOC_ROLE add constraint FK_DF_DOC_ROLE_DOC foreign key (DOC_ID) references DF_DOC (ID);

alter table DF_DOC_ROLE add constraint FK_DF_DOC_ROLE_ROLE foreign key (ROLE_ID) references DF_ROLE (ID);

alter table DF_DOC_ROLE add constraint FK_DF_DOC_ROLE_USER foreign key (USER_ID) references SEC_USER (ID);

