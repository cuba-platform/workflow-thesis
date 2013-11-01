package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.workflow.gui.app.base.CardRolesFrame;
import com.vaadin.ui.AbstractSelect;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class CardRolesFrameCompanion implements CardRolesFrame.Companion {
    @Override
    public void setTableColumnHeader(Table table, Object columnId, String header) {
        com.vaadin.ui.Table vTable = (com.vaadin.ui.Table) WebComponentsHelper.unwrap(table);
        vTable.setColumnHeader(columnId, header);
    }

    @Override
    public void setTableVisibleColumns(Table table, Object[] visibleColumns) {
        com.vaadin.ui.Table vTable = (com.vaadin.ui.Table) WebComponentsHelper.unwrap(table);
        vTable.setVisibleColumns(visibleColumns);
    }

    @Override
    public Object[] getVisibleColumns(Table table) {
        com.vaadin.ui.Table vTable = (com.vaadin.ui.Table) WebComponentsHelper.unwrap(table);
        return vTable.getVisibleColumns();
    }

    @Override
    public void setLookupNullSelectionAllowed(LookupField lookupField, boolean value) {
        AbstractSelect select = (AbstractSelect) WebComponentsHelper.unwrap(lookupField);
        select.setNullSelectionAllowed(value);
    }
}
