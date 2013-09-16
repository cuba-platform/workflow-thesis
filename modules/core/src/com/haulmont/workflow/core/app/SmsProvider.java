/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
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
