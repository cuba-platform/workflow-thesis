/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.base.attachments;

import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.Card;

public interface AttachmentCreator {
    Attachment createObject();

    public interface CardAttachmentCreator extends AttachmentCreator, CardGetter {}

    public interface CardGetter {
        Card getCard();
    }
}
