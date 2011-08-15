/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 12.01.11 17:49
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "wf$CardStage")
@Table(name = "WF_CARD_STAGE")
@NamePattern("%s|procStage.name")
public class CardStage extends StandardEntity {
    private static final long serialVersionUID = 7689780973392226763L;

    @Column(name = "START_DATE")
    protected Date startDate;

    @Column(name = "END_DATE_PLAN")
    protected Date endDatePlan;

    @Column(name = "END_DATE_FACT")
    protected Date endDateFact;

    @Column(name = "NOTIFIED")
    protected Boolean notified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_STAGE_ID")
    protected ProcStage procStage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID")
    protected Card card;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDatePlan() {
        return endDatePlan;
    }

    public void setEndDatePlan(Date endDatePlan) {
        this.endDatePlan = endDatePlan;
    }

    public Date getEndDateFact() {
        return endDateFact;
    }

    public void setEndDateFact(Date endDateFact) {
        this.endDateFact = endDateFact;
    }

    public ProcStage getProcStage() {
        return procStage;
    }

    public void setProcStage(ProcStage procStage) {
        this.procStage = procStage;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Boolean getNotified() {
        return notified;
    }

    public void setNotified(Boolean notified) {
        this.notified = notified;
    }
}
