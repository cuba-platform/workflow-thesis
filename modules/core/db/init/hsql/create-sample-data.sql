insert into SYS_FOLDER (ID, CREATE_TS, VERSION, TYPE, PARENT_ID, NAME)
values ('45918c54-e8b1-11de-93e3-9f30becb6744', current_timestamp, 0, 'A', null, 'New cards');

insert into SYS_APP_FOLDER (FOLDER_ID, FILTER_COMPONENT, FILTER_XML, VISIBILITY_SCRIPT, QUANTITY_SCRIPT)
values ('45918c54-e8b1-11de-93e3-9f30becb6744', '[wf$Card.browse].genericFilter', '<?xml version="1.0" encoding="UTF-8"?><filter><and><c name="state" class="java.lang.String" caption="msg://com.haulmont.workflow.core.entity/Card.state" type="PROPERTY">c.state = :component$genericFilter.state70315<param name="component$genericFilter.state70315">New</param></c></and></filter>', 'cuba/test/appfolders/IAmAdmin.groovy', 'workflow/appfolders/NewCardQty.groovy');

insert into SYS_FOLDER (ID, CREATE_TS, VERSION, TYPE, PARENT_ID, NAME)
values ('43220464-e8b4-11de-902d-8f2700949387', current_timestamp, 0, 'A', null, 'My assignments');

insert into SYS_APP_FOLDER (FOLDER_ID, FILTER_COMPONENT, FILTER_XML, VISIBILITY_SCRIPT, QUANTITY_SCRIPT)
values ('43220464-e8b4-11de-902d-8f2700949387', '[wf$Assignment.browse]', null, null, 'workflow/appfolders/MyAssignmentsQty.groovy');

insert into WF_ATTACHMENTTYPE (ID,CODE,ISDEFAULT)
values ('6c9c8ccc-e761-11df-94cb-6f884bc56e70','Attachment_type.attachment',true);
