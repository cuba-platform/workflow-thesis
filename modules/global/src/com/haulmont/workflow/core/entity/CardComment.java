/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.Composition;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserFormatTools;
import com.haulmont.cuba.security.entity.User;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.util.List;

@Entity(name = "wf$CardComment")
@Table(name = "WF_CARD_COMMENT")
@SystemLevel
public class CardComment extends StandardEntity {

    @Lob
    @Column(name = "CARD_COMMENT", length = 100000)
    protected String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    protected User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUBSTITUTED_USER_ID")
    protected User substitutedSender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID")
    protected Card card;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    protected CardComment parent;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "WF_CARD_COMMENT_USER",
            joinColumns = @JoinColumn(name = "CARD_COMMENT_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID"))
    @Composition
    protected List<User> addressees;

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

    public User getSubstitutedSender() {
        return substitutedSender;
    }

    public void setSubstitutedSender(User substitutedSender) {
        this.substitutedSender = substitutedSender;
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

    @MetaProperty
    public String getDisplayUser() {
        if (sender == null) {
            return "";
        }

        UserFormatTools formatTools = AppBeans.get(UserFormatTools.NAME);
        return formatTools.formatUser(sender, substitutedSender);
    }

    private String userNameOrLogin(User user) {
        return StringUtils.isBlank(user.getName()) ? user.getLogin() : user.getName();
    }
}
