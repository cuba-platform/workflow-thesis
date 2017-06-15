
alter table WF_ASSIGNMENT add CARD_ROLE_ID varchar(36)^
alter table WF_ASSIGNMENT add constraint FK_WF_ATTACHMENT_CARD_ROLE foreign key (CARD_ROLE_ID) references WF_CARD_ROLE (ID)^
