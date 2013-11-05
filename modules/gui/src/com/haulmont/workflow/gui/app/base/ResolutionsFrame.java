/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.base;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.app.LinkColumnHelper;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import org.apache.commons.lang.time.DateUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

/**
 * @author krivopustov
 * @version $Id$
 */
public class ResolutionsFrame extends AbstractFrame {
    private TreeTable table;

    public void init() {
        table = getComponent("resolutionsTable");

        table.setStyleProvider(new Table.StyleProvider<Assignment>() {
            @Nullable
            @Override
            public String getStyleName(Assignment assignment, @Nullable String property) {
                if ((assignment.getDueDate() != null) &&
                        (((assignment.getFinished() == null && equalsDateBeforeDate(assignment.getDueDate(), TimeProvider.currentTimestamp())))
                                || (assignment.getFinished() != null && equalsDateBeforeDate(assignment.getDueDate(), assignment.getFinished())))) {
                    return "overdue";
                }
                if (assignment.getFinished() == null)
                    return "taskremind";
                else
                    return "";
            }
        });

//          next code uses screens from Thesis (task.log.dialog). Probably logic must be implemented in Thesis

//        vTable.addGeneratedColumn(resolutionsDs.getMetaClass().getPropertyPath("locOutcomeResult"),
//                new com.vaadin.ui.Table.ColumnGenerator() {
//                    @Override
//                    public com.vaadin.ui.Component generateCell(com.vaadin.ui.Table table, Object itemId, Object columnId) {
//                        final Assignment assignment = resolutionsDs.getItem((UUID) itemId);
//                        String state = assignment.getLocOutcome();
//                        if (state != null && StringUtils.contains(state, ".Saved")) {
//                            final com.vaadin.ui.Button vButton = new com.vaadin.ui.Button(assignment.getLocOutcomeResult());
//                            vButton.setStyleName("link");
//                            vButton.addClickListener(new com.vaadin.ui.Button.ClickListener() {
//                                @Override
//                                public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
//                                    WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo("task.log.dialog");
//                                    App.getInstance().getWindowManager().openWindow(windowInfo, WindowManager.OpenType.DIALOG,
//                                            Collections.<String, Object>singletonMap("item", assignment));
//                                }
//                            });
//                            return vButton;
//                        } else
//                            return new com.vaadin.ui.Label(assignment.getLocOutcomeResult());
//                    }
//                });

        table.addAction(new AbstractAction("openResolution") {
            @Override
            public void actionPerform(Component component) {
                openResolution(table.getDatasource().getItem());
            }
        });

        LinkColumnHelper.initColumn(table, "createTs", new LinkColumnHelper.Handler() {
            @Override
            public void onClick(Entity entity) {
                openResolution(entity);
            }
        });
    }

    private void openResolution(Entity entity) {
        final Window window = openEditor("wf$Assignment.edit", entity, WindowManager.OpenType.DIALOG);
        window.addListener(new Window.CloseListener() {
            @SuppressWarnings("unchecked")
            public void windowClosed(String actionId) {
                if (Window.COMMIT_ACTION_ID.equals(actionId) && window instanceof Window.Editor) {
                    Entity item = ((Window.Editor) window).getItem();
                    if (item != null) {
                        table.getDatasource().updateItem(item);
                    }
                }
            }
        });
    }

    public void setCard(final Card card) {
        CollectionDatasource<Assignment, UUID> ds = getDsContext().get("resolutionsDs");
        ds.refresh(Collections.<String, Object>singletonMap("cardId", card != null ? card.getId() : null));
        table.expandAll();
    }

    public void refreshDs() {
        getDsContext().get("resolutionsDs").refresh();
    }

    private boolean equalsDateBeforeDate(Date fromDate, Date toDate) {
        return fromDate != null && toDate != null
                && DateUtils.truncate(fromDate, Calendar.MINUTE).before(DateUtils.truncate(toDate, Calendar.MINUTE));
    }
}