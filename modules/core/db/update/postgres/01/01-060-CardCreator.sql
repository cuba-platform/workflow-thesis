-- Description: Creating property creator in card

alter table WF_CARD add column CREATOR_ID uuid;
alter table WF_CARD add constraint FK_WF_CARD_USER foreign key (CREATOR_ID) references SEC_USER (ID);