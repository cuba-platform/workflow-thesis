alter table WF_ASSIGNMENT add SUBPROC_CARD_ID uuid^
alter table WF_ASSIGNMENT add constraint WF_ASSIGNMENT_SUBPROC_CARD foreign key (SUBPROC_CARD_ID) references WF_CARD(ID)^
alter table WF_ASSIGNMENT add FAMILY_ASSIGNMENT_ID uuid^
alter table WF_ASSIGNMENT add constraint WF_ASSIGNMENT_FAMILY_ASSIGNMENT foreign key (FAMILY_ASSIGNMENT_ID) references WF_ASSIGNMENT(ID)^
