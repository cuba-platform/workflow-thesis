/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 26.01.2010 16:58:52
 *
 * $Id$
 */
package com.haulmont.workflow.core.timer;

import com.haulmont.workflow.core.entity.Card;

import java.util.Date;
import java.util.Map;

public class TimerActionContext {

    private final Card card;
    private final String jbpmExecutionId;
    private final String activity;
    private final Date dueDate;
    private final Map<String, String> params;

    public TimerActionContext(Card card, String jbpmExecutionId, String activity, Date dueDate, Map<String, String> params) {
        this.activity = activity;
        this.card = card;
        this.dueDate = dueDate;
        this.jbpmExecutionId = jbpmExecutionId;
        this.params = params;
    }

    public String getActivity() {
        return activity;
    }

    public Card getCard() {
        return card;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getJbpmExecutionId() {
        return jbpmExecutionId;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
