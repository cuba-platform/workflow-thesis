/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.base.attachments;

import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.workflow.core.entity.Attachment;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author artamonov
 * @version $Id$
 */
public class AttachmentClipboard extends AbstractEditor {

    @Override
    public void init(Map<String, Object> params) {
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
