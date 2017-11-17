alter table WF_USER_GROUP_USER drop primary key^
alter table WF_USER_GROUP_USER add constraint WF_USER_GROUP_USER_PK primary key (USER_GROUP_ID, USER_ID)^