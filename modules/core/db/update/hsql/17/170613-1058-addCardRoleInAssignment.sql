
alter table WF_ATTACHMENT add CARD_ROLE_ID varchar(36)^
alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_CARD_ROLE foreign key (CARD_ROLE_ID) references WF_CARD_ROLE (ID)^
