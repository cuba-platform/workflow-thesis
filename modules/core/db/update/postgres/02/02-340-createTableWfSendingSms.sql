-- Description:

create table WF_SENDING_SMS
(
    ID uuid not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),

    SMS_ID varchar(255),
    PHONE varchar(50),
    MESSAGE varchar(255),
    ERROR_CODE integer,
    STATUS integer,
    LAST_CHANGE_DATE timestamp,
    ATTEMPTS_COUNT int,
    primary key (ID)
)^

create table WF_USER_NOTIFIED_BY_SMS
(
    ID uuid not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),

    USER_ID uuid not null,
    primary key (ID)
)^
alter table WF_USER_NOTIFIED_BY_SMS add constraint FK_WF_USER_NOTIFIED_BY_SMS_USER foreign key (USER_ID) references SEC_USER (ID)^