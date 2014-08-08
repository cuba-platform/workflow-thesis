/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.annotation.Listeners;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;

import javax.persistence.*;

@Entity(name = "wf$CardAttachment")
@DiscriminatorValue("C")
@Listeners("workflow_CardAttachmentEntityListener")
@NamePattern("%s|name")
@SystemLevel
public class CardAttachment extends Attachment {

    private static final long serialVersionUID = -6196909618841405629L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID")
    protected Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSIGNMENT_ID")
    protected Assignment assignment;

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }
}
