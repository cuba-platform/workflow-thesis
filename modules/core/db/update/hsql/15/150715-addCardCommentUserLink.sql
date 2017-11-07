
create table WF_CARD_COMMENT_USER (
    CARD_COMMENT_ID varchar(36),
    USER_ID varchar(36),
    primary key (CARD_COMMENT_ID, USER_ID)
)^

alter table WF_CARD_COMMENT_USER add constraint FK_WF_CCU_CARD_COMMENT foreign key (CARD_COMMENT_ID) references WF_CARD_COMMENT (ID)^
alter table WF_CARD_COMMENT_USER add constraint FK_WF_CCU_USER foreign key (USER_ID) references SEC_USER (ID)^

