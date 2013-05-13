/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Yuryi Artamonov
 * Created: 22.10.2010 17:50:53
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base.attachments;

import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.Card;

public interface AttachmentCreator {
    Attachment createObject();

    public interface CardAttachmentCreator extends AttachmentCreator, CardGetter {}

    public interface CardGetter {
        Card getCard();
    }
}
