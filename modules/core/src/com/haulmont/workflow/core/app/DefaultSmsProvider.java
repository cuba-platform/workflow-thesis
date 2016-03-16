/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.exception.SmsException;

import java.util.UUID;

/**
 *
 */
public class DefaultSmsProvider implements SmsProvider {
    @Override
    public String sendSms(String phone, String message) throws SmsException {
        return UUID.randomUUID().toString();
    }

    @Override
    public String checkStatus(String smsId) throws SmsException {
        return "Delivered";
    }

    @Override
    public String getBalance() throws SmsException {
        return "100";
    }
}
