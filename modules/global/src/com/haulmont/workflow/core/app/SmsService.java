/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
public interface SmsService {

    public static String NAME = "workflow_SmsService";

    /**
     * Sends an sms message.
     *
     * @param phone     phone number
     * @param addressee addressee
     * @param message   message text
     */
    void sendSms(String phone, String addressee, String message);
}
