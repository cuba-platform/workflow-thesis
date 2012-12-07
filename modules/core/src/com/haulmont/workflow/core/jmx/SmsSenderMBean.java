/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.jmx;

import com.haulmont.workflow.core.exception.SmsException;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
public interface SmsSenderMBean {
    String sendSms(String phone,String addressee, String message);

    String checkStatus(String smsId);

    String getBalance() throws SmsException;

    boolean getUseDefaultSmsSending();

    void setUseDefaultSmsSending(boolean value);

    String getSmsProviderClassName();

    void setSmsProviderClassName(String value);
}
