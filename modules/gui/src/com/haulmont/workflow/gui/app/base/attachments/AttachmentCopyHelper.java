/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.base.attachments;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.workflow.core.entity.Attachment;

import java.util.ArrayList;
import java.util.List;

public class AttachmentCopyHelper {
    private static String FILE_DESCRIPTOR_BUFFER = "FILE_DESCRIPTOR_BUFFER";

    private AttachmentCopyHelper(){}

    public static void put(ArrayList<Attachment> items) {
        AppBeans.get(UserSessionSource.class).getUserSession().setAttribute(FILE_DESCRIPTOR_BUFFER,items);
    }

    public static java.util.List<Attachment> take() {
        UserSessionSource userSessionSource = AppBeans.get(UserSessionSource.class);
        List<Attachment> lst = userSessionSource.getUserSession().getAttribute(FILE_DESCRIPTOR_BUFFER);
        userSessionSource.getUserSession().removeAttribute(FILE_DESCRIPTOR_BUFFER);
        return lst;
    }

    public static java.util.List<Attachment> get() {
        return AppBeans.get(UserSessionSource.class).getUserSession().getAttribute(FILE_DESCRIPTOR_BUFFER);
    }
}