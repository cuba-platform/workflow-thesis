-- Description:

update WF_CARD w set HAS_ATTACHMENTS = true where w.id in (select wf.id from WF_CARD wf join WF_ATTACHMENT a on a.card_id = wf.id)^