/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
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
