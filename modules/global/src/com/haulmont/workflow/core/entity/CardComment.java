/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Valery Novikov
 * Created: 06.08.2010 15:37:08
 *
 * $Id$
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.Aggregation;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;
import java.util.List;

@Entity(name = "wf$CardComment")
@Table(name = "WF_CARD_COMMENT")
public class CardComment extends StandardEntity {

    @Column(name = "COMMENT", length = 1000)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID")
    private Card card;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    private CardComment parent;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "WF_CARD_COMMENT_USER",
            joinColumns = @JoinColumn(name = "CARD_COMMENT_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID"))
    @Aggregation
    private List<User> addressees;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public CardComment getParent() {
        return parent;
    }

    public void setParent(CardComment parent) {
        this.parent = parent;
    }

    public List<User> getAddressees() {
        return addressees;
    }

    public void setAddressees(List<User> addressees) {
        this.addressees = addressees;
    }
}
