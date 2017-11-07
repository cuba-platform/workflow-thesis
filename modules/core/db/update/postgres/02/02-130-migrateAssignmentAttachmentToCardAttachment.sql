-- Description:

update WF_ATTACHMENT set CARD_ID = (select a.card_id from WF_ASSIGNMENT a where a.id = assignment_id), type = 'C' where CARD_ID is NULL and type = 'A';