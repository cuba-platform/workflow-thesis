-- Description: WF_ATTACHMENTTYPE fix default type code
update WF_ATTACHMENTTYPE set CODE = 'AttachmentType.attachment' where CODE = 'Attachment_type.attachment'