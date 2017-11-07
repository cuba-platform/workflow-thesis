/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.NamePattern;

import javax.persistence.*;

/**
 *
 *
 */
@Entity(name = "wf$CardVariable")
@Table(name = "WF_CARD_VARIABLE")
@NamePattern("%s|name")
public class CardVariable extends AbstractProcessVariable {
    private static final long serialVersionUID = 4889538726688827424L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID")
    private Card card;

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }
}
