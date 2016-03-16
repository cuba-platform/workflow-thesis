/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.jmx;

/**
 *
 */

public interface SmsManagerMBean {

    int getDelayCallCount();

    int getMessageQueueCapacity();

    boolean getUseSmsSending();

    void setUseSmsSending(boolean value);
}
