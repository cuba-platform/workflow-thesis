--$Description:

update wa set wa.substituted_creator_id=(select TOP 1 su.id from sec_user su where su.login=wa.created_by order by delete_ts desc) FROM WF_ATTACHMENT wa^
