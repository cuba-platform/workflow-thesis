/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 22.12.10 10:29
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.design;

import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.ConfigProvider;
import com.haulmont.cuba.core.global.UserSessionProvider;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataService;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.cuba.web.filestorage.WebExportDisplay;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.workflow.core.app.CompilationMessage;
import com.haulmont.workflow.core.app.DesignerService;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import com.haulmont.workflow.core.exception.TemplateGenerationException;
import com.haulmont.workflow.core.global.WfConfig;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import org.apache.commons.lang.BooleanUtils;

import java.util.*;

public class DesignBrowser extends AbstractWindow {

    private CollectionDatasource<Design, UUID> ds;
    private Table table;
    private DesignerService service;

    public DesignBrowser(IFrame frame) {
        super(frame);
    }

    @Override
    protected void init(Map<String, Object> params) {
        ds = getDsContext().get("designDs");
        table = getComponent("designTable");
        service = ServiceLocator.lookup(DesignerService.NAME);

        initActions();
        initColumns();
    }

    private void initActions() {
        final TableActionsHelper helper = new TableActionsHelper(this, table);
        helper.createRefreshAction();
        helper.createCreateAction(WindowManager.OpenType.DIALOG);

        table.addAction(new CopyAction());
        table.addAction(new ImportAction());
        table.addAction(new ExportAction());

        helper.createRemoveAction();

        Action designAction = new AbstractAction("design") {
            public void actionPerform(Component component) {
                Set selected = table.getSelected();
                if (!selected.isEmpty()) {
                    String id = ((Design) selected.iterator().next()).getId().toString();
                    openDesigner(id);
                }
            }
        };
        table.addAction(designAction);
        table.setItemClickAction(designAction);

        table.addAction(new ScriptsAction());
        table.addAction(new LocalizeAction());
        table.addAction(new CompileAction());
        table.addAction(new DeployAction());

        helper.addListener(
                new ListActionsHelper.Listener() {
                    public void entityCreated(Entity entity) {
                        openDesigner(entity.getId().toString());
                    }

                    public void entityEdited(Entity entity) {
                    }

                    public void entityRemoved(Set<Entity> entity) {
                    }
                }
        );

        PopupButton notificationMatrixBtn = getComponent("notificationMatrix");
        notificationMatrixBtn.addAction(new UploadNotificationMatrixAction());
        notificationMatrixBtn.addAction(new ClearNotificationMatrixAction());
        notificationMatrixBtn.addAction(new DownloadNotificationMatrix());
    }

    private void initColumns() {
        com.vaadin.ui.Table vTable = (com.vaadin.ui.Table) WebComponentsHelper.unwrap(table);
        MetaPropertyPath nmUploadedCol = table.getDatasource().getMetaClass().getPropertyPath("notificationMatrixUploaded");
        vTable.removeGeneratedColumn(nmUploadedCol);
        vTable.addGeneratedColumn(
                nmUploadedCol,
                new com.vaadin.ui.Table.ColumnGenerator() {
                    public com.vaadin.ui.Component generateCell(com.vaadin.ui.Table source, Object itemId, Object columnId) {
                        final Design design = ds.getItem((UUID) itemId);
                        if (BooleanUtils.isTrue(design.getNotificationMatrixUploaded())) {
                            Button button = new Button(getMessage("showNotificationMatrix"));
                            button.setStyleName("link");
                            button.setImmediate(true);
                            button.addListener(
                                    new Button.ClickListener() {
                                        public void buttonClick(Button.ClickEvent event) {
                                            Design d = getDsContext().getDataService().reload(design, "_local");
                                            WebExportDisplay export = new WebExportDisplay();
                                            export.show(
                                                    new ByteArrayDataProvider(d.getNotificationMatrix()),
                                                    "NotificationMatrix",
                                                    ExportFormat.XLS
                                            );
                                        }
                                    }
                            );
                            return button;
                        } else {
                            return new Label("");
                        }
                    }
                }
        );
    }

    private void openDesigner(String id) {
        String designerUrl = ConfigProvider.getConfig(WfConfig.class).getDesignerUrl();
        StringBuilder url = new StringBuilder();
        url.append(ControllerUtils.getControllerURL(designerUrl))
                .append("?id=")
                .append(id)
                .append("&s=")
                .append(UserSessionProvider.getUserSession().getId());
        String target = String.valueOf(Math.round(Math.random() * 100));
        App.getInstance().getAppWindow().open(new ExternalResource(url.toString()), target);
    }

    private class CopyAction extends AbstractAction {
        protected CopyAction() {
            super("copy");
        }

        public void actionPerform(Component component) {
            Set selected = table.getSelected();
            if (!selected.isEmpty()) {
                final Design design = (Design) selected.iterator().next();
                UUID newId = service.copyDesign(design.getId());
                openDesigner(newId.toString());
            }
        }
    }

    private class ExportAction extends AbstractAction {
        protected ExportAction() {
            super("export");
        }

        public void actionPerform(Component component) {
            Set selected = table.getSelected();
            if (!selected.isEmpty()) {
                Design design = (Design) selected.iterator().next();
                design = getDsContext().getDataService().reload(design, "export");
                try {
                    new WebExportDisplay().show(new ByteArrayDataProvider(service.exportDesign(design)), "Design", ExportFormat.ZIP);
                } catch (Exception e) {

                    showNotification(
                            getMessage("notification.exportFailed"),
                            e.getMessage(),
                            NotificationType.ERROR
                    );
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private class ImportAction extends AbstractAction {
        protected ImportAction() {
            super("import");
        }

        public void actionPerform(Component component) {
            final ImportDialog importDialog = openWindow("wf$Design.import", WindowManager.OpenType.DIALOG);
            importDialog.addListener(new CloseListener() {
                public void windowClosed(String actionId) {
                    if (Window.COMMIT_ACTION_ID.equals(actionId)) {

                        try {
                            service.importDesign(importDialog.getBytes());
                        } catch (Exception ex) {

                            showNotification(
                                    getMessage("notification.importFailed"),
                                    ex.getMessage(),
                                    NotificationType.ERROR
                            );
                            throw new RuntimeException(ex);
                        }
                        table.getDatasource().refresh();
                    }
                }
            });
        }
    }

    private class CompileAction extends AbstractAction {
        public CompileAction() {
            super("compile");
        }

        public void actionPerform(Component component) {
            Set selected = table.getSelected();
            if (!selected.isEmpty()) {
                final Design design = (Design) selected.iterator().next();
                if (design.getCompileTs() != null) {
                    showOptionDialog(
                            getMessage("confirmCompile.title"),
                            String.format(getMessage("confirmCompile.msg"), design.getName()),
                            MessageType.CONFIRMATION,
                            new Action[]{
                                    new DialogAction(DialogAction.Type.YES) {
                                        @Override
                                        public void actionPerform(Component component) {
                                            compile(design);
                                        }
                                    },
                                    new DialogAction(DialogAction.Type.NO)
                            }
                    );

                } else {
                    compile(design);
                }
            }
        }

        private void compile(Design design) {
            DesignerService service = ServiceLocator.lookup(DesignerService.NAME);
            try {
                CompilationMessage message = service.compileDesign(design.getId());
                ds.refresh();
                if (message.getErrors().size() == 0 && message.getWarnings().size() == 0)
                    showNotification(getMessage("notification.compileSuccess"), NotificationType.HUMANIZED);
                else if (message.getErrors().size() == 0 && message.getWarnings().size() > 0)
                    showOptionDialog(getMessage("notification.compileWithWarnings"), prepareCompilationMessage(message), IFrame.MessageType.CONFIRMATION,
                            new Action[]{
                                    new DialogAction(DialogAction.Type.OK)
                            });
                else {
                    showOptionDialog(getMessage("notification.compileFailed"), prepareCompilationMessage(message), IFrame.MessageType.CONFIRMATION,
                            new Action[]{
                                    new DialogAction(DialogAction.Type.OK)
                            });
                }

            } catch (DesignCompilationException e) {
                showNotification(
                        getMessage("notification.compileFailed"),
                        e.getMessage(),
                        NotificationType.ERROR
                );
            }
        }

        private String prepareCompilationMessage(CompilationMessage message) {
            StringBuilder result = new StringBuilder();
            if (message.getErrors().size() > 0) {
                result.append("<b>" + getMessage("notification.errors") + "</b><br />");
            }
            for (String error : message.getErrors()) {
                result.append(error);
                result.append("<br />");
            }
            if (message.getWarnings().size() > 0) {
                result.append("<b>" + getMessage("notification.warnings") + "</b><br />");
            }
            for (String warning : message.getWarnings()) {
                result.append(warning);
                result.append("<br />");
            }
            return result.toString();
        }
    }

    private class DeployAction extends AbstractAction {
        public DeployAction() {
            super("deploy");
        }

        public void actionPerform(Component component) {
            Set selected = table.getSelected();
            if (!selected.isEmpty()) {
                final Design design = (Design) selected.iterator().next();
                if (design.getCompileTs() == null) {
                    showNotification(getMessage("notification.notCompiled"), NotificationType.WARNING);
                } else {
                    App.getInstance().getWindowManager().getDialogParams().setWidth(500);
                    final DeployDesignWindow window = openWindow("wf$Design.deploy", WindowManager.OpenType.DIALOG, Collections.<String, Object>singletonMap("design", design));
                    window.addListener(
                            new CloseListener() {
                                public void windowClosed(String actionId) {
                                    if ("ok".equals(actionId)) {
                                        ds.refresh();
                                        showNotification(getMessage("notification.deploySuccess"), NotificationType.HUMANIZED);
                                    } else {
                                        showNotification(
                                                getMessage("notification.deployFailed"),
                                                window.getErrorMsg(),
                                                NotificationType.HUMANIZED
                                        );
                                    }
                                }
                            }
                    );
                }
            }
        }
    }

    private class ScriptsAction extends AbstractAction {
        protected ScriptsAction() {
            super("scripts");
        }

        public void actionPerform(Component component) {
            Set selected = table.getSelected();
            if (!selected.isEmpty()) {
                final Design design = (Design) selected.iterator().next();
                Window window = openWindow(
                        "wf$DesignScript.browse",
                        WindowManager.OpenType.THIS_TAB,
                        Collections.<String, Object>singletonMap("design", design)
                );
//                window.addListener(
//                        new CloseListener() {
//                            public void windowClosed(String actionId) {
//                                if (Window.COMMIT_ACTION_ID.equals(actionId))
//                                    ds.refresh();
//                            }
//                        }
//                );
            }
        }
    }

    private class LocalizeAction extends AbstractAction {
        protected LocalizeAction() {
            super("localize");
        }

        public void actionPerform(Component component) {
            Set selected = table.getSelected();
            if (!selected.isEmpty()) {
                final Design design = (Design) selected.iterator().next();
                if (design.getCompileTs() == null) {
                    showNotification(getMessage("notification.notCompiled"), NotificationType.WARNING);
                } else {
                    Window window = openEditor("wf$Design.localize", design, WindowManager.OpenType.THIS_TAB);
                    window.addListener(
                            new CloseListener() {
                                public void windowClosed(String actionId) {
                                    if (Window.COMMIT_ACTION_ID.equals(actionId))
                                        ds.refresh();
                                }
                            }
                    );
                }
            }
        }
    }

    private class UploadNotificationMatrixAction extends AbstractAction {
        protected UploadNotificationMatrixAction() {
            super("uploadNotificationMatrix");
        }

        public void actionPerform(Component component) {
            Set selected = table.getSelected();
            if (!selected.isEmpty()) {
                Design selectedDesign = (Design) selected.iterator().next();
                final Design design = ds.getDataService().reload(selectedDesign,"_local");
                final NotificationMatrixWindow window = openWindow("wf$Design.notificationMatrix", WindowManager.OpenType.DIALOG);
                window.addListener(
                        new CloseListener() {
                            public void windowClosed(String actionId) {
                                if (Window.COMMIT_ACTION_ID.equals(actionId)){
                                    design.setNotificationMatrix(window.getBytes());
                                    design.setNotificationMatrixUploaded(true);
                                    ds.getDataService().commit(new CommitContext(Collections.singleton(design)));
                                    DesignerService service = ServiceLocator.lookup(DesignerService.NAME);
                                    service.saveNotificationMatrixFile(design);
                                    ds.refresh();
                                }
                            }
                        }
                );
            }
        }
    }

    private class ClearNotificationMatrixAction extends AbstractAction {
        protected ClearNotificationMatrixAction() {
            super("clearNotificationMatrix");
        }

        public void actionPerform(Component component) {
            Set selected = table.getSelected();
            if (!selected.isEmpty()) {
                final DataService dataService = getDsContext().getDataService();
                final Design design = dataService.reload((Design) selected.iterator().next(), "_local");
                showOptionDialog(
                        getMessage("confirmNMClear.title"),
                        String.format(getMessage("confirmNMClear.msg"), design.getName()),
                        MessageType.CONFIRMATION,
                        new Action[]{
                                new DialogAction(DialogAction.Type.YES) {
                                    @Override
                                    public void actionPerform(Component component) {
                                        design.setNotificationMatrix(null);
                                        design.setNotificationMatrixUploaded(false);
                                        dataService.commit(new CommitContext(Collections.singleton(design)));
                                        ds.refresh();
                                    }
                                },
                                new DialogAction(DialogAction.Type.NO)
                        }
                );
            }
        }
    }

    private class DownloadNotificationMatrix extends AbstractAction {
        protected DownloadNotificationMatrix() {
            super("downloadNotificationMatrix");
        }

        public void actionPerform(Component component) {
            Set selected = table.getSelected();
            try {
                if (!selected.isEmpty()) {
                    final Design design = (Design) selected.iterator().next();
                    if (design.getCompileTs() != null) {
                        WebExportDisplay export = new WebExportDisplay();

                        byte[] bytes = service.getNotificationMatrixTemplate(design.getUuid());
                        ByteArrayDataProvider array = new ByteArrayDataProvider(bytes);
                        export.show(array, "NotificationMatrix", ExportFormat.XLS);
                    } else {
                        showNotification(getMessage("notification.CompileDesignBefore"), NotificationType.HUMANIZED);
                    }
                }
            } catch (TemplateGenerationException e) {
                showNotification(
                        getMessage("notification.createTemplateFailed"),
                        e.getMessage(),
                        NotificationType.ERROR
                );
            }

        }
    }
}