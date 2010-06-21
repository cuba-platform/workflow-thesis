/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 20.04.2010 13:04:43
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.gui.components.AbstractAccessData;

import java.util.Map;

public class TransitionFormAccessData extends AbstractAccessData {
    private Map<String, Object> params;

    public TransitionFormAccessData(Map<String, Object> params) {
        super(params);
        this.params = params;
    }

    public boolean isCommentVisible() {
        String commentVisible = (String)params.get("param$commentVisible");
        return commentVisible != null && Boolean.valueOf(commentVisible);
    }

    public boolean isCardRolesVisible() {
        String cardRolesVisible = (String)params.get("param$cardRolesVisible");
        return cardRolesVisible != null && Boolean.valueOf(cardRolesVisible);
    }

    public boolean isDueDateVisible() {
        String dueDateVisible = (String)params.get("param$dueDateVisible");
        return dueDateVisible != null && Boolean.valueOf(dueDateVisible);
    }

    public boolean isAttachmentsVisible() {
        String attachmentsVisible = (String)params.get("param$attachmentsVisible");
        return attachmentsVisible != null && Boolean.valueOf(attachmentsVisible);
    }

    public boolean isRefusedOnlyVisible() {
        String refusedOnlyVisible = (String)params.get("param$refusedOnlyVisible");
        return refusedOnlyVisible != null && Boolean.valueOf(refusedOnlyVisible);
    }
}
