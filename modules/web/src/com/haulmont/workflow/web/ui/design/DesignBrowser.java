/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.web.ui.design;

import com.haulmont.bali.datastruct.Pair;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.cuba.web.filestorage.WebExportDisplay;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.cuba.web.gui.components.WebLabel;
import com.haulmont.workflow.core.app.CompilationMessage;
import com.haulmont.workflow.core.app.DesignerService;
import com.haulmont.workflow.core.app.ProcessVariableService;
import com.haulmont.workflow.core.entity.AbstractProcessVariable;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.error.DesignCompilationError;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import com.haulmont.workflow.core.exception.TemplateGenerationException;
import com.haulmont.workflow.core.global.WfConfig;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import org.apache.commons.lang.BooleanUtils;

import javax.inject.Inject;
import java.util.*;

/**
 * @author krivopustov
 * @version $Id$
 */
public class DesignBrowser extends AbstractWindow {

    protected CollectionDatasource<Design, UUID> ds;
    protected Table table;
    protected DesignerService service;

    @Inject
    protected Table variablesTable;

    @Inject
    protected ProcessVariableService processVariableService;

    @Override
    public void init(Map<String, Object> params) {
        ds = getDsContext().get("designDs");
        table = getComponent("designTable");
        service = AppBeans.get(DesignerService.NAME);

        initActions();
        initColumns();
    }

    protected void initActions() {
        final TableActionsHelper helper = new TableActionsHelper(this, table);
        helper.createCreateAction(WindowManager.OpenType.DIALOG);

        table.addAction(new CopyAction());
        table.addAction(new ImportAction());
        table.addAction(new ExportAction());
        table.addAction(new EditAction(table, WindowManager.OpenType.DIALOG));
        helper.createRemoveAction();

        Action designAction = new AbstractAction("design") {
            @Override
            public void actionPerform(Component component) {
                Set<Design> selected = table.getSelected();
                if (!selected.isEmpty()) {
                    String id = selected.iterator().next().getId().toString();
                    openDesigner(id);
                }
            }
        };
        table.addAction(designAction);
        table.setItemClickAction(designAction);

        table.addAction(new ScriptsAction());
        table.addAction(new LocalizeAction());
        table.addAction(new CompileAction(table));
        table.addAction(new DeployAction());

        table.addAction(new AbstractEntityAction<Design>("showAffectedDesigns", table) {

            @Override
            protected Boolean isShowAfterActionNotification() {
                return false;
            }

            @Override
            protected Boolean isUpdateSelectedEntities() {
                return false;
            }

            @Override
            public void doActionPerform(Component component) {
                Window window = openWindow("wf$Design.browse", WindowManager.OpenType.THIS_TAB, Collections.<String, Object>singletonMap("subprocId", "%" + getEntity().getId().toString() + "%"));
                window.addListener(new CloseListener() {
                    @Override
                    public void windowClosed(String actionId) {
                        table.getDatasource().refresh();
                    }
                });
            }
        });

        table.addAction(new AbstractEntityAction<Design>("editProcessVariables", table) {

            @Override
            protected Boolean isShowAfterActionNotification() {
                return false;
            }

            @Override
            protected Boolean isUpdateSelectedEntities() {
                return false;
            }

            @Override
            public void doActionPerform(Component component) {
                App.getInstance().getWindowManager().getDialogParams().setWidth(900);
                App.getInstance().getWindowManager().getDialogParams().setHeight(600);
                final Window window = openWindow("wf$DesignProcessVariable.browse", WindowManager.OpenType.DIALOG, Collections.<String, Object>singletonMap("design", getEntity()));
                window.addListener(
                        new CloseListener() {
                            public void windowClosed(String actionId) {
                                ds.refresh();
                            }
                        }
                );
            }
        });

        helper.addListener(
                new ListActionsHelper.Listener() {
                    @Override
                    public void entityCreated(Entity entity) {
                        openDesigner(entity.getId().toString());
                    }

                    @Override
                    public void entityEdited(Entity entity) {
                    }

                    @Override
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
                            button.addClickListener(
                                    new Button.ClickListener() {
                                        public void buttonClick(Button.ClickEvent event) {
                                            Design d = getDsContext().getDataSupplier().reload(design, "_local");
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

        variablesTable.addGeneratedColumn("value", new Table.ColumnGenerator() {
            @Override
            public Component generateCell(Entity entity) {
                final AbstractProcessVariable designProcessVariable = (AbstractProcessVariable) entity;
                Component componentValue = new WebLabel();
                String localizedValue = processVariableService.getLocalizedValue(designProcessVariable);
                ((com.haulmont.cuba.gui.components.Label) componentValue).setValue(localizedValue);
                return componentValue;
            }
        });
    }

    private void openDesigner(String id) {
        String designerUrl = AppBeans.get(Configuration.class).getConfig(WfConfig.class).getDesignerUrl();
        StringBuilder url = new StringBuilder();
        url.append(ControllerUtils.getWebControllerURL(designerUrl))
                .append("?id=")
                .append(id)
                .append("&s=")
                .append(AppBeans.get(UserSessionSource.class).getUserSession().getId());
        App.getInstance().getAppUI().getPage().open(url.toString(), "_blank");
    }

    private class CopyAction extends ItemTrackingAction {
        protected CopyAction() {
            super("copy");
        }

        @Override
        public void actionPerform(Component component) {
            Set<Design> selected = table.getSelected();
            if (!selected.isEmpty()) {
                final Design design = selected.iterator().next();
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
                try {
                    new WebExportDisplay().show(new ByteArrayDataProvider(service.exportDesigns(selected)), "Designs", ExportFormat.ZIP);
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
                            service.importDesigns(importDialog.getBytes());
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

    private class CompileAction extends AbstractEntityAction<Design> {

        private final static String ACTION_ID = "compile";

        public CompileAction(Design entity, IFrame frame) {
            super(ACTION_ID, entity, frame);
        }

        public CompileAction(Table table) {
            super(ACTION_ID, table);
        }

        public CompileAction(Datasource<Design> datasource, IFrame frame) {
            super(ACTION_ID, datasource, frame);
        }

        @Override
        protected Boolean isSupportMultiselect() {
            return true;
        }

        @Override
        protected Boolean isShowAfterActionNotification() {
            return false;
        }

        @Override
        protected Boolean isConfirmation() {
            return true;
        }

        @Override
        public void doActionPerform(Component component) {
            StringBuilder result = new StringBuilder();
            for (Design design : getEntities()) {
                Pair<String, String> compileResult = compile(design);
                result.append("<b>").append(design.getName()).append(":</b> ").append(compileResult.getFirst());
                if (compileResult.getSecond() != null) {
                    result.append("<br>").append(compileResult.getSecond());
                }
                result.append("<br>");
            }
            showMessageDialog(getMessage("compilationResult"), result.toString(), MessageType.CONFIRMATION);
        }

        private Pair<String, String> compile(Design design) {
            DesignerService service = AppBeans.get(DesignerService.NAME);
            try {
                CompilationMessage message = service.compileDesign(design.getId());
                ds.refresh();
                if (message.getErrors().size() == 0 && message.getWarnings().size() == 0) {
                    return new Pair<>(getMessage("notification.compileSuccess"), prepareCompilationMessage(message));
                } else if (message.getErrors().size() == 0 && message.getWarnings().size() > 0)
                    return new Pair<>(getMessage("notification.compileWithWarnings"), prepareCompilationMessage(message));
                else {
                    return new Pair<>(getMessage("notification.compileFailed"), prepareCompilationMessage(message));
                }

            } catch (DesignCompilationException e) {
                return new Pair<>(getMessage("notification.compileFailed"), e.getMessage());
            }
        }

        private String prepareCompilationMessage(CompilationMessage message) {
            StringBuilder result = new StringBuilder();
            if (message.getErrors().size() > 0) {
                result.append("<b>").append(getMessage("notification.errors")).append("</b><br />");
            }
            for (DesignCompilationError error : message.getErrors()) {
                result.append(error.getMessage());
                result.append("<br />");
            }
            if (message.getWarnings().size() > 0) {
                result.append("<b>").append(getMessage("notification.warnings")).append("</b><br />");
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
            Set<Design> selected = table.getSelected();
            if (!selected.isEmpty()) {
                final Design design = selected.iterator().next();
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

        @Override
        public void actionPerform(Component component) {
            Set<Design> selected = table.getSelected();
            if (!selected.isEmpty()) {
                final Design design = selected.iterator().next();
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

        @Override
        public void actionPerform(Component component) {
            Set<Design> selected = table.getSelected();
            if (!selected.isEmpty()) {
                final Design design = selected.iterator().next();
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

    private void showDesignProcessVariables(Design design) {
        App.getInstance().getWindowManager().getDialogParams().setWidth(900);
        App.getInstance().getWindowManager().getDialogParams().setHeight(600);
        final Window window = openWindow("wf$DesignProcessVariable.browse", WindowManager.OpenType.DIALOG, Collections.<String, Object>singletonMap("design", design));
        window.addListener(
                new CloseListener() {
                    public void windowClosed(String actionId) {
                        ds.refresh();
                    }
                }
        );
    }

    private class UploadNotificationMatrixAction extends AbstractAction {
        protected UploadNotificationMatrixAction() {
            super("uploadNotificationMatrix");
        }

        @Override
        public void actionPerform(Component component) {
            Set<Design> selected = table.getSelected();
            if (!selected.isEmpty()) {
                Design selectedDesign = selected.iterator().next();
                final Design design = ds.getDataSupplier().reload(selectedDesign, "_local");
                final NotificationMatrixWindow window = openWindow("wf$Design.notificationMatrix", WindowManager.OpenType.DIALOG);
                window.addListener(
                        new CloseListener() {
                            public void windowClosed(String actionId) {
                                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                                    design.setNotificationMatrix(window.getBytes());
                                    design.setNotificationMatrixUploaded(true);
                                    ds.getDataSupplier().commit(new CommitContext(Collections.singleton(design)));
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

        @Override
        public void actionPerform(Component component) {
            Set<Design> selected = table.getSelected();
            if (!selected.isEmpty()) {
                final DataSupplier dataSupplier = getDsContext().getDataSupplier();
                final Design design = dataSupplier.reload(selected.iterator().next(), "_local");
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
                                        dataSupplier.commit(new CommitContext(Collections.singleton(design)));
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

        @Override
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