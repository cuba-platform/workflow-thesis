/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.proc;

import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RefreshAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.workflow.core.entity.Proc;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>$Id$</p>
 *
 * @author devyatkin
 */
public class ProcBrowser extends AbstractWindow {
    public ProcBrowser(IFrame frame) {
        super(frame);
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        Table table = getComponent("procTable");
        table.addAction(new RefreshAction(table));
        table.addAction(new EditAction(table));
        table.addAction(new RemoveProc(table, true));
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
