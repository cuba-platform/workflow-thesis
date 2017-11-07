-- Description: index on CardInfo

drop index if exists IDX_WF_CARD_INFO_CARD^

create index IDX_WF_CARD_INFO_CARD on WF_CARD_INFO(card_id)^