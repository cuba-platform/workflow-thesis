/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;


import com.haulmont.workflow.core.entity.SendingSms;

import java.util.List;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
public interface SmsManagerAPI {
    String NAME = "workflow_SmsManager";

    List<SendingSms> addSmsToQueue(List<SendingSms> sendingSmsList);

    void queueSmsToSend();
}
