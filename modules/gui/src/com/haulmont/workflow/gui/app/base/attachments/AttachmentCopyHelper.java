/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.base.attachments;

import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.workflow.core.entity.Attachment;

import java.util.ArrayList;
import java.util.List;

public class AttachmentCopyHelper {
    private static String FILE_DESCRIPTOR_BUFFER = "FILE_DESCRIPTOR_BUFFER";

    private AttachmentCopyHelper(){}

    public static void put(ArrayList<Attachment> items) {
        UserSessionClient.getUserSession().setAttribute(FILE_DESCRIPTOR_BUFFER,items);
    }

    public static java.util.List<Attachment> take() {
        List<Attachment> lst = UserSessionClient.getUserSession().getAttribute(FILE_DESCRIPTOR_BUFFER);
        UserSessionClient.getUserSession().removeAttribute(FILE_DESCRIPTOR_BUFFER);
        return lst;
    }

    public static java.util.List<Attachment> get() {
        return UserSessionClient.getUserSession().getAttribute(FILE_DESCRIPTOR_BUFFER);
    }
}