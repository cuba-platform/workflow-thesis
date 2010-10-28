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

import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.workflow.core.entity.Attachment;

import java.util.*;
import java.util.List;

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
        return new AbstractAction("actions.Copy") {
            public void actionPerform(Component component) {
                Set descriptors = attachments.getSelected();
                if (descriptors.size() > 0) {
                    ArrayList<Attachment> selected = new ArrayList<Attachment>();
                    Iterator iter = descriptors.iterator();
                    while (iter.hasNext()) {
                        selected.add((Attachment) iter.next());
                    }
                    AttachmentCopyHelper.put(selected);
                    String info;
                    if (descriptors.size() == 1)
                        info = MessageProvider.getMessage(getClass(), "messages.copyInfo");
                    else
                        info = MessageProvider.getMessage(getClass(), "messages.manyCopyInfo");
                    attachments.getFrame().showNotification(info, IFrame.NotificationType.HUMANIZED);
                }
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
        return new AbstractAction("actions.Paste") {
            public void actionPerform(Component component) {
                List<Attachment> buffer = AttachmentCopyHelper.get();
                if ((buffer != null) && (buffer.size() > 0)) {
                    for (Attachment attach : buffer) {
                        Attachment attachment = propsSetter.createObject();
                        attachment.setFile(attach.getFile());
                        attachment.setComment(attach.getComment());
                        attachment.setName(attach.getName());
                        attachment.setUuid(UUID.randomUUID());

                        UUID fileUid = attach.getFile().getUuid();
                        Object[] ids = attachments.getDatasource().getItemIds().toArray();
                        boolean find = false;
                        int i = 0;
                        while ((i < ids.length) && !find) {
                            Attachment obj = (Attachment)attachments.getDatasource().getItem(ids[i]);
                            find = obj.getFile().getUuid() == fileUid;
                            i++;
                        }
                        if (!find) {
                            attachments.getDatasource().addItem(attachment);
                            attachments.refresh();
                        }
                    }
                } else {
                    String info = MessageProvider.getMessage(getClass(), "messages.bufferEmptyInfo");
                    attachments.getFrame().showNotification(info, IFrame.NotificationType.HUMANIZED);
                }
            }
        };
    }
}
