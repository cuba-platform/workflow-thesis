/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.exception.SmsException;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */

public interface SmsProvider {

    String sendSms(String phone, String message) throws SmsException;

    String checkStatus(String smsId) throws SmsException;

    String getBalance() throws SmsException;
}
