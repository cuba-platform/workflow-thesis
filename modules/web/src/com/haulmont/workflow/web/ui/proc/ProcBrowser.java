/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.proc;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RefreshAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.web.App;
import com.haulmont.workflow.core.entity.Proc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author devyatkin
 * @version $Id$
 */
public class ProcBrowser extends AbstractWindow {

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        Table table = getComponent("procTable");
        table.addAction(new RefreshAction(table));
        table.addAction(new EditAction(table));
        table.addAction(new RemoveProc(table, true));
        table.addAction(new AbstractEntityAction<Proc>("editProcessVariables", table) {

            @Override
            protected Boolean isShowAfterActionNotification() {
                return false;
            }

            @Override
            protected Boolean isUpdateSelectedEntities() {
                return false;
            }

            @Override
            public void doActionPerform(Component component) {
                App.getInstance().getWindowManager().getDialogParams().setWidth(900);
                App.getInstance().getWindowManager().getDialogParams().setHeight(600);
                final Window window = openWindow("wf$ProcVariable.browse", WindowManager.OpenType.DIALOG, Collections.<String, Object>singletonMap("proc", getEntity()));
            }
        });
    }

    protected class RemoveProc extends RemoveAction {

        public RemoveProc(ListComponent owner, boolean autocommit) {
            super(owner, autocommit);
        }

        public void actionPerform(Component component) {
            if (!isEnabled()) return;
            Set<Proc> selected = owner.getSelected();
            final Set toRemove = new HashSet();
            for (Proc item : selected) {
                if (item.getDesign() != null) {
                    toRemove.add(item);
                }
            }
            if (!toRemove.isEmpty()) {
                confirmAndRemove(toRemove);
            }
        }
    }
}