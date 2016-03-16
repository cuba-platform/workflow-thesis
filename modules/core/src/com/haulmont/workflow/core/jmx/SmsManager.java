/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.jmx;

import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.security.app.Authenticated;
import com.haulmont.workflow.core.app.SmsSenderConfig;

import org.springframework.stereotype.Component;
import javax.inject.Inject;

/**
 */
@Component("workflow_SmsManagerMBean")
public class SmsManager implements SmsManagerMBean {

    protected SmsSenderConfig config;

    @Inject
    public void setConfiguration(Configuration configuration) {
        this.config = configuration.getConfig(SmsSenderConfig.class);
    }

    @Override
    public int getDelayCallCount() {
        return config.getDelayCallCount();
    }

    @Override
    public int getMessageQueueCapacity() {
        return config.getMessageQueueCapacity();
    }

    @Override
    public boolean getUseSmsSending() {
        return config.getUseSmsSending();
    }

    @Authenticated
    @Override
    public void setUseSmsSending(boolean value) {
        config.setUseSmsSending(value);
    }
}
