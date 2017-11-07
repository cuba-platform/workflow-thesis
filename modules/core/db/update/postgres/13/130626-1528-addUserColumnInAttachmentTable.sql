--$Description:
alter table WF_ATTACHMENT add USER_ID uuid^
alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_USER foreign key (USER_ID) references SEC_USER (ID)^