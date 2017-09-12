/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.gui.app.proc;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RefreshAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.workflow.core.entity.Proc;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
                getDialogOptions()
                        .setWidth("900px")
                        .setHeight("600px");

                Window variablesEditor = openWindow("wf$ProcVariable.browse",
                        WindowManager.OpenType.DIALOG, ParamsMap.of("proc", target.getSingleSelected()));
                variablesEditor.addCloseListener(actionId -> target.requestFocus());
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