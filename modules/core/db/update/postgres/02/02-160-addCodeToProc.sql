-- Description:

alter table WF_PROC add column CODE varchar(255)^
alter table WF_PROC add constraint WF_PROC_UNIQ_CODE unique (CODE)^

update WF_PROC set CODE = JBPM_PROCESS_KEY^