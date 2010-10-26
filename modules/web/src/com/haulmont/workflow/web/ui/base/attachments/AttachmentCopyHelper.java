/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Yuryi Artamonov
 * Created: 22.10.2010 16:42:35
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base.attachments;

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