/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.jmx;

import com.haulmont.workflow.core.exception.SmsException;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
public interface SmsSenderMBean {
    String sendSms(String phone, String addressee, String message);

    String checkStatus(String smsId);

    String getBalance() throws SmsException;

    boolean getUseDefaultSmsSending();

    void setUseDefaultSmsSending(boolean value);

    String getSmsProviderClassName();

    void setSmsProviderClassName(String value);

    int getSmsMaxParts();

    void setSmsMaxParts(int value);
}
