-- Description: rename column SUBSTITUTE_USER_ID to SUBSTITUTED_USER_ID and add constraint


{call sp_rename 'WF_CARD_COMMENT.SUBSTITUTE_USER_ID', 'SUBSTITUTED_USER_ID', 'column'}^

alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_SUBSTITUTED_USER foreign key (SUBSTITUTED_USER_ID) references SEC_USER (ID)^
