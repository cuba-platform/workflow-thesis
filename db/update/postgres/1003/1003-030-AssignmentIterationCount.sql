-- $Id$
-- Description: Adding WF_ASSIGNMENT.ITERATION field

insert into SYS_DB_UPDATE (SCRIPT_NAME) values ('1003-030-AssignmentIterationCount');

-- begin script

alter table WF_ASSIGNMENT add ITERATION integer;

