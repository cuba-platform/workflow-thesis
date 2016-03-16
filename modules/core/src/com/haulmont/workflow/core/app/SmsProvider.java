/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.exception.SmsException;

/**
 *
 */

public interface SmsProvider {

    String sendSms(String phone, String message) throws SmsException;

    String checkStatus(String smsId) throws SmsException;

    String getBalance() throws SmsException;
}
