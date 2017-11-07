-- Description:

alter table WF_PROC add column AVAILABLE_ROLE_ID uuid^
alter table WF_PROC add constraint WF_PROC_AVAILABLE_ROLE_ID foreign key (AVAILABLE_ROLE_ID) references SEC_ROLE(ID)^
