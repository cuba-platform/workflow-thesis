/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Yuryi Artamonov
 * Created: 22.10.2010 17:40:34
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base.attachments;

import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.workflow.core.entity.Attachment;

import java.util.*;

// Create copy/paste buttons actions for attachments table
public class AttachmentCopyButtons {
    private AttachmentCopyButtons() {
    }

    /* Create copy attachment action for table
     * @param attachmentsTable Table with attachments
     * @return Action
     */
    public static Action createCopyAction(Table attachmentsTable) {
        final Table attachments = attachmentsTable;
        return new AbstractAction("copyAttachment") {
            public void actionPerform(Component component) {
                Set descriptors = attachments.getSelected();
                ArrayList<Attachment> selected = new ArrayList<Attachment>();
                Iterator iter = descriptors.iterator();
                while (iter.hasNext()) {
                    selected.add((Attachment) iter.next());
                }
                AttachmentCopyHelper.put(selected);
            }
        };
    }

    /* Create paste attachment action for table
     * @param attachmentsTable Table with attachments
     * @param creator Custom method for set object properties
     * @return Action
     */
    public static Action createPasteAction(Table attachmentsTable, AttachmentCreator creator) {
        final Table attachments = attachmentsTable;
        final AttachmentCreator propsSetter = creator;
        return new AbstractAction("pasteAttachment") {
            public void actionPerform(Component component) {
                List<Attachment> buffer = AttachmentCopyHelper.get();
                if (buffer != null) {
                    for (Attachment attach : buffer) {
                        Attachment attachment = propsSetter.createObject();
                        attachment.setFile(attach.getFile());
                        attachment.setComment(attach.getComment());
                        attachment.setName(attach.getName());
                        attachment.setUuid(UUID.randomUUID());
                        attachments.getDatasource().addItem(attachment);
                        attachments.refresh();
                    }
                }
            }
        };
    }
}
