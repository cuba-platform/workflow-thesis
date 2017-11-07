/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.global;

import com.google.common.collect.Lists;
import com.haulmont.workflow.core.entity.Card;

import java.io.Serializable;
import java.util.List;

public class ReassignInfo implements Serializable {

    private static final long serialVersionUID = -2178542775074131046L;

    protected Card card;
    protected String role;
    protected String state;
    protected List<String> visibleRoles = Lists.newArrayList();
    protected boolean commentVisible;
    protected boolean commentRequired;

    public ReassignInfo(Card card, String role, List<String> visibleRoles, boolean commentVisible, boolean commentRequired) {
        this.card = card;
        this.role = role;
        this.visibleRoles = visibleRoles;
        this.commentVisible = commentVisible;
        this.commentRequired = commentRequired;
    }

    public ReassignInfo(Card card, String role, List<String> visibleRoles) {
        this(card, role, visibleRoles, false, false);
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getVisibleRoles() {
        return visibleRoles;
    }

    public void setVisibleRoles(List<String> visibleRoles) {
        this.visibleRoles = visibleRoles;
    }

    public boolean isCommentVisible() {
        return commentVisible;
    }

    public void setCommentVisible(boolean commentVisible) {
        this.commentVisible = commentVisible;
    }

    public boolean isCommentRequired() {
        return commentRequired;
    }

    public void setCommentRequired(boolean commentRequired) {
        this.commentRequired = commentRequired;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
