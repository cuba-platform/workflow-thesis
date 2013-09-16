/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.global;

import com.haulmont.workflow.core.entity.Card;

import java.io.Serializable;
import java.util.List;

/**
 * @author subbotin
 * @version $Id$
 */
public class ReassignInfo implements Serializable {

    private static final long serialVersionUID = -2178542775074131046L;

    private Card card;
    private String role;
    private List<String> visibleRoles;
    private boolean commentVisible;
    private boolean commentRequired;

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

    public String getRole() {
        return role;
    }

    public List<String> getVisibleRoles() {
        return visibleRoles;
    }

    public boolean isCommentVisible() {
        return commentVisible;
    }

    public boolean isCommentRequired() {
        return commentRequired;
    }
}
