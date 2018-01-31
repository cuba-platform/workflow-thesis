/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "wf$Timer")
@Table(name = "WF_TIMER")
@SystemLevel
public class TimerEntity extends BaseUuidEntity {

    private static final long serialVersionUID = 4721435849536800174L;

    @Column(name = "DUE_DATE")
    protected Date dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID")
    protected Card card;

    @Column(name = "JBPM_EXECUTION_ID", length = 255)
    protected String jbpmExecutionId;

    @Column(name = "ACTIVITY", length = 255)
    protected String activity;

    @Column(name = "FACTORY_CLASS", length = 0)
    protected String factoryClass;

    @Column(name = "ACTION_CLASS", length = 200)
    protected String actionClass;

    @Column(name = "ACTION_PARAMS", length = 1000)
    protected String actionParams;

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getJbpmExecutionId() {
        return jbpmExecutionId;
    }

    public void setJbpmExecutionId(String jbpmExecutionId) {
        this.jbpmExecutionId = jbpmExecutionId;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getActionClass() {
        return actionClass;
    }

    public void setActionClass(String actionClass) {
        this.actionClass = actionClass;
    }

    public String getActionParams() {
        return actionParams;
    }

    public void setActionParams(String actionParams) {
        this.actionParams = actionParams;
    }

    public String getFactoryClass() {
        return factoryClass;
    }

    public void setFactoryClass(String factoryClass) {
        this.factoryClass = factoryClass;
    }
}
