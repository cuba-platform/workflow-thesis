-- $Id$
-- Description: add WF_PROC_ROLE.ASSIGN_TO_CREATOR field

insert into SYS_DB_UPDATE (SCRIPT_NAME) values ('1003-010-add_ProcRole_assignToCreator');

-- begin script

alter table WF_PROC_ROLE add ASSIGN_TO_CREATOR boolean;
