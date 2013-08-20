/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;
import java.util.Map;

/**
 * @author krivopustov
 * @version $Id$
 */
public class ResolutionEditor extends AbstractEditor {

    @Override
    public void setItem(Entity item) {
        super.setItem(item);
        Datasource assignmentDs = getDsContext().get("assignmentDs");
        Assignment assignment = (Assignment) assignmentDs.getItem();
        User currentUser = UserSessionClient.getUserSession().getCurrentOrSubstitutedUser();
        ((TextField) getComponent("comment")).setEditable(currentUser.equals(assignment.getUser()));
    }
}