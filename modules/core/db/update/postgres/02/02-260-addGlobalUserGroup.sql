
alter table WF_USER_GROUP add column GLOBAL boolean, add column SUBSTITUTED_CREATOR_ID uuid
^

alter table WF_USER_GROUP add constraint WF_USER_GROUP_SEC_USER foreign key (SUBSTITUTED_CREATOR_ID) references SEC_USER(ID)
^

update WF_USER_GROUP set GLOBAL = false
^