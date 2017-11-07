-- Description: Creating property parentCard in card

alter table WF_CARD add column PARENT_CARD_ID uuid;
alter table WF_CARD add constraint FK_WF_CARD_CARD foreign key (PARENT_CARD_ID) references WF_CARD (ID);