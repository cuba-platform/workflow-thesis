/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.jmx;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */

public interface SmsManagerMBean {

    int getDelayCallCount();

    int getMessageQueueCapacity();

    boolean getUseSmsSending();

    void setUseSmsSending(boolean value);
}
