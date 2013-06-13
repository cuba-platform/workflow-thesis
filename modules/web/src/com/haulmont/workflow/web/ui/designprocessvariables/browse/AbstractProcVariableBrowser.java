/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.designprocessvariables.browse;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RefreshAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.web.gui.components.WebLabel;
import com.haulmont.workflow.core.app.ProcessVariableService;
import com.haulmont.workflow.core.entity.AbstractProcessVariable;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * <p>$Id: AbstractProcVariableBrowser.java 11002 2013-03-29 11:31:42Z zaharchenko $</p>
 *
 * @author Zaharchenko
 */
public abstract class AbstractProcVariableBrowser extends AbstractLookup {

    @Inject
    protected ProcessVariableService processVariableService;

    @Inject
    protected Table table;

    public AbstractProcVariableBrowser() {
        super();
    }

    @Override
    public void init(Map<String, Object> params) {
        table.addAction(new EditAction(table, WindowManager.OpenType.DIALOG));
        table.addAction(new RefreshAction(table));
        table.addAction(new RemoveAction(table));
        table.addAction(new CreateAction(table, WindowManager.OpenType.DIALOG) {
            @Override
            public Map<String, Object> getInitialValues() {
                return getInitialValuesForCreate();
            }
        });

        generateValueColumnForVariablesTable();
    }

    protected Map<String, Object> getInitialValuesForCreate() {
        return Collections.emptyMap();
    }

    protected void generateValueColumnForVariablesTable() {
        table.addGeneratedColumn("value", new Table.ColumnGenerator() {
            @Override
            public Component generateCell(Entity entity) {
                final AbstractProcessVariable designProcessVariable = (AbstractProcessVariable) entity;
                Component componentValue = new WebLabel();
                String localizedValue = processVariableService.getLocalizedValue(designProcessVariable);
                ((Label) componentValue).setValue(localizedValue);
                return componentValue;
            }
        });

    }


    @Override
    public boolean close(final String actionId, final boolean force) {
        StringBuilder notInitiatedVariables = new StringBuilder();
        CollectionDatasource<AbstractProcessVariable, UUID> collectionDatasource = table.getDatasource();
        for (UUID id : collectionDatasource.getItemIds()) {
            AbstractProcessVariable processVariable = collectionDatasource.getItem(id);
            if (processVariable.getAttributeType() == null) {
                if (notInitiatedVariables.length() > 0) notInitiatedVariables.append(", ");
                notInitiatedVariables.append(processVariable.getName());
            }
        }
        if (notInitiatedVariables.length() > 0) {
            showOptionDialog(getMessage("warning"), String.format(getMessage("attributeTypeNotsetForVariable"),
                    notInitiatedVariables.toString()),
                    MessageType.WARNING, Arrays.<Action>asList(new DialogAction(DialogAction.Type.OK) {
                @Override
                public void actionPerform(Component component) {
                    AbstractProcVariableBrowser.super.close(actionId, force);
                }
            }));
            return false;
        } else {
            return super.close(actionId);
        }
    }

    @Override
    public boolean close(final String actionId) {
        return close(actionId, false);
    }
}
