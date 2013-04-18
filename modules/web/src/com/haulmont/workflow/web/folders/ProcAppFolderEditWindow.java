/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.folders;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.entity.AppFolder;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.Folder;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.Table.Column;
import com.haulmont.cuba.gui.components.TwinColumn;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.gui.presentations.Presentations;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.app.folders.AppFolderEditWindow;
import com.haulmont.cuba.web.gui.components.*;
import com.haulmont.cuba.web.toolkit.VersionedThemeResource;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.entity.ProcAppFolder;
import com.haulmont.workflow.core.entity.ProcCondition;
import com.haulmont.workflow.core.entity.ProcState;
import com.vaadin.data.Property;
import com.vaadin.ui.*;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.persistence.MappedSuperclass;
import java.io.StringWriter;
import java.util.*;

/**
 * @author pavlov
 * @version $Id$
 */
public class ProcAppFolderEditWindow extends AppFolderEditWindow {

    Log log = LogFactory.getLog(ProcAppFolderEditWindow.class);

    private ProcConditionDatasource procConditionDatasource;
    private LookupField entityField;
    private TwinColumn rolesField;
    private String procConditionsXml;
    private TextField entityAliasField;

    protected Metadata metadata = AppBeans.get(Metadata.class);

    private boolean modified = false;

    public ProcAppFolderEditWindow(boolean adding, Folder folder, Presentations presentations, Runnable commitHandler) {
        super(false, folder, presentations, commitHandler);

        messagesPack = messagesPack + " com.haulmont.workflow.web.folders";

//        if (!adding) {
        details();
        setWidth(650, Unit.PIXELS);

        TabSheet tabSheet = new TabSheet();
        layout.addComponent(tabSheet, 3);

        VerticalLayout scriptTabLayout = new VerticalLayout();
        scriptTabLayout.setMargin(true);
        scriptTabLayout.setCaption(getMessage("scriptTab"));
        tabSheet.addComponent(scriptTabLayout);

        scriptTabLayout.addComponent(quantityScriptField);
        scriptTabLayout.addComponent(visibilityScriptField);

        VerticalLayout builderTabLayout = new VerticalLayout();
        builderTabLayout.setSpacing(true);
        builderTabLayout.setMargin(true);
        builderTabLayout.setCaption(getMessage("builderTab"));
        tabSheet.addComponent(builderTabLayout);

        initRolesField(builderTabLayout);

        WebGroupBox webGroupBox = new WebGroupBox();
        webGroupBox.setCaption(getMessage("conditions"));
        ComponentContainer groupBoxContainer = (ComponentContainer) WebComponentsHelper.unwrap(webGroupBox);
        builderTabLayout.addComponent(groupBoxContainer);

        initEntityField(groupBoxContainer);
        initConditionField(groupBoxContainer);

        initApplyButton(builderTabLayout);

        loadData();
//        }

    }

    @Override
    protected void initButtonOkListener() {
        okBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                AppFolder folder = (AppFolder) ProcAppFolderEditWindow.this.folder;
                if (StringUtils.isBlank(nameField.getValue())) {
                    String msg = getMessage("folders.folderEditWindow.emptyName");

                    App.getInstance().getWindowManager().showNotification(msg, IFrame.NotificationType.TRAY);
                    return;
                }
                folder.setName(nameField.getValue());
                folder.setTabName(tabNameField.getValue());

                if (sortOrderField.getValue() == null || "".equals(sortOrderField.getValue())) {
                    folder.setSortOrder(null);
                } else {
                    Object value = sortOrderField.getValue();
                    int sortOrder;
                    try {
                        sortOrder = Integer.parseInt((String) value);
                    } catch (NumberFormatException e) {
                        String msg = getMessage("folders.folderEditWindow.invalidSortOrder");
                        App.getInstance().getWindowManager().showNotification(msg, IFrame.NotificationType.TRAY);
                        return;
                    }
                    folder.setSortOrder(sortOrder);
                }

                Object parent = parentSelect.getValue();
                if (parent instanceof Folder)
                    folder.setParent((Folder) parent);
                else
                    folder.setParent(null);

                if (modified) {
                    buildFolder(false);
                }

                if (visibilityScriptField != null) {
                    String scriptText = visibilityScriptField.getValue();
                    folder.setVisibilityScript(scriptText);
                }
                if (quantityScriptField != null) {
                    String scriptText = quantityScriptField.getValue();
                    folder.setQuantityScript(scriptText);
                }
                folder.setApplyDefault(Boolean.valueOf(applyDefaultCb.getValue().toString()));

                ProcAppFolderEditWindow.this.commitHandler.run();

                close();
            }
        });
    }

    private void details() {
        GridLayout grid = new GridLayout(2, 4);
        grid.setSpacing(true);
        layout.replaceComponent(nameField, grid);

        grid.addComponent(new Label(nameField.getCaption()));
        nameField.setCaption(null);
        grid.addComponent(nameField);

        grid.addComponent(new Label(tabNameField.getCaption()));
        tabNameField.setCaption(null);
        grid.addComponent(tabNameField);

        grid.addComponent(new Label(parentSelect.getCaption()));
        parentSelect.setCaption(null);
        grid.addComponent(parentSelect);

        grid.addComponent(new Label(sortOrderField.getCaption()));
        sortOrderField.setCaption(null);
        grid.addComponent(sortOrderField);
    }

    private void initApplyButton(Layout layout) {
        Button add = new Button(getMessage("apply"));
        add.setIcon(new VersionedThemeResource("icons/ok.png"));
        add.setStyleName("icon");
        layout.addComponent(add);
        ((Layout.AlignmentHandler) layout).setComponentAlignment(add, Alignment.MIDDLE_RIGHT);
        add.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                buildFolder(true);
            }
        });
    }

    private void buildFolder(boolean apply) {
        try {
            Document doc = DocumentHelper.createDocument();

            Element rootEl = doc.addElement("root");

            String entityAlias = StringUtils.trimToNull(entityAliasField.getValue());
            if (procConditionDatasource.getItemIds().size() > 0 && entityAlias == null) {
                App.getInstance().getWindowManager().showNotification(
                        getMessage("folders.folderEditWindow.emptyEntityAlias"),
                        IFrame.NotificationType.TRAY);
            }

            rootEl.addElement("entity").addText(((MetaClass) entityField.getValue()).getName());
            rootEl.addElement("entityAlias").addText(entityAlias);

            Element rolesElement = rootEl.addElement("roles");
            Collection<Role> roles = rolesField.getValue();
            if (roles != null) {
                for (Role role : roles) {
                    rolesElement.addElement("role").addText(role.getName());
                }
            }

            Element conditionsElement = rootEl.addElement("conditions");

            Collection<UUID> itemIds = procConditionDatasource.getItemIds();
            if (itemIds != null) {
                for (UUID itemId : itemIds) {
                    ProcCondition procCondition = procConditionDatasource.getItem(itemId);
                    Element condition = conditionsElement.addElement("condition");
                    Proc proc = procCondition.getProc();
                    if (proc != null) {
                        condition.addElement("proc").addText(proc.getCode());
                    }

                    Collection<ProcState> states = procCondition.getStates();
                    String statesString = "";
                    for (ProcState state : states) {
                        statesString += "," + state.getName();
                    }
                    if (statesString.length() > 0) {
                        condition.addElement("inExpr").addText(procCondition.getInExpr().toString());
                        condition.addElement("states").addText(statesString.substring(1));
                    }
                }
            }


            StringWriter writer = new StringWriter();
            Dom4j.writeDocument(doc, true, writer);
            procConditionsXml = writer.toString();
            ((ProcAppFolder) folder).setProcAppFolderXml(procConditionsXml);

            FolderBuilder builder = new FolderBuilder((ProcAppFolder) folder);
            builder.build();
            visibilityScriptField.setValue(((ProcAppFolder) folder).getVisibilityScript());
            quantityScriptField.setValue(((ProcAppFolder) folder).getQuantityScript());

            modified = false;
            if (apply)
                App.getInstance().getWindowManager().showNotification(getMessage("buildSuccessful"), IFrame.NotificationType.HUMANIZED);
        } catch (Exception e) {
            if (apply)
                App.getInstance().getWindowManager().showNotification(getMessage("buildFailed"), IFrame.NotificationType.HUMANIZED);
            log.error(e);
        }
    }

    private void initEntityField(ComponentContainer layout) {
        GridLayout grid = new GridLayout(4, 1);
        grid.setSpacing(true);
        layout.addComponent(grid);


        Label label = new Label(getMessage("entity"));
        grid.addComponent(label);

        entityField = new WebLookupField();
        entityField.setWidth("250px");
        entityField.setRequired(true);
        entityField.addListener(new ValueListener() {
            @Override
            public void valueChanged(Object o, String s, Object o1, Object o2) {
                modified = true;
            }
        });
        grid.addComponent(WebComponentsHelper.unwrap(entityField));

        MetaClass cardMetaClass = metadata.getSession().getClass("wf$Card");
        Collection<MetaClass> metaClasses = cardMetaClass.getDescendants();

        Map<String, Object> items = new TreeMap<>();
        for (MetaClass metaClass : metaClasses) {
            if (metaClass.getJavaClass().getAnnotation(MappedSuperclass.class) == null) {
                items.put(metaClass.getName() + " (" + messages.getTools().getEntityCaption(metaClass) + ")", metaClass);
            }
        }

        entityField.setOptionsMap(items);

        Label entityAliasLabel = new Label(getMessage("entityAlias"));
        grid.addComponent(entityAliasLabel);

        entityAliasField = new TextField();
        entityAliasField.setWidth("50px");
        entityAliasField.setRequired(true);
        entityAliasField.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                modified = true;
            }
        });
        grid.addComponent(entityAliasField);
    }

    private void initRolesField(Layout layout) {
        WebGroupBox webGroupBox = new WebGroupBox();
        webGroupBox.setCaption(getMessage("roles"));

        ComponentContainer cubaGroupBox = (ComponentContainer) WebComponentsHelper.unwrap(webGroupBox);
        layout.addComponent(cubaGroupBox);

        rolesField = new WebTwinColumn();
        rolesField.setWidth("500px");
        rolesField.setHeight("110px");

        cubaGroupBox.addComponent(WebComponentsHelper.unwrap(rolesField));

        CollectionDatasource rolesDs = new DsBuilder()
                .setMetaClass(metadata.getSession().getClass("sec$Role"))
                .setViewName(View.MINIMAL)
                .buildCollectionDatasource();
        rolesDs.refresh();

        rolesField.setOptionsDatasource(rolesDs);
        rolesField.addListener(new ValueListener() {
            @Override
            public void valueChanged(Object o, String s, Object o1, Object o2) {
                modified = true;
            }
        });
    }

    private void initConditionField(ComponentContainer layout) {

        HorizontalLayout hBox = new HorizontalLayout();
        hBox.setSpacing(true);
        layout.addComponent(hBox);

        Button add = new Button(getMessage("add"));
        hBox.addComponent(add);
        add.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Window window = new ProcConditionEditWindow(procConditionDatasource);
                window.setWidth("600px");
                window.setModal(true);
                window.center();

                App.getInstance().getAppUI().addWindow(window);
            }
        });


        Button remove = new Button(getMessage("remove"));
        hBox.addComponent(remove);

        final WebTable table = new WebTable();
        table.setWidth("100%");
        table.setHeight("100px");
        Table vTable = (Table) WebComponentsHelper.unwrap(table);
        layout.addComponent(vTable);
        vTable.setColumnCollapsingAllowed(false);
        vTable.setColumnReorderingAllowed(false);

        remove.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                Entity entity = table.getSingleSelected();
                if (entity != null) {
                    procConditionDatasource.removeItem((ProcCondition) entity);
                }
            }
        });

        MetaClass metaClass = metadata.getSession().getClassNN(ProcCondition.class);

        Column procColumn = new Column(metaClass.getPropertyPath("proc"));
        procColumn.setCaption(getMessage("proc"));
        table.addColumn(procColumn);

        Column inExprColumn = new Column(metaClass.getPropertyPath("inExpr"));
        inExprColumn.setCaption("");
        inExprColumn.setFormatter(new com.haulmont.cuba.gui.components.Formatter() {
            @Override
            public String format(Object value) {
                if (BooleanUtils.isTrue((Boolean) value)) {
                    return getMessage("in");
                } else {
                    return getMessage("notIn");
                }
            }
        });
        table.addColumn(inExprColumn);

        Column statesColumn = new Column(metaClass.getPropertyPath("states"));
        statesColumn.setCaption(getMessage("states"));
        table.addColumn(statesColumn);

        procConditionDatasource = new ProcConditionDatasource();
        procConditionDatasource.setup(null, null, "procConditionDs", metaClass,
                metadata.getViewRepository().getView(metaClass, View.LOCAL));
        procConditionDatasource.loadData(Collections.<String, Object>emptyMap());
        table.setDatasource(procConditionDatasource);

        procConditionDatasource.addListener(new CollectionDsListenerAdapter<ProcCondition>() {
            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation, List<ProcCondition> items) {
                modified = true;
            }
        });
    }

    private void loadData() {
        procConditionsXml = ((ProcAppFolder) folder).getProcAppFolderXml();
        String filterId = ((ProcAppFolder) folder).getFilterComponentId();
        if (procConditionsXml != null) {
            Document doc = Dom4j.readDocument(procConditionsXml);
            Element rootElem = doc.getRootElement();

            String entity = rootElem.elementText("entity");
            setEntity(entity);

            String entityAlias = rootElem.elementText("entityAlias");
            entityAliasField.setValue(entityAlias);

            Set<Role> selected = new HashSet<>();
            for (Element roleEl : Dom4j.elements(rootElem.element("roles"), "role")) {
                String role = roleEl.getText();
                CollectionDatasource rolesDs = rolesField.getOptionsDatasource();
                for (Object key : rolesDs.getItemIds()) {
                    Role roleItem = (Role) rolesDs.getItem(key);
                    if (role.equals(roleItem.getName())) {
                        selected.add(roleItem);
                        break;
                    }
                }
            }
            rolesField.setValue(selected);

            for (Element conditionEl : Dom4j.elements(rootElem.element("conditions"), "condition")) {
                String proc = conditionEl.elementText("proc");
                String states = conditionEl.elementText("states");
                String inExpr = conditionEl.elementText("inExpr");


                if (proc != null || states != null) {
                    ProcCondition procCondition = new ProcCondition();
                    List<ProcState> list = loadStates(proc, states);

                    if (proc != null) {
                        Proc p = loadProc(proc);
                        procCondition.setProc(p);
                    }

                    if (states != null) {
                        procCondition.setInExpr(BooleanUtils.isTrue(Boolean.valueOf(inExpr)));
                        procCondition.setStates(list);
                    }
                    procConditionDatasource.addItem(procCondition);
                }
            }
        } else if (filterId != null) {
            String entity = filterId.substring(filterId.indexOf("[") + 1, filterId.indexOf(".browse"));
            setEntity(entity);
        }
        modified = false;
    }

    private List<ProcState> loadStates(String proc, String states) {
        LoadContext ctx = new LoadContext(ProcState.class);
        ctx.setView("browse");
        LoadContext.Query query = ctx.setQueryString("select e from wf$ProcState e where e.name in (:states)" +
                (proc != null ? " and e.proc.code = :proc" : ""));
        Map<String, Object> params = new HashMap<>();
        params.put("states", Arrays.asList(states.split(",")));
        if (proc != null) {
            params.put("proc", proc);
        }
        query.setParameters(params);
        return AppBeans.get(DataService.class).loadList(ctx);
    }

    private Proc loadProc(String proc) {
        LoadContext ctx = new LoadContext(Proc.class);
        ctx.setView(View.LOCAL);
        LoadContext.Query query = ctx.setQueryString("select p from wf$Proc p where p.code = :proc");
        Map<String, Object> params = new HashMap<>();
        params.put("proc", proc);
        query.setParameters(params);
        List<Entity> list = AppBeans.get(DataService.class).loadList(ctx);
        return list.isEmpty() ? null : (Proc) list.iterator().next();
    }

    private void setEntity(String entityName) {
        for (Object mc : entityField.getOptionsMap().values()) {
            if (entityName.equals(((MetaClass) mc).getName())) {
                entityField.setValue(mc);
                break;
            }
        }
    }
}