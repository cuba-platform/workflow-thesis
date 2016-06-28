/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.gui.app.sms.formatter;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.Formatter;
import com.haulmont.workflow.core.enums.SmsStatus;

public class SmsStatusFormatter implements Formatter {
    @Override
    public String format(Object value) {
        SmsStatus status = (SmsStatus) value;
        if (status == null)
            return "";
        return AppBeans.get(Messages.class).getMessage(status);
    }
}
