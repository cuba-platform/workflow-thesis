-- Description: Creating CardInfo table

create table WF_CARD_INFO (
    ID uuid,
    NAME varchar(50),
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    CARD_ID uuid,
    TYPE integer,
    USER_ID uuid,
    JBPM_EXECUTION_ID varchar(255),
    ACTIVITY varchar(255),
    primary key (ID)
);

alter table WF_CARD_INFO add constraint FK_WF_CARD_INFO_CARD foreign key (CARD_ID) references WF_CARD(ID);
alter table WF_CARD_INFO add constraint FK_WF_CARD_INFO_USER foreign key (USER_ID) references SEC_USER(ID);