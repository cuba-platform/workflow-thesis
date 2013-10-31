/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.workcalendar;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.workflow.core.entity.WorkCalendarEntity;

import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class WorkCalendarBrowser extends AbstractWindow {

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        Table workDaysTable = getComponent("workDaysTable");
        TableActionsHelper workDaysHelper = new TableActionsHelper(this, workDaysTable);
        workDaysTable.addAction(new AbstractAction("create") {
            public void actionPerform(Component component) {
                Window window = openEditor("wf$WorkCalendarWorkDay.edit", new WorkCalendarEntity(), WindowManager.OpenType.DIALOG);
                window.addListener(new Window.CloseListener() {
                    public void windowClosed(String actionId) {
                        getDsContext().get("workDaysDs").refresh();
                    }
                });
            }

            @Override
            public String getCaption() {
                return AppBeans.get(Messages.class).getMainMessage("actions.Create");
            }
        });

        workDaysTable.addAction(new AbstractAction("edit") {
            public void actionPerform(Component component) {
                Entity entity = getDsContext().get("workDaysDs").getItem();
                if (entity != null) {
                    Window window = openEditor("wf$WorkCalendarWorkDay.edit", entity, WindowManager.OpenType.DIALOG);
                    window.addListener(new Window.CloseListener() {
                        public void windowClosed(String actionId) {
                            getDsContext().get("workDaysDs").refresh();
                        }
                    });
                }

            }
            @Override
            public String getCaption() {
                return AppBeans.get(Messages.class).getMainMessage("actions.Edit");
            }
        });
        workDaysHelper.createRemoveAction();

        Table exceptionDaysTable = getComponent("exceptionDaysTable");
        TableActionsHelper exceptionDaysHelper = new TableActionsHelper(this, exceptionDaysTable);
        exceptionDaysTable.addAction(new AbstractAction("create") {
            public void actionPerform(Component component) {
                Window window = openEditor("wf$WorkCalendarExceptionDay.edit", new WorkCalendarEntity(), WindowManager.OpenType.DIALOG);
                window.addListener(new Window.CloseListener() {
                    public void windowClosed(String actionId) {
                        getDsContext().get("exceptionDaysDs").refresh();
                    }
                });
            }

            @Override
            public String getCaption() {
                return AppBeans.get(Messages.class).getMainMessage("actions.Create");
            }
        });

        exceptionDaysTable.addAction(new AbstractAction("edit") {
            public void actionPerform(Component component) {
                Entity entity = getDsContext().get("exceptionDaysDs").getItem();
                if (entity != null) {
                    Window window = openEditor("wf$WorkCalendarExceptionDay.edit", entity, WindowManager.OpenType.DIALOG);
                    window.addListener(new Window.CloseListener() {
                        public void windowClosed(String actionId) {
                            getDsContext().get("exceptionDaysDs").refresh();
                        }
                    });
                }
            }
            @Override
            public String getCaption() {
                return AppBeans.get(Messages.class).getMainMessage("actions.Edit");
            }
        });
        exceptionDaysHelper.createRemoveAction();

    }
}