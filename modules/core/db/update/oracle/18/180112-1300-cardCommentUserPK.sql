--$Id$

alter table WF_CARD_COMMENT_USER drop primary key^

alter table WF_CARD_COMMENT_USER add primary key(CARD_COMMENT_ID, USER_ID)^