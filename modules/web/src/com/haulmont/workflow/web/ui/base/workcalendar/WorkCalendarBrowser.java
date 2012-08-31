/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 24.02.2010 13:22:00
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base.workcalendar;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.workflow.core.entity.WorkCalendarEntity;

import java.util.Map;

public class WorkCalendarBrowser extends AbstractWindow {
    public WorkCalendarBrowser(IFrame frame) {
        super(frame);
    }

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
