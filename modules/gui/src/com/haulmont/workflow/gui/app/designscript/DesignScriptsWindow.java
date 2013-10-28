/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.designscript;

import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignScript;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.*;

/**
 * @author krivopustov
 * @version $Id$
 */
public class DesignScriptsWindow extends AbstractWindow {

    private Design design;
    private CollectionDatasource<DesignScript, UUID> ds;
    private Table table;

    @Inject
    protected Component actionsPane;

    @Inject
    protected TextField nameField;

    @Inject
    protected TextArea contentField;

    @Override
    public void init(Map<String, Object> params) {
        design = (Design) params.get("design");
        if (design == null)
            throw new IllegalArgumentException("Design instance must be passed in params");

        ds = getDsContext().get("scriptsDs");

        table = getComponent("table");
        table.addAction(new RemoveAction(table));
        table.addAction(new NewAction());
        table.addAction(new ModifyAction());

        addAction(new SaveAction());
        addAction(new CancelAction());
    }

    private void enableControls() {
        actionsPane.setVisible(true);
        nameField.setEditable(true);
        contentField.setEditable(true);
        table.setEnabled(false);
        table.getButtonsPanel().setEnabled(true);
    }

    private void disableControls() {
        actionsPane.setVisible(false);
        nameField.setEditable(false);
        contentField.setEditable(false);
        table.setEnabled(true);
    }

    private class NewAction extends AbstractAction {

        protected NewAction() {
            super("new");
        }

        @Override
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
            enableControls();
        }
    }

    private class ModifyAction extends AbstractAction {

        protected ModifyAction() {
            super("modify");
        }

        @Override
        public void actionPerform(Component component) {
            if (!table.getSelected().isEmpty()) {
                enableControls();
            }
        }
    }

    private class SaveAction extends AbstractAction {

        protected SaveAction() {
            super("save");
        }

        @Override
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
            disableControls();
        }
    }

    private class CancelAction extends AbstractAction {

        protected CancelAction() {
            super("cancel");
        }

        @Override
        public void actionPerform(Component component) {
            ds.refresh();
            disableControls();
        }
    }
}