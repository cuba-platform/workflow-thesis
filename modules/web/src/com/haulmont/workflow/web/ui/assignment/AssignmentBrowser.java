/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.ui.assignment;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.RefreshAction;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;

import javax.inject.Named;
import java.util.Map;
import java.util.Set;

/**
 * @author krivopustov
 * @version $Id$
 */
public class AssignmentBrowser extends AbstractWindow {

    @Named("aTable")
    protected Table table;

    public void init(Map<String, Object> params) {
        super.init(params);
        table.addAction(new RefreshAction(table));
        table.addAction(new AbstractAction("open") {
            @Override
            public void actionPerform(Component component) {
                Set<Assignment> selected = table.getSelected();
                if (selected.size() == 1) {
                    Card card = selected.iterator().next().getCard();
                    Window window = openEditor("wf$Card.edit", card, WindowManager.OpenType.THIS_TAB);
                    window.addListener(new CloseListener() {
                        @Override
                        public void windowClosed(String actionId) {
                            if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                                table.getDatasource().refresh();
                            }
                        }
                    });
                }
            }
        });
    }
}