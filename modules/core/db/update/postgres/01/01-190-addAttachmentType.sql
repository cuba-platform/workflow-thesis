-- Description: WF_ATTACHMENTTYPE created
create table WF_ATTACHMENTTYPE (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    VERSION integer,
    NAME varchar(500),
    COMMENT varchar(1000),
    ISDEFAULT boolean,
    CODE varchar(200),
    primary key (ID)
)^

alter table WF_ATTACHMENT add TYPE_ID uuid^

alter table WF_ATTACHMENT add constraint FK_WF_ATTACHMENT_TYPE foreign key (TYPE_ID)
references WF_ATTACHMENTTYPE (ID)^

insert into WF_ATTACHMENTTYPE (ID,CODE,ISDEFAULT)
values ('6c9c8ccc-e761-11df-94cb-6f884bc56e70','Attachment_type.attachment',true)^

update WF_ATTACHMENT set TYPE_ID = att.id from WF_ATTACHMENTTYPE att where att.id = '6c9c8ccc-e761-11df-94cb-6f884bc56e70'^