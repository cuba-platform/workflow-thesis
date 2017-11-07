-- Description:


alter table WF_ATTACHMENT add column VERSION_OF_ID uuid
^
alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_ATTACHMENT foreign key (VERSION_OF_ID) references WF_ATTACHMENT (ID)
^
alter table WF_ATTACHMENT add column VERSION_NUM integer
^