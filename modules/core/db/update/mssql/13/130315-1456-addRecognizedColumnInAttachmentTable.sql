-- Description:
alter table WF_ATTACHMENT add RECOGNIZED_FILE_ID uniqueidentifier^
alter table WF_ATTACHMENT add constraint FK_WF_RECOGNIZED_FILE foreign key (RECOGNIZED_FILE_ID) references SYS_FILE (ID)^
