/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.gui.app.base;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.TextArea;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;

import javax.inject.Inject;

public class ResolutionEditor extends AbstractEditor {
    @Inject
    protected UserSessionSource userSessionSource;

    @Override
    public void setItem(Entity item) {
        super.setItem(item);
        Datasource assignmentDs = getDsContext().get("assignmentDs");
        Assignment assignment = (Assignment) assignmentDs.getItem();
        User currentUser = userSessionSource.getUserSession().getCurrentOrSubstitutedUser();
        ((TextArea) getComponent(getCommentField())).setEditable(currentUser.equals(assignment.getUser())
                && assignment.getFinished() == null);
    }

    protected String getCommentField() {
        return "comment";
    }
}