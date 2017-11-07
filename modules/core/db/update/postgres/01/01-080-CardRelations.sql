-- Description: Creating Card Relation Table

create table WF_CARD_RELATION (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    CARD_ID uuid,
    RELATED_CARD_ID uuid,
    primary key (ID)
);

alter table WF_CARD_RELATION add constraint FK_WF_CC_CARD foreign key (CARD_ID) references WF_CARD (ID);
alter table WF_CARD_RELATION add constraint FK_WF_CC_CARD_RELATED foreign key (RELATED_CARD_ID) references WF_CARD (ID);