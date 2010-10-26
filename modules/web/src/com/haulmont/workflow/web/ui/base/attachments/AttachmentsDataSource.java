/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Yuryi Artamonov
 * Created: 26.10.2010 17:43:04
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base.attachments;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.gui.data.DataService;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.workflow.core.entity.Attachment;

import java.util.List;
import java.util.Map;

public class AttachmentsDataSource extends CollectionDatasourceImpl {
    public AttachmentsDataSource(DsContext context, DataService dataservice, String id,
                                 MetaClass metaClass, String viewName) {
        super(context, dataservice, id, metaClass, viewName);
    }

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
