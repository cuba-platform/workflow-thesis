/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 27.01.11 14:58
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.designscript;

import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignScript;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class DesignScriptsWindow extends AbstractWindow {

    private Design design;
    private CollectionDatasource<DesignScript, UUID> ds;
    private Table table;

    public DesignScriptsWindow(IFrame frame) {
        super(frame);
    }

    @Override
    protected void init(Map<String, Object> params) {
        design = (Design) params.get("design");
        if (design == null)
            throw new IllegalArgumentException("Design instance must be passed in params");

        ds = getDsContext().get("scriptsDs");

        table = getComponent("table");
        table.addAction(new RemoveAction(table,false));
        table.addAction(new NewAction());

        addAction(new SaveAction());
        addAction(new CancelAction());
    }

    private class NewAction extends AbstractAction {

        protected NewAction() {
            super("new");
        }

        public void actionPerform(Component component) {
            Collection<UUID> designIds = ds.getItemIds();
            for (UUID id : designIds) {
                DesignScript designScript = ds.getItem(id);
                if (StringUtils.trimToNull(designScript.getName()) == null) {
                    return;
                }
            }

            DesignScript designScript = new DesignScript();
            designScript.setDesign(design);

            ds.addItem(designScript);
            table.setSelected(designScript);
        }
    }

    private class SaveAction extends AbstractAction {

        protected SaveAction() {
            super("save");
        }

        public void actionPerform(Component component) {
            Collection<UUID> designIds = ds.getItemIds();
            Set<String> designScriptNames = new HashSet<String>();
            for (UUID id : designIds) {
                DesignScript designScript = ds.getItem(id);
                if (StringUtils.trimToNull(designScript.getName()) == null) {
                    showNotification(getMessage("emptyScriptName"), NotificationType.TRAY);
                    return;
                }
                if (designScriptNames.contains(designScript.getName())) {
                    showNotification(getMessage("duplicateScriptName") + " " +
                            designScript.getName(), NotificationType.TRAY);
                    return;
                } else {
                    designScriptNames.add(designScript.getName());
                }
            }
            ds.commit();
        }
    }

    private class CancelAction extends AbstractAction {

        protected CancelAction() {
            super("cancel");
        }

        public void actionPerform(Component component) {
            ds.refresh();
        }
    }
}
