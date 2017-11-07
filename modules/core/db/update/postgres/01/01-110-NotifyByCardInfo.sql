-- Description: adding fields required for notifications

alter table WF_CARD_INFO add DELETE_TS timestamp
^
alter table WF_CARD_INFO add DELETED_BY varchar(50)
^
alter table WF_CARD_INFO add DESCRIPTION varchar(500)
^

alter table WF_CARD_ROLE add NOTIFY_BY_CARD_INFO boolean
^
