/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.gui.components.AbstractAccessData;
import com.haulmont.workflow.core.entity.Card;
import org.apache.commons.lang.BooleanUtils;

import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class TransitionFormAccessData extends AbstractAccessData {
    private Map<String, Object> params;

    public TransitionFormAccessData(Map<String, Object> params) {
        super(params);
        this.params = params;
    }

    public boolean isCommentVisible() {
        String commentVisible = (String) params.get("commentVisible");
        return commentVisible != null && Boolean.valueOf(commentVisible);
    }

    public boolean isCardRolesVisible() {
        String cardRolesVisible = (String) params.get("cardRolesVisible");
        return cardRolesVisible != null && Boolean.valueOf(cardRolesVisible);
    }

    public boolean isDueDateVisible() {
        //If duration in process is enabled we won't show duration field in transition form.
        //Assignments' dueDates will be set by roles' durations.
        Card card = (Card) params.get("card");
        if (card != null && card.getProc() != null && BooleanUtils.isTrue(card.getProc().getDurationEnabled())) {
            return false;
        }
        String dueDateVisible = (String) params.get("dueDateVisible");
        return dueDateVisible != null && Boolean.valueOf(dueDateVisible);
    }

    public boolean isAttachmentsVisible() {
        String attachmentsVisible = (String) params.get("attachmentsVisible");
        return attachmentsVisible != null && Boolean.valueOf(attachmentsVisible);
    }

    public boolean isRefusedOnlyVisible() {
        String refusedOnlyVisible = (String) params.get("refusedOnlyVisible");
        return refusedOnlyVisible != null && Boolean.valueOf(refusedOnlyVisible);
    }
}
