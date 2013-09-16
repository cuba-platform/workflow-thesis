/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;
import com.haulmont.cuba.core.config.defaults.DefaultBoolean;
import com.haulmont.cuba.core.config.defaults.DefaultInt;

/**
 * @author novikov
 * @version $Id$
 */
@Source(type = SourceType.DATABASE)
public interface SmsSenderConfig extends Config {

    /**
     * Used in server startup.
     * Sending smses will be started after delayCallCount cron ticks (used to not overload server in startup)
     */
    @Property("workflow.sms.delayCallCount")
    @Default("2")
    int getDelayCallCount();

    /**
     * MaxResults query limit for load messages from DB in one tick
     */
    @Property("workflow.sms.messageQueueCapacity")
    @Default("100")
    int getMessageQueueCapacity();

    /**
     * @return Number of sending attempts
     */
    @Property("workflow.sms.defaultSendingAttemptsCount")
    @DefaultInt(10)
    int getDefaultSendingAttemptsCount();

    /**
     * Use SmsMenager
     */
    @Property("workflow.sms.useSmsSending")
    @DefaultBoolean(false)
    boolean getUseSmsSending();
    void setUseSmsSending(boolean value);

    /**
     * Use default SmsProvider
     */
    @Property("workflow.sms.useDefaultSmsSending")
    @DefaultBoolean(false)
    boolean getUseDefaultSmsSending();
    void setUseDefaultSmsSending(boolean value);

    /**
     * Class name SmsProvider
     */
    @Property("workflow.sms.smsProviderClassName")
    @Default("com.haulmont.workflow.core.app.DefaultSmsProvider")
    String getSmsProviderClassName();
    void setSmsProviderClassName(String value);

    @Property("workflow.sms.maxSendingTimeSec")
    @DefaultInt(86400)
    int getMaxSendingTimeSec();

    @Property("workflow.sms.smsMaxParts")
    @Default("1")
    int getSmsMaxParts();

    void setSmsMaxParts(int value);
}
