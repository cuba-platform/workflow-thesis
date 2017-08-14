alter table WF_ASSIGNMENT add CARD_ROLE_ID varchar(32)^
alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_CARD_ROLE foreign key (CARD_ROLE_ID) references WF_CARD_ROLE (ID)^
create index IDX_WF_ASSIGNMENT_CARD_ROLE on WF_ASSIGNMENT (CARD_ROLE_ID)^

