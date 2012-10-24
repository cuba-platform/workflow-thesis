/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.formatter;

import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.components.Formatter;
import com.haulmont.workflow.core.exception.SmsException;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
public class SmsExceptionFormatter implements Formatter {
    @Override
    public String format(Object value) {
        int i = (Integer) value;
        if (i == 0)
            return "";
        else
            return MessageProvider.getMessage(SmsException.class,"smsException." + i);
    }
}
