--$Description:

update WF_ATTACHMENT wa set substituted_creator_id=(select su.id from sec_user su where su.login=wa.created_by order by delete_ts desc limit 1)^
