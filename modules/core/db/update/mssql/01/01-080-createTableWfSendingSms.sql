-- $Id: 01-080-createTableWfSendingSms.sql 9315 2012-10-18 07:38:19Z Novikov $
-- Description:

create table WF_SENDING_SMS
(
    ID uniqueidentifier not null,
    CREATE_TS datetime,
    CREATED_BY varchar(50),

    SMS_ID varchar(255),
    PHONE varchar(50),
    MESSAGE varchar(255),
    ERROR_CODE integer,
    STATUS integer,
    LAST_CHANGE_DATE datetime,
    ATTEMPTS_COUNT integer,
    primary key nonclustered (ID)
)^

create clustered index IDX_WF_SENDING_SMS_CREATE_TS on WF_SENDING_SMS (CREATE_TS)^

create table WF_USER_NOTIFIED_BY_SMS
(
    ID uniqueidentifier not null,
    CREATE_TS datetime,
    CREATED_BY varchar(50),

    USER_ID uniqueidentifier not null,
    primary key (ID)
)^
alter table WF_USER_NOTIFIED_BY_SMS add constraint FK_WF_USER_NOTIFIED_BY_SMS_USER foreign key (USER_ID) references SEC_USER (ID)^