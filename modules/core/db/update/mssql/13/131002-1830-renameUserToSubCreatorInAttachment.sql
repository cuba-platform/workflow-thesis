--$Description: renames USER_ID column

exec sp_rename 'WF_ATTACHMENT.USER_ID', 'SUBSTITUTED_CREATOR_ID', 'COLUMN' ^