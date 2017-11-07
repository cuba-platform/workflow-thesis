alter table WF_CARD add FAMILY_CARD_ID uniqueidentifier^
alter table WF_CARD add constraint WF_CARD_FAMILY_CARD foreign key (FAMILY_CARD_ID) references WF_CARD(ID)^
alter table WF_CARD add FAMILY_JBPM_PROCESS_ID varchar(255)^