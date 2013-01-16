/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.web.ui.base.attachments;

import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.workflow.core.entity.Attachment;

import java.util.List;
import java.util.Map;

/**
 * @author artamonov
 * @version $Id$
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
