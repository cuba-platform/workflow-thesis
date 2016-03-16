
alter table wf_assignment rename column COMMENT to ASSIGNMENT_COMMENT;
alter table wf_attachment rename column TYPE to ATTACHMENT_TYPE;
alter table wf_attachment rename column COMMENT to ATTACHMENT_COMMENT;
alter table wf_attachmenttype rename column COMMENT to ATTACHMENTTYPE_COMMENT;
alter table wf_calendar rename column COMMENT to CALENDAR_COMMENT;
alter table wf_card rename column TYPE to CARD_TYPE;
alter table wf_card_comment rename column COMMENT to CARD_COMMENT;
alter table wf_card_info rename column TYPE to CARD_INFO_TYPE;
alter table wf_design_file rename column TYPE to DESIGN_FILE_TYPE;
alter table wf_proc_role_permission rename column TYPE to PERMISSION_TYPE;