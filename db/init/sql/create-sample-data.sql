--insert into SEC_USER (ID, CREATE_TS, VERSION, LOGIN, LOGIN_LC, PASSWORD, NAME, GROUP_ID)
--values ('40288137-1ef4-11c8-011e-f41247370001', current_timestamp, 0, 'abramov', 'abramov', '402881371ef411c8011ef411c8c50000', 'Dmitry Abramov', '0fa2b1a5-1d68-4d69-9fbd-dff348347f93');
--
--insert into SEC_USER (ID, CREATE_TS, VERSION, LOGIN, LOGIN_LC, PASSWORD, NAME, GROUP_ID)
--values ('01e37691-1a9b-11de-b900-da881aea47a6', current_timestamp, 0, 'krivopustov', 'krivopustov', null, 'Konstantin Krivopustov', '0fa2b1a5-1d68-4d69-9fbd-dff348347f93');
--
--insert into SEC_USER (ID, CREATE_TS, VERSION, LOGIN, LOGIN_LC, PASSWORD, NAME, GROUP_ID)
--values ('8d21de74-dd81-11de-8f9e-635ed7244d80', current_timestamp, 0, 'gorodnov', 'gorodnov', null, 'Nikolay Gorodnov', '0fa2b1a5-1d68-4d69-9fbd-dff348347f93');
--
--insert into SEC_ROLE (ID, CREATE_TS, VERSION, NAME, IS_SUPER)
--values ('40288137-1ef4-11c8-011e-f416e4150005', current_timestamp, 0, 'Users', 0);
--
--insert into SEC_USER_ROLE (ID, CREATE_TS, VERSION, USER_ID, ROLE_ID)
--values ('40288137-1ef4-11c8-011e-f41aaa740006', current_timestamp, 0, '40288137-1ef4-11c8-011e-f41247370001', '40288137-1ef4-11c8-011e-f416e4150005');
--
--insert into SEC_USER_ROLE (ID, CREATE_TS, VERSION, USER_ID, ROLE_ID)
--values ('40288137-1ef4-11c8-011e-f41aaa740007', current_timestamp, 0, '40288137-1ef4-11c8-011e-f41247370001', '0c018061-b26f-4de2-a5be-dff348347f93');
--
--insert into SEC_USER_ROLE (ID, CREATE_TS, VERSION, USER_ID, ROLE_ID)
--values ('c1cb9822-dd81-11de-a618-bf812d0f2d80', current_timestamp, 0, '8d21de74-dd81-11de-8f9e-635ed7244d80', '40288137-1ef4-11c8-011e-f416e4150005');


insert into SYS_FOLDER (ID, CREATE_TS, VERSION, TYPE, PARENT_ID, NAME)
values ('45918c54-e8b1-11de-93e3-9f30becb6744', current_timestamp, 0, 'A', null, 'New cards');

insert into SYS_APP_FOLDER (FOLDER_ID, FILTER_COMPONENT, FILTER_XML, VISIBILITY_SCRIPT, QUANTITY_SCRIPT)
values ('45918c54-e8b1-11de-93e3-9f30becb6744', '[wf$Card.browse].genericFilter', '<?xml version="1.0" encoding="UTF-8"?><filter><and><c name="state" class="java.lang.String" caption="msg://com.haulmont.workflow.core.entity/Card.state" type="PROPERTY">c.state = :component$genericFilter.state70315<param name="component$genericFilter.state70315">New</param></c></and></filter>', 'cuba/test/appfolders/IAmAdmin.groovy', 'workflow/appfolders/NewCardQty.groovy');

insert into SYS_FOLDER (ID, CREATE_TS, VERSION, TYPE, PARENT_ID, NAME)
values ('43220464-e8b4-11de-902d-8f2700949387', current_timestamp, 0, 'A', null, 'My assignments');

insert into SYS_APP_FOLDER (FOLDER_ID, FILTER_COMPONENT, FILTER_XML, VISIBILITY_SCRIPT, QUANTITY_SCRIPT)
values ('43220464-e8b4-11de-902d-8f2700949387', '[wf$Assignment.browse]', null, null, 'workflow/appfolders/MyAssignmentsQty.groovy');
