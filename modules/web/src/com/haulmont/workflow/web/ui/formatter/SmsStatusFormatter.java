/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.web.ui.formatter;

import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.components.Formatter;
import com.haulmont.workflow.core.enums.SmsStatus;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
public class SmsStatusFormatter implements Formatter {
    @Override
    public String format(Object value) {
        SmsStatus status = (SmsStatus) value;
        if (status == null)
            return "";
        return MessageProvider.getMessage(status);
    }
}
