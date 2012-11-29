/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Prefix;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;
import com.haulmont.cuba.core.config.defaults.DefaultBoolean;
import com.haulmont.cuba.core.config.defaults.DefaultInt;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
@Prefix("workflow.sms.")
@Source(type = SourceType.DATABASE)
public interface SmsSenderConfig extends Config {
    /**
     * Used in server startup.
     * Sending smses will be started after delayCallCount cron ticks (used to not overload server in startup)
     */
    @Default("2")
    int getDelayCallCount();

    /**
     * MaxResults query limit for load messages from DB in one tick
     */
    @Default("100")
    int getMessageQueueCapacity();

    /**
     * @return Quantity of sending attempts
     */
    @DefaultInt(10)
    int getDefaultSendingAttemptsCount();

    /**
     * Use SmsMenager
     *
     * @return
     */
    @DefaultBoolean(false)
    boolean getUseSmsSending();

    void setUseSmsSending(boolean value);

    /**
     * Use SmsProvider by default
     *
     * @return
     */
    @DefaultBoolean(false)
    boolean getUseDefaultSmsSending();

    void setUseDefaultSmsSending(boolean value);

    /**
     * Class name SmsProvider
     *
     * @return
     */
    @Default("com.haulmont.workflow.core.app.DefaultSmsProvider")
    String getSmsProviderClassName();

    void setSmsProviderClassName(String value);

    @DefaultInt(86400)
    int getMaxSendingTimeSec();
}
