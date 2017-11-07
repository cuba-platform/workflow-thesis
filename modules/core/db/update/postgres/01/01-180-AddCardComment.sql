-- Description: WF_CARD_COMMENT, WF_CARD_COMMENT_USER
create table WF_CARD_COMMENT (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    VERSION integer,
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    COMMENT varchar(1000),
    USER_ID uuid,
    CARD_ID uuid,
    PARENT_ID uuid,
    primary key (ID)
);

alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_USER foreign key (USER_ID) references SEC_USER (ID);
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_CARD foreign key (CARD_ID) references WF_CARD (ID);
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_PARENT foreign key (PARENT_ID) references WF_CARD_COMMENT (ID);

create table WF_CARD_COMMENT_USER (
    CARD_COMMENT_ID uuid,
    USER_ID uuid,
    primary key (CARD_COMMENT_ID, USER_ID)
);

alter table WF_CARD_COMMENT_USER add constraint FK_WF_CCU_CARD_COMMENT foreign key (CARD_COMMENT_ID) references WF_CARD_COMMENT (ID);
alter table WF_CARD_COMMENT_USER add constraint FK_WF_CCU_USER foreign key (USER_ID) references SEC_USER (ID);