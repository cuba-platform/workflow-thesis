/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
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
