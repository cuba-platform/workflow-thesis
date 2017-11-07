-- Description: One Card - Many Processes enhancement (continues)

alter table WF_CARD add SUBSTITUTED_CREATOR_ID uuid;
alter table WF_CARD add constraint FK_WF_CARD_SUBSTITUTED_CREATOR foreign key (SUBSTITUTED_CREATOR_ID) references SEC_USER (ID);
