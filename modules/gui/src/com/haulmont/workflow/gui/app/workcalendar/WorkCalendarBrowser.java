/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.workcalendar;

import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.WindowManager.OpenType;
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

    private static final String CREATE_ACTION_ID = "create";
    private static final String CREATE_ACTION_CAPTION = "actions.Create";
    private static final String EDIT_ACTION_ID = "edit";
    private static final String EDIT_ACTION_CAPTION = "actions.Edit";

    @Inject
    protected Metadata metadata;

    @Inject
    protected Table workDaysTable;

    @Inject
    protected Table exceptionDaysTable;

    @Inject
    protected CollectionDatasource<WorkCalendarEntity, UUID> workDaysDs;

    @Inject
    protected CollectionDatasource<WorkCalendarEntity, UUID> exceptionDaysDs;

    protected ClientConfig clientConfig;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        clientConfig = AppBeans.get(Configuration.class).getConfig(ClientConfig.class);

        workDaysTable.addAction(createCreateWorkDayAction());
        workDaysTable.addAction(createEditWorkDayAction());
        workDaysTable.addAction(new RemoveAction(workDaysTable));

        exceptionDaysTable.addAction(createCreateExceptionDaysAction());
        exceptionDaysTable.addAction(createEditExceptionDaysAction());
        exceptionDaysTable.addAction(new RemoveAction(exceptionDaysTable));
    }

    protected AbstractAction createEditExceptionDaysAction() {
        AbstractAction editAction = new ItemTrackingAction(EDIT_ACTION_ID) {
            @Override
            public void actionPerform(Component component) {
                Entity entity = exceptionDaysDs.getItem();
                if (entity != null) {
                    Window window = openEditor("wf$WorkCalendarExceptionDay.edit", entity, OpenType.DIALOG);
                    window.addCloseListener(actionId -> {
                        exceptionDaysDs.refresh();

                        exceptionDaysTable.requestFocus();
                    });
                }
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage(EDIT_ACTION_CAPTION);
            }
        };

        editAction.setShortcut(clientConfig.getTableEditShortcut());

        return editAction;
    }

    protected AbstractAction createCreateExceptionDaysAction() {
        AbstractAction createAction = new AbstractAction(CREATE_ACTION_ID) {
            @Override
            public void actionPerform(Component component) {
                Window window = openEditor("wf$WorkCalendarExceptionDay.edit", metadata.create(WorkCalendarEntity.class), OpenType.DIALOG);
                window.addCloseListener(actionId -> {
                    exceptionDaysDs.refresh();

                    exceptionDaysTable.requestFocus();
                });
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage(CREATE_ACTION_CAPTION);
            }
        };

        createAction.setShortcut(clientConfig.getTableInsertShortcut());

        return createAction;
    }

    protected AbstractAction createCreateWorkDayAction() {
        AbstractAction createAction = new AbstractAction(CREATE_ACTION_ID) {
            @Override
            public void actionPerform(Component component) {
                Window window = openEditor("wf$WorkCalendarWorkDay.edit", metadata.create(WorkCalendarEntity.class), OpenType.DIALOG);
                window.addCloseListener(actionId -> {
                    workDaysDs.refresh();
                });
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage(CREATE_ACTION_CAPTION);
            }
        };

        createAction.setShortcut(clientConfig.getTableInsertShortcut());

        return createAction;
    }

    protected AbstractAction createEditWorkDayAction() {
        AbstractAction editAction = new ItemTrackingAction(EDIT_ACTION_ID) {
            @Override
            public void actionPerform(Component component) {
                Entity entity = workDaysDs.getItem();
                if (entity != null) {
                    Window window = openEditor("wf$WorkCalendarWorkDay.edit", entity, OpenType.DIALOG);
                    window.addCloseListener(actionId -> {
                        workDaysDs.refresh();
                    });
                }
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage(EDIT_ACTION_CAPTION);
            }
        };

        editAction.setShortcut(clientConfig.getTableEditShortcut());

        return editAction;
    }
}