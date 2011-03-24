/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 20.01.2010 11:37:53
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.annotation.Listeners;

import javax.persistence.*;

@Entity(name = "wf$CardAttachment")
@DiscriminatorValue("C")
@Listeners("com.haulmont.workflow.core.listeners.CardAttachmentEntityListener")
@NamePattern("%s|name")
public class CardAttachment extends Attachment {

    private static final long serialVersionUID = -6196909618841405629L;

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
