/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.assignment;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

/**
 * @author krivopustov
 * @version $Id$
 */
public class AssignmentBrowser extends AbstractWindow {

    @Named("aTable")
    protected Table<Assignment> table;

    @Inject
    protected WindowConfig windowConfig;

    public void openAssignment() {
        Set<Assignment> selected = table.getSelected();
        if (selected.size() == 1) {
            Card card = selected.iterator().next().getCard();

            String windowAlias = windowConfig.getMetaClassScreenId(card.getMetaClass(), ".assignment");
            if (!windowConfig.hasWindow(windowAlias)) {
                windowAlias = windowConfig.getEditorScreenId(card.getMetaClass());
            }

            Window window = openEditor(windowAlias, card, WindowManager.OpenType.THIS_TAB);
            window.addCloseListener(actionId -> {
                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                    table.getDatasource().refresh();
                }
            });
        }
    }
}