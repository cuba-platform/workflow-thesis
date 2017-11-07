-- Description:
alter table WF_CARD add column CATEGORY_ID uuid^
alter table WF_CARD add constraint FK_WF_CARD_CATEGORY_ID foreign key (CATEGORY_ID) references SYS_CATEGORY(ID)^