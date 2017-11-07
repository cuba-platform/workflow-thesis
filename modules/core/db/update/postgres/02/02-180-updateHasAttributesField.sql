-- Description: update field HAS_ATTRIBUTES in WF_CARD

update WF_CARD w set HAS_ATTRIBUTES = true where w.category_id in (select distinct category_id from SYS_CATEGORY_ATTR);
update WF_CARD set HAS_ATTRIBUTES = false where HAS_ATTRIBUTES is NULL;