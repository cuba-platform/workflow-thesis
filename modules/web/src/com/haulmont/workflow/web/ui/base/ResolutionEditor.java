/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 20.01.2010 12:43:01
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.web.app.FileDownloadHelper;
import com.haulmont.workflow.core.entity.Assignment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ResolutionEditor extends AbstractEditor {

    private Table attachmentsTable;

    public ResolutionEditor(IFrame frame) {
        super(frame);
    }

    @Override
    protected void init(Map<String, Object> params) {
        super.init(params);
        attachmentsTable = getComponent("attachmentsTable");
        TableActionsHelper attachmentsTH = new TableActionsHelper(this, attachmentsTable);
        attachmentsTH.createCreateAction(
                new ValueProvider() {
                    public Map<String, Object> getValues() {
                        Map<String, Object> values = new HashMap<String, Object>();
                        values.put("assignment", getDsContext().get("assignmentDs").getItem());
                        values.put("file", new FileDescriptor());
                        return values;
                    }
                    public Map<String, Object> getParameters() {
                        return Collections.emptyMap();
                    }
                },
                WindowManager.OpenType.DIALOG);
        attachmentsTH.createEditAction(WindowManager.OpenType.DIALOG);
        attachmentsTH.createRemoveAction(false);
    }

    @Override
    public void setItem(Entity item) {
        super.setItem(item);
        FileDownloadHelper.initGeneratedColumn(attachmentsTable, "file");

        Assignment assignment = (Assignment) item;
        boolean editable = assignment.getFinished() == null
                && assignment.getUser() != null
                && UserSessionClient.currentOrSubstitutedUserId().equals(assignment.getUser().getId());

        TextField commentText = getComponent("comment");
        commentText.setEditable(editable);

        for (Action action : attachmentsTable.getActions()) {
            action.setEnabled(editable);
        }
    }
}
