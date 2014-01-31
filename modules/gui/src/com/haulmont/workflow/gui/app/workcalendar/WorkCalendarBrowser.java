/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.workcalendar;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.workflow.core.entity.WorkCalendarEntity;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class WorkCalendarBrowser extends AbstractWindow {

    @Inject
    protected Table workDaysTable;

    @Inject
    protected Table exceptionDaysTable;

    @Inject
    protected CollectionDatasource<WorkCalendarEntity, UUID> workDaysDs;

    @Inject
    protected CollectionDatasource<WorkCalendarEntity, UUID> exceptionDaysDs;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        workDaysTable.addAction(new AbstractAction("create") {
            @Override
            public void actionPerform(Component component) {
                Window window = openEditor("wf$WorkCalendarWorkDay.edit", new WorkCalendarEntity(), WindowManager.OpenType.DIALOG);
                window.addListener(new CloseListener() {
                    @Override
                    public void windowClosed(String actionId) {
                        workDaysDs.refresh();
                    }
                });
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage("actions.Create");
            }
        });

        workDaysTable.addAction(new ItemTrackingAction("edit") {
            @Override
            public void actionPerform(Component component) {
                Entity entity = workDaysDs.getItem();
                if (entity != null) {
                    Window window = openEditor("wf$WorkCalendarWorkDay.edit", entity, WindowManager.OpenType.DIALOG);
                    window.addListener(new CloseListener() {
                        @Override
                        public void windowClosed(String actionId) {
                            workDaysDs.refresh();
                        }
                    });
                }
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage("actions.Edit");
            }
        });
        workDaysTable.addAction(new RemoveAction(workDaysTable));

        exceptionDaysTable.addAction(new AbstractAction("create") {
            @Override
            public void actionPerform(Component component) {
                Window window = openEditor("wf$WorkCalendarExceptionDay.edit", new WorkCalendarEntity(), WindowManager.OpenType.DIALOG);
                window.addListener(new CloseListener() {
                    @Override
                    public void windowClosed(String actionId) {
                        exceptionDaysDs.refresh();

                        exceptionDaysTable.requestFocus();
                    }
                });
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage("actions.Create");
            }
        });

        exceptionDaysTable.addAction(new ItemTrackingAction("edit") {
            @Override
            public void actionPerform(Component component) {
                Entity entity = exceptionDaysDs.getItem();
                if (entity != null) {
                    Window window = openEditor("wf$WorkCalendarExceptionDay.edit", entity, WindowManager.OpenType.DIALOG);
                    window.addListener(new CloseListener() {
                        @Override
                        public void windowClosed(String actionId) {
                            exceptionDaysDs.refresh();

                            exceptionDaysTable.requestFocus();
                        }
                    });
                }
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage("actions.Edit");
            }
        });
        exceptionDaysTable.addAction(new RemoveAction(exceptionDaysTable));
    }
}