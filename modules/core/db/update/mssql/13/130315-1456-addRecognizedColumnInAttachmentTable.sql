alter table WF_ATTACHMENT add column RECOGNIZED_ATTACHMENT_ID uniqueidentifier^
alter table WF_ATTACHMENT add constraint FK_WF_RECOGNIZED_FILE foreign key (RECOGNIZED_FILE_DESCRIPTOR_ID) references SYS_FILE (ID)^
