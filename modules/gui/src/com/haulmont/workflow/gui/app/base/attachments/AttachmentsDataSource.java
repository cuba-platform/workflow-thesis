/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.gui.app.base.attachments;

import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.workflow.core.entity.Attachment;

import java.util.List;
import java.util.Map;

/**
 */
public class AttachmentsDataSource extends CollectionDatasourceImpl {

    @Override
    protected void loadData(Map params) {
        List<Attachment> attachments = AttachmentCopyHelper.get();
        data.clear();
        if (attachments != null)
            for (Attachment attach : attachments) {
                data.put(attach.getId(), attach);
            }
    }

    @Override
    public void commit() {
    }
}
