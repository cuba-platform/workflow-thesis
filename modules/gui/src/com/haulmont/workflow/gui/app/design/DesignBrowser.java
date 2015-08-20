/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.design;

import com.haulmont.bali.datastruct.Pair;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.workflow.core.app.CompilationMessage;
import com.haulmont.workflow.core.app.DesignerService;
import com.haulmont.workflow.core.app.ProcessVariableService;
import com.haulmont.workflow.core.entity.AbstractProcessVariable;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.error.DesignCompilationError;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import com.haulmont.workflow.core.exception.TemplateGenerationException;
import com.haulmont.workflow.core.global.WfConfig;
import org.apache.commons.lang.BooleanUtils;

import javax.inject.Inject;
import java.util.*;

/**
 * @author krivopustov
 * @version $Id$
 */
public class DesignBrowser extends AbstractWindow {

    @Inject
    protected CollectionDatasource<Design, UUID> designDs;

    @Inject
    protected Table designTable;

    @Inject
    protected DesignerService service;

    @Inject
    protected Table variablesTable;

    @Inject
    protected ProcessVariableService processVariableService;

    @Inject
    protected ExportDisplay exportDisplay;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    protected Configuration configuration;

    @Inject
    protected DataSupplier dataSupplier;

    @Override
    public void init(Map<String, Object> params) {
        initActions();
        initColumns();
    }

    protected void initActions() {
        designTable.addAction(new CreateAction(designTable, WindowManager.OpenType.DIALOG) {
            @Override
            protected void afterCommit(Entity entity) {
                openDesigner(entity.getId().toString());
            }
        });

        designTable.addAction(new CopyAction());
        designTable.addAction(new ImportAction());
        designTable.addAction(new ExportAction());
        designTable.addAction(new EditAction(designTable, WindowManager.OpenType.DIALOG));
        designTable.addAction(new RemoveAction(designTable) {
            @Override
            protected void doRemove(Set selected, boolean autocommit) {
                CollectionDatasource datasource = target.getDatasource();
                Set<Entity> reloadedDesigns = new HashSet<>();
                for (Object obj : selected) {
                    Entity reloadedDesign = dataSupplier.reload((Entity) obj, View.LOCAL);
                    reloadedDesigns.add(reloadedDesign);
                }
                for (Object item : reloadedDesigns) {
                    datasource.removeItem((Entity) item);
                }

                if (autocommit && (datasource.getCommitMode() != Datasource.CommitMode.PARENT)) {
                    try {
                        datasource.commit();
                    } catch (RuntimeException e) {
                        datasource.refresh();
                        throw e;
                    }
                }
            }
        });

        Action designAction = new AbstractAction("design") {
            @Override
            public void actionPerform(Component component) {
                Set<Design> selected = designTable.getSelected();
                if (!selected.isEmpty()) {
                    String id = selected.iterator().next().getId().toString();
                    openDesigner(id);
                }
            }
        };
        designTable.addAction(designAction);
        designTable.setItemClickAction(designAction);

        designTable.addAction(new ScriptsAction());
        designTable.addAction(new LocalizeAction());
        designTable.addAction(new CompileAction(designTable));
        designTable.addAction(new DeployAction());

        designTable.addAction(new AbstractEntityAction<Design>("showAffectedDesigns", designTable) {

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
                Window window = openWindow("wf$Design.browse",
                        WindowManager.OpenType.THIS_TAB,
                        Collections.<String, Object>singletonMap("subprocId", "%" + getEntity().getId().toString() + "%"));
                window.addListener(new CloseListener() {
                    @Override
                    public void windowClosed(String actionId) {
                        table.getDatasource().refresh();
                        table.requestFocus();
                    }
                });
            }
        });

        designTable.addAction(new AbstractEntityAction<Design>("editProcessVariables", designTable) {
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
                getDialogParams().setWidth(900);
                getDialogParams().setHeight(600);

                final Window window = openWindow("wf$DesignProcessVariable.browse",
                        WindowManager.OpenType.DIALOG,
                        Collections.<String, Object>singletonMap("design", getEntity()));
                window.addListener(new CloseListener() {
                    @Override
                    public void windowClosed(String actionId) {
                        designDs.refresh();
                        table.requestFocus();
                    }
                });
            }
        });

        PopupButton notificationMatrixBtn = (PopupButton) getComponentNN("notificationMatrix");

        notificationMatrixBtn.addAction(new UploadNotificationMatrixAction());
        notificationMatrixBtn.addAction(new ClearNotificationMatrixAction());
        notificationMatrixBtn.addAction(new DownloadNotificationMatrix());
    }

    protected void initColumns() {
        designTable.addGeneratedColumn("notificationMatrix", new Table.ColumnGenerator<Design>() {
            @Override
            public Component generateCell(final Design entity) {
                if (BooleanUtils.isTrue(entity.getNotificationMatrixUploaded())) {
                    Button button = componentsFactory.createComponent(Button.class);
                    button.setStyleName("link");
                    button.setAction(new AbstractAction("showNotificationMatrix") {
                        @Override
                        public void actionPerform(Component component) {
                            Design d = getDsContext().getDataSupplier().reload(entity, "_local");
                            exportDisplay.show(new ByteArrayDataProvider(d.getNotificationMatrix()), "NotificationMatrix", ExportFormat.XLS);
                        }

                        @Override
                        public String getCaption() {
                            return getMessage("showNotificationMatrix");
                        }
                    });
                    return button;
                } else {
                    Label label = componentsFactory.createComponent(Label.class);
                    label.setValue(getMessage("notUploaded"));
                    return label;
                }
            }
        });

        variablesTable.addGeneratedColumn("value", new Table.ColumnGenerator() {
            @Override
            public Component generateCell(Entity entity) {
                final AbstractProcessVariable designProcessVariable = (AbstractProcessVariable) entity;
                Component componentValue = componentsFactory.createComponent(Label.class);
                String localizedValue = processVariableService.getLocalizedValue(designProcessVariable);
                ((com.haulmont.cuba.gui.components.Label) componentValue).setValue(localizedValue);
                return componentValue;
            }
        });
    }

    protected void openDesigner(String id) {
        String webAppUrl = configuration.getConfig(GlobalConfig.class).getWebAppUrl();
        String designerUrl = AppBeans.get(Configuration.class).getConfig(WfConfig.class).getDesignerUrl();
        StringBuilder url = new StringBuilder();
        url.append(webAppUrl)
                .append("/dispatch/")
                .append(designerUrl)
                .append("?id=")
                .append(id)
                .append("&s=")
                .append(AppBeans.get(UserSessionSource.class).getUserSession().getId());
        showWebPage(url.toString(), Collections.<String, Object>singletonMap("tryToOpenAsPopup", Boolean.TRUE));
    }

    protected class CopyAction extends ItemTrackingAction {
        public CopyAction() {
            super("copy");
        }

        @Override
        public void actionPerform(Component component) {
            Set<Design> selected = designTable.getSelected();
            if (!selected.isEmpty()) {
                final Design design = selected.iterator().next();
                UUID newId = service.copyDesign(design.getId());
                openDesigner(newId.toString());

                designDs.refresh();
            }
        }
    }

    protected class ExportAction extends AbstractAction {
        public ExportAction() {
            super("export");
        }

        @Override
        public void actionPerform(Component component) {
            Set selected = designTable.getSelected();
            if (!selected.isEmpty()) {
                try {
                    exportDisplay.show(new ByteArrayDataProvider(service.exportDesigns(selected)), "Designs", ExportFormat.ZIP);
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

    protected class ImportAction extends AbstractAction {
        public ImportAction() {
            super("import");
        }

        @Override
        public void actionPerform(Component component) {
            final ImportDialog importDialog = (ImportDialog) openWindow("wf$Design.import", WindowManager.OpenType.DIALOG);
            importDialog.addListener(new CloseListener() {
                @Override
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
                        designTable.getDatasource().refresh();
                    }
                }
            });
        }
    }

    protected class CompileAction extends AbstractEntityAction<Design> {

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
            showMessageDialog(getMessage("compilationResult"), result.toString(), MessageType.CONFIRMATION_HTML);
        }

        private Pair<String, String> compile(Design design) {
            DesignerService service = AppBeans.get(DesignerService.NAME);
            try {
                CompilationMessage message = service.compileDesign(design.getId());
                designDs.refresh();
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

    protected class DeployAction extends AbstractAction {
        public DeployAction() {
            super("deploy");
        }

        @Override
        public void actionPerform(Component component) {
            Set<Design> selected = designTable.getSelected();
            if (!selected.isEmpty()) {
                final Design design = selected.iterator().next();
                if (design.getCompileTs() == null) {
                    showNotification(getMessage("notification.notCompiled"), NotificationType.WARNING);
                } else {
                    getDialogParams().setWidth(500);
                    final DeployDesignWindow window = (DeployDesignWindow) openWindow("wf$Design.deploy",
                            WindowManager.OpenType.DIALOG,
                            Collections.<String, Object>singletonMap("design", design));
                    window.addListener(
                            new CloseListener() {
                                public void windowClosed(String actionId) {
                                    if ("close".equals(actionId) || "cancel".equals(actionId))
                                        return;
                                    if ("ok".equals(actionId)) {
                                        designDs.refresh();
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

    protected class ScriptsAction extends AbstractAction {
        public ScriptsAction() {
            super("scripts");
        }

        @Override
        public void actionPerform(Component component) {
            Set<Design> selected = designTable.getSelected();
            if (!selected.isEmpty()) {
                final Design design = selected.iterator().next();
                Window window = openWindow(
                        "wf$DesignScript.browse",
                        WindowManager.OpenType.THIS_TAB,
                        Collections.<String, Object>singletonMap("design", design)
                );
                window.addListener(new CloseListener() {
                    @Override
                    public void windowClosed(String actionId) {
                        designTable.requestFocus();
                    }
                });
            }
        }
    }

    protected class LocalizeAction extends AbstractAction {
        public LocalizeAction() {
            super("localize");
        }

        @Override
        public void actionPerform(Component component) {
            Set<Design> selected = designTable.getSelected();
            if (!selected.isEmpty()) {
                final Design design = selected.iterator().next();
                if (design.getCompileTs() == null) {
                    showNotification(getMessage("notification.notCompiled"), NotificationType.WARNING);
                } else {
                    Window window = openEditor("wf$Design.localize", design, WindowManager.OpenType.THIS_TAB);
                    window.addListener(new CloseListener() {
                        @Override
                        public void windowClosed(String actionId) {
                            if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                                designDs.refresh();
                            }
                            designTable.requestFocus();
                        }
                    });
                }
            }
        }
    }

    protected void showDesignProcessVariables(Design design) {
        getDialogParams().setWidth(900);
        getDialogParams().setHeight(600);
        final Window window = openWindow("wf$DesignProcessVariable.browse",
                WindowManager.OpenType.DIALOG,
                Collections.<String, Object>singletonMap("design", design));
        window.addListener(
                new CloseListener() {
                    public void windowClosed(String actionId) {
                        designDs.refresh();
                    }
                }
        );
    }

    protected class UploadNotificationMatrixAction extends AbstractAction {
        public UploadNotificationMatrixAction() {
            super("uploadNotificationMatrix");
        }

        @Override
        public void actionPerform(Component component) {
            Set<Design> selected = designTable.getSelected();
            if (!selected.isEmpty()) {
                Design selectedDesign = selected.iterator().next();
                final Design design = designDs.getDataSupplier().reload(selectedDesign, "_local");
                final NotificationMatrixWindow window = (NotificationMatrixWindow) openWindow("wf$Design.notificationMatrix", WindowManager.OpenType.DIALOG);
                window.addListener(
                        new CloseListener() {
                            public void windowClosed(String actionId) {
                                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                                    design.setNotificationMatrix(window.getBytes());
                                    design.setNotificationMatrixUploaded(true);
                                    designDs.getDataSupplier().commit(new CommitContext(Collections.singleton(design)));
                                    DesignerService service = ServiceLocator.lookup(DesignerService.NAME);
                                    service.saveNotificationMatrixFile(design);
                                    designDs.refresh();
                                }
                            }
                        }
                );
            }
        }
    }

    protected class ClearNotificationMatrixAction extends AbstractAction {
        public ClearNotificationMatrixAction() {
            super("clearNotificationMatrix");
        }

        @Override
        public void actionPerform(Component component) {
            Set<Design> selected = designTable.getSelected();
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
                                        designDs.refresh();

                                        designTable.requestFocus();
                                    }
                                },
                                new DialogAction(DialogAction.Type.NO) {
                                    @Override
                                    public void actionPerform(Component component) {
                                        designTable.requestFocus();
                                    }
                                }
                        }
                );
            }
        }
    }

    protected class DownloadNotificationMatrix extends AbstractAction {
        public DownloadNotificationMatrix() {
            super("downloadNotificationMatrix");
        }

        @Override
        public void actionPerform(Component component) {
            Set selected = designTable.getSelected();
            try {
                if (!selected.isEmpty()) {
                    final Design design = (Design) selected.iterator().next();
                    if (design.getCompileTs() != null) {
                        byte[] bytes = service.getNotificationMatrixTemplate(design.getUuid());
                        ByteArrayDataProvider array = new ByteArrayDataProvider(bytes);
                        exportDisplay.show(array, "NotificationMatrix", ExportFormat.XLS);
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