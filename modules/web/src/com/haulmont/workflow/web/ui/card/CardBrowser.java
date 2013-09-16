/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.ui.card;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.RefreshAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.web.App;
import com.haulmont.workflow.core.entity.Card;

import javax.inject.Named;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author krivopustov
 * @version $Id$
 */
public class CardBrowser extends AbstractWindow {

    @Named("cardTable")
    protected Table table;

    public void init(Map<String, Object> params) {
        super.init(params);
        table.addAction(new RefreshAction(table));
        table.addAction(new CreateAction(table));
        table.addAction(new AbstractAction("open") {
            @Override
            public void actionPerform(Component component) {
                    Set<Card> selected = table.getSelected();
                    if (selected.size() == 1) {
                        Window window = openEditor("wf$Card.edit", selected.iterator().next(), WindowManager.OpenType.THIS_TAB);
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
        table.addAction(new RemoveAction(table));
        table.addAction(new ViewVariablesAction());
    }

    private class ViewVariablesAction extends AbstractAction {

        public ViewVariablesAction() {
            super("viewVariables");
        }

        public void actionPerform(Component component) {
            Set selected = table.getSelected();
            if (!selected.isEmpty()) {
                final Card card = (Card) selected.iterator().next();
                App.getInstance().getWindowManager().getDialogParams().setWidth(700);
                openWindow("wf$CardVariable.browse", WindowManager.OpenType.DIALOG, Collections.<String, Object> singletonMap("card", card));
            }
        }
    }
}