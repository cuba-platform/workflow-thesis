--$Id$
--$Description:

update wf_attachment set user_id=(select id from sec_user su where su.login=created_by)^