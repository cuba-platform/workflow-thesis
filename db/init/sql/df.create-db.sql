create table DF_DOC (
    CARD_ID varchar(36),
    NUMBER varchar(50),
    DATE timestamp,
    primary key (CARD_ID)
);

alter table DF_DOC add constraint FK_DF_DOC_CARD foreign key (CARD_ID) references WF_CARD (ID);
