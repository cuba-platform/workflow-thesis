/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.app.proc;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RefreshAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.gui.components.AbstractEntityAction;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author devyatkin
 * @version $Id$
 */
public class ProcBrowser extends AbstractLookup {

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        Table table = (Table) getComponentNN("procTable");

        table.addAction(new RefreshAction(table));
        table.addAction(new EditAction(table));
        table.addAction(new RemoveProc(table, true));
        table.addAction(new BaseAction("editProcessVariables") {

            @Override
            public void actionPerform(Component component) {
                getDialogParams().setWidth(900);
                getDialogParams().setHeight(600);

                Window variablesEditor = openWindow("wf$ProcVariable.browse",
                        WindowManager.OpenType.DIALOG, Collections.<String, Object>singletonMap("proc", target.getSingleSelected()));
                variablesEditor.addListener(new CloseListener() {
                    @Override
                    public void windowClosed(String actionId) {
                        target.requestFocus();
                    }
                });
            }

            @Override
            public boolean isApplicable() {
                return target != null && target.getSelected().size() == 1;
            }
        });
    }

    protected class RemoveProc extends RemoveAction {
        public RemoveProc(ListComponent owner, boolean autocommit) {
            super(owner, autocommit);
        }

        @Override
        public void actionPerform(Component component) {
            if (!isEnabled()) return;
            Set<Proc> selected = target.getSelected();
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

        @Override
        protected void afterRemove(Set selected) {
            super.afterRemove(selected);

            target.requestFocus();
        }
    }
}