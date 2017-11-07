
alter table WF_PROC_APP_FOLDER alter column FOLDER_ID set not null^
alter table WF_PROC_APP_FOLDER add constraint WF_PROC_APP_FOLDER_PKEY primary key(FOLDER_ID);