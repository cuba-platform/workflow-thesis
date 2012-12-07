/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
public interface SmsService {

    public static String NAME = "workflow_SmsService";

    void sendSms(String phone, String addressee, String message);
}
