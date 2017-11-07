-- Description:
alter table WF_ATTACHMENTTYPE add ISSYSTEM boolean^
alter table WF_ATTACHMENTTYPE add constraint WF_ATTACHMENTTYPE_UNIQ_CODE unique (CODE, DELETE_TS)^
update WF_ATTACHMENTTYPE set ISSYSTEM = false;
update WF_ATTACHMENTTYPE set ISSYSTEM = true where id = '6c9c8ccc-e761-11df-94cb-6f884bc56e70';
