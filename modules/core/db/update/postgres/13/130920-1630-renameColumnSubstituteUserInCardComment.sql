-- Description: rename column SUBSTITUTE_USER_ID to SUBSTITUTED_USER_ID and add constraint

alter table WF_CARD_COMMENT rename column SUBSTITUTE_USER_ID to SUBSTITUTED_USER_ID^

alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_SUBSTITUTED_USER foreign key (SUBSTITUTED_USER_ID) references SEC_USER (ID)^