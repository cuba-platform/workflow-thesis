-- Description: replace all the constraints with an identifier more than 30 characters long

alter table WF_CARD_COMMENT drop constraint FK_WF_CARD_COMMENT_SUBSTITUTED_USER^
alter table WF_CARD_COMMENT add constraint FK_WF_CARD_COMMENT_SUB_USER foreign key (SUBSTITUTED_USER_ID) references SEC_USER (ID)^

alter table WF_ASSIGNMENT drop constraint WF_ASSIGNMENT_FAMILY_ASSIGNMENT^
alter table WF_ASSIGNMENT add constraint WF_ASSIGNMENT_FAMILY_ASS foreign key (FAMILY_ASSIGNMENT_ID) references WF_ASSIGNMENT(ID)^

alter table WF_DEFAULT_PROC_ACTOR drop constraint FK_WF_DEFAULT_PROC_ACTOR_PROC_ROLE^
alter table WF_DEFAULT_PROC_ACTOR add constraint FK_WF_DEFAULT_PROC_ACT_PRO_ROL foreign key (PROC_ROLE_ID) references WF_PROC_ROLE (ID)^

alter table WF_PROC_ROLE_PERMISSION drop constraint FK_WF_PROC_ROLE_PERMISSION_TO_PROC_ROLE^
alter table WF_PROC_ROLE_PERMISSION add constraint FK_WF_PROC_ROLE_PER_TO_PRO_ROL foreign key (PROC_ROLE_TO_ID) references WF_PROC_ROLE (ID)^

alter table WF_PROC_ROLE_PERMISSION drop constraint FK_WF_PROC_ROLE_PERMISSION_FROM_PROC_ROLE^
alter table WF_PROC_ROLE_PERMISSION add constraint FK_WF_PROC_ROL_PER_FRO_PRO_ROL foreign key (PROC_ROLE_FROM_ID) references WF_PROC_ROLE (ID)^

alter table WF_PROC_APP_FOLDER drop constraint FK_WF_PROC_APP_FOLDER_APP_FOLDER^
alter table WF_PROC_APP_FOLDER add constraint FK_WF_PROC_APP_FOLDER_APP_FOL foreign key (FOLDER_ID) references SYS_APP_FOLDER (FOLDER_ID)^

alter table WF_USER_NOTIFIED_BY_SMS drop constraint FK_WF_USER_NOTIFIED_BY_SMS_USER^
alter table WF_USER_NOTIFIED_BY_SMS add constraint FK_WF_USER_NOTIFIED_BY_SMS_USE foreign key (USER_ID) references SEC_USER(ID)^

alter table WF_DESIGN_PROCESS_VARIABLE drop constraint FK_WF_DESIGN_PROCESS_VARIABLE_TO_WF_DESIGN^
alter table WF_DESIGN_PROCESS_VARIABLE add constraint FK_WF_DES_PROC_VAR_TO_WF_DES foreign key (DESIGN_ID) references WF_DESIGN(ID)^