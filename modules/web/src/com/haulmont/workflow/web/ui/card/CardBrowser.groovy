/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 25.11.2009 10:53:31
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.card

import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.WindowManager
import com.haulmont.cuba.gui.components.actions.CreateAction
import com.haulmont.cuba.gui.components.actions.RefreshAction
import com.haulmont.cuba.gui.components.actions.RemoveAction
import com.haulmont.cuba.web.App
import com.haulmont.workflow.core.entity.Card

public class CardBrowser extends AbstractWindow {

    def CardBrowser() {
        super();
    }

    public void init(Map<String, Object> params) {
        super.init(params);
        Table table = getComponent("cardTable")
        table.addAction(new RefreshAction(table))
        table.addAction(new CreateAction(table))
        table.addAction(new ActionAdapter('open', [
                actionPerform: {
                    Set selected = table.getSelected()
                    if (selected.size() == 1) {
                        Window window = openEditor('wf$Card.edit', selected.iterator().next(), WindowManager.OpenType.THIS_TAB)
                        window.addListener({ String actionId ->
                            if (actionId == Window.COMMIT_ACTION_ID) {
                                table.getDatasource().refresh()
                            }
                        } as Window.CloseListener)
                    }
                },
                getCaption: {
                    return getMessage('open')
                }
        ]))
        table.addAction(new RemoveAction(table))
        table.addAction(new ViewVariablesAction());
    }

    private class ViewVariablesAction extends AbstractAction {
        private static final long serialVersionUID = -1170007555308549776L;

        public ViewVariablesAction() {
            super("viewVariables");
        }

        public void actionPerform(Component component) {
            Table table = getComponent("cardTable")
            Set selected = table.getSelected();
            if (!selected.isEmpty()) {
                final Card card = (Card) selected.iterator().next();
                App.getInstance().getWindowManager().getDialogParams().setWidth(700);
                openWindow('wf$CardVariable.browse', WindowManager.OpenType.DIALOG, Collections.<String, Object> singletonMap("card", card));
            }
        }
    }
}