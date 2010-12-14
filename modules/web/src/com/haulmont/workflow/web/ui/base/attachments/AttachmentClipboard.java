/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Yuryi Artamonov
 * Created: 26.10.2010 17:42:05
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base.attachments;

import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.workflow.core.entity.Attachment;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttachmentClipboard extends AbstractEditor {
    public AttachmentClipboard(IFrame frame) {
        super(frame);
    }

    @Override
    protected void init(Map<String, Object> params) {
        super.init(params);

        Button removeAttachBtn = getComponent("removeAttach");
        final Table attachmentsTable = getComponent("attachmentsTable");
        if (attachmentsTable != null)
            AttachmentColumnGeneratorHelper.addSizeGeneratedColumn(attachmentsTable);
        removeAttachBtn.setAction(new AbstractAction("removeAttach") {
            public void actionPerform(Component component) {
                Set selected = attachmentsTable.getSelected();
                List<Attachment> buffer = AttachmentCopyHelper.get();
                Iterator iter = selected.iterator();
                while (iter.hasNext()) {
                    Attachment attach = (Attachment) iter.next();
                    if (buffer.contains(attach))
                        buffer.remove(attach);
                }
                attachmentsTable.refresh();
            }
        });
        removeAttachBtn.setCaption(MessageProvider.getMessage(getClass(), "actions.Remove"));

        Button clearAttachBtn = getComponent("clearAttach");
        clearAttachBtn.setAction(new AbstractAction("clearAttach") {
            public void actionPerform(Component component) {
                Set selected = attachmentsTable.getSelected();
                List<Attachment> buffer = AttachmentCopyHelper.get();
                if (buffer != null)
                    buffer.clear();
                attachmentsTable.refresh();
            }
        });
        clearAttachBtn.setCaption(MessageProvider.getMessage(getClass(), "actions.Clear"));

        Button refreshAttachBtn = getComponent("refreshAttach");
        refreshAttachBtn.setAction(new AbstractAction("refreshAttach") {
            public void actionPerform(Component component) {
                attachmentsTable.refresh();
            }
        });
        refreshAttachBtn.setCaption(MessageProvider.getMessage(getClass(), "actions.Refresh"));
    }
}
