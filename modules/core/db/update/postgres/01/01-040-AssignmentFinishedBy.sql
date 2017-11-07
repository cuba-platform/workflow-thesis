-- Description: Adding WF_ASSIGNMENT.FINISHED_BY field

alter table WF_ASSIGNMENT add FINISHED_BY uuid;

alter table WF_ASSIGNMENT add constraint FK_WF_ASSIGNMENT_FINISHED_BY foreign key (FINISHED_BY) references SEC_USER (ID);
