/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.web.ui.proc;

import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.TabSheet;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.*;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.gui.data.impl.CollectionPropertyDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.gui.components.WebButton;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.cuba.web.gui.components.WebLookupField;
import com.haulmont.workflow.core.app.ProcRolePermissionsService;
import com.haulmont.workflow.core.entity.DefaultProcActor;
import com.haulmont.workflow.core.entity.OrderFillingType;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.entity.ProcRole;
import com.vaadin.data.Property;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

public class ProcEditor extends AbstractEditor<Proc> {

    @Inject
    protected Table rolesTable;

    @Inject
    protected Table dpaTable;

    @Inject
    protected Datasource<Proc> procDs;

    @Inject
    protected CollectionDatasource<DefaultProcActor, UUID> dpaDs;

    @Inject
    protected CollectionDatasource<Role, UUID> secRoles;

    @Inject
    protected CollectionDatasource<ProcRole, UUID> rolesDs;

    @Inject
    protected CollectionDatasource permissionsDs;

    @Inject
    protected WebButton moveUp;

    @Inject
    protected WebButton moveDown;

    @Inject
    protected TabSheet tabsheet;

    @Inject
    protected Table stagesTable;

    @Inject
    protected Messages messages;

    @Inject
    protected ProcRolePermissionsService procRolePermissionsService;

    protected Table permissionsTable;
    protected Set<TabSheet.Tab> initedTabs = new HashSet<>();
    protected Map<Object, Object> multiUserMap = new HashMap<>();
    protected Map<Object, Object> assignToCreatorMap = new HashMap<>();
    protected Map<Object, Object> orderFillingTypeMap = new HashMap<>();
    protected List<Action> dpaActions = new ArrayList<>();

    protected CreateAction createDpaAction;

    protected boolean isMultiUserEditable(ProcRole pr) {
        return true;
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        secRoles.refresh();

        CreateAction createAction = new CreateAction(rolesTable){
            @Override
            public Map<String, Object> getInitialValues() {
                Map<String, Object> values = new HashMap<>();
                values.put("proc", procDs.getItem());
                int order = findMaxSortOrder() + 1;
                values.put("sortOrder", order);
                return values;
            }

            @Override
            public Map<String, Object> getWindowParams() {
                return null;
            }
        };

        rolesTable.addAction(createAction);
        rolesTable.addAction(new RemoveAction(rolesTable, false));

        createDpaAction = new CreateAction(dpaTable){
            @Override
            public Map<String, Object> getWindowParams() {
                List<UUID> userIds = new LinkedList<>();
                for (UUID uuid : dpaDs.getItemIds()) {
                    DefaultProcActor dpa = dpaDs.getItem(uuid);
                    User user = null;
                    if (dpa != null) {
                        user = dpa.getUser();
                    }
                    if (user != null) {
                        userIds.add(user.getId());
                    }
                }
                Map<String, Object> values = new HashMap<>();
                values.put("userIds", userIds);
                values.put("isMulti", rolesDs.getItem().getMultiUser());
                return values;
            }

            @Override
            public Map<String, Object> getInitialValues() {
                Map<String, Object> values = new HashMap<>();
                values.put("procRole", rolesDs.getItem());
                return values;
            }
        };

        dpaTable.addAction(createDpaAction);
        dpaActions.add(createDpaAction);

        EditAction dpaEditAction = new EditAction(dpaTable, WindowManager.OpenType.DIALOG){
            @Override
            public Map<String, Object> getWindowParams() {
                List<UUID> userIds = new LinkedList<> ();
                for (UUID uuid : dpaDs.getItemIds()) {
                    DefaultProcActor dpa = dpaDs.getItem(uuid);
                    User user = null;
                    if (dpa != null) {
                        user = dpa.getUser();
                    }
                    if (user != null && !dpa.equals(dpaDs.getItem()))
                        userIds.add(user.getId());
                }
                Map<String, Object> values = new HashMap<>();
                values.put("userIds", userIds);
                values.put("isMulti", rolesDs.getItem().getMultiUser());
                return values;
            }
        };

        dpaTable.addAction(dpaEditAction);
        dpaActions.add(dpaEditAction);

        RemoveAction dpaRemoveAction = new RemoveAction(dpaTable,false);
        dpaTable.addAction(dpaRemoveAction);
        dpaActions.add(dpaRemoveAction);

        for (Action action : dpaActions) {
            action.setEnabled(false);
        }

        dpaDs.addListener(new CollectionDsListenerAdapter<DefaultProcActor>(){
            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation, List<DefaultProcActor> items) {
                enableDpaActions();
            }

            @Override
            public void itemChanged(Datasource<DefaultProcActor> ds, @Nullable DefaultProcActor prevItem, @Nullable DefaultProcActor item) {
                enableCheckBox();
            }
        });

        getDsContext().addListener(new DsContext.CommitListener() {
            @Override
            public void beforeCommit(CommitContext context) {
                for (Entity entity : context.getCommitInstances()) {
                    if (procDs.getItem().equals(entity)) {
                        Proc p = (Proc) entity;
                        if (p.getCardTypes() != null && !p.getCardTypes().startsWith(","))
                            p.setCardTypes("," + p.getCardTypes());
                        if (p.getCardTypes() != null && !p.getCardTypes().endsWith(","))
                            p.setCardTypes(p.getCardTypes() + ",");
                        return;
                    }
                }
            }

            @Override
            public void afterCommit(CommitContext context, Set<Entity> result) {
            }
        });

        rolesDs.addListener(new DsListenerAdapter<ProcRole>(){
            @Override
            public void valueChanged(ProcRole source, String property, @Nullable Object prevValue, @Nullable Object value) {
                enableDpaActions();
            }

            @Override
            public void itemChanged(Datasource<ProcRole> ds, @Nullable ProcRole prevItem, @Nullable ProcRole item) {
                enableDpaActions();
            }
        });

        moveUp.setAction(new AbstractAction("moveUp") {
            @Override
            public void actionPerform(com.haulmont.cuba.gui.components.Component component) {
                Set selected = rolesTable.getSelected();
                if (selected.isEmpty())
                    return;

                ProcRole curCr = (ProcRole) selected.iterator().next();
                UUID prevId = ((CollectionPropertyDatasourceImpl<ProcRole, UUID>)rolesDs).prevItemId(curCr.getId());
                if (prevId == null)
                    return;

                Integer tmp = curCr.getSortOrder();
                ProcRole prevCr = rolesDs.getItem(prevId);
                if (prevCr != null) {
                    curCr.setSortOrder(prevCr.getSortOrder());
                    prevCr.setSortOrder(tmp);
                }

                sortRolesDs("sortOrder");
            }
        });

        moveDown.setAction(new AbstractAction("moveDown") {
            @Override
            public void actionPerform(com.haulmont.cuba.gui.components.Component component) {
                Set selected = rolesTable.getSelected();
                if (selected.isEmpty())
                    return;

                ProcRole curCr = (ProcRole) selected.iterator().next();
                UUID nextId = ((CollectionPropertyDatasourceImpl<ProcRole, UUID>) rolesDs).nextItemId(curCr.getId());
                if (nextId == null)
                    return;

                Integer tmp = curCr.getSortOrder();
                ProcRole nextCr = rolesDs.getItem(nextId);
                if (nextCr != null) {
                    curCr.setSortOrder(nextCr.getSortOrder());
                    nextCr.setSortOrder(tmp);
                }

                sortRolesDs("sortOrder");
            }
        });

        initLazyTabs();
    }

    protected void sortRolesDs(String property) {
        CollectionDatasource.Sortable.SortInfo<MetaPropertyPath> sortInfo = new CollectionDatasource.Sortable.SortInfo<>();
        sortInfo.setOrder(CollectionDatasource.Sortable.Order.ASC);
        sortInfo.setPropertyPath(rolesDs.getMetaClass().getPropertyPath(property));
        CollectionDatasource.Sortable.SortInfo[] sortInfos = new CollectionDatasource.Sortable.SortInfo[1];
        sortInfos[0] = sortInfo;
        ((CollectionPropertyDatasourceImpl) rolesDs).sort(sortInfos);
        rolesDs.refresh();
    }

    protected int findMaxSortOrder() {
        int max = 0;
        for (UUID id: rolesDs.getItemIds()) {
            ProcRole pr = rolesDs.getItem(id);
            if (pr != null) {
                if (pr.getSortOrder() != null && pr.getSortOrder() > max) {
                    max = pr.getSortOrder();
                }
            }
        }
        return max;
    }

    protected void initLazyTabs() {
        tabsheet.addListener(new TabSheet.TabChangeListener() {
            @Override
            public void tabChanged(TabSheet.Tab newTab) {
                if (!initedTabs.contains(newTab)) {
                    initedTabs.add(newTab);
                    if (newTab.getName().equals("stagesTab")) {
                        initStagesTab();
                    }
                }
            }
        });
    }

    protected void initStagesTab() {
        stagesTable.addAction(new CreateAction(stagesTable, WindowManager.OpenType.THIS_TAB){
            @Override
            public Map<String, Object> getWindowParams() {
                return null;
            }

            @Override
            public Map<String, Object> getInitialValues() {
                return Collections.<String, Object> singletonMap("proc", procDs.getItem());
            }
        });
        stagesTable.addAction(new EditAction(stagesTable, WindowManager.OpenType.THIS_TAB));
        stagesTable.addAction(new RemoveAction(stagesTable, false));
    }


    public void commitAndClose() {
        if (permissionsDs.isModified()) {
            procRolePermissionsService.clearPermissionsCache();
        }
        super.commitAndClose();
    }

    public void setItem(Entity item) {
        super.setItem(item);
        Proc proc = getItem();
        final CollectionDatasource<ProcRole, UUID> rolesDs = rolesTable.getDatasource();
        final com.vaadin.ui.Table vTable = (com.vaadin.ui.Table) WebComponentsHelper.unwrap(rolesTable);
        MetaPropertyPath pp = rolesDs.getMetaClass().getPropertyPath("code");
        vTable.removeGeneratedColumn(pp);
        final Map<Object, com.vaadin.ui.Component> codeMap = new HashMap<>();
        vTable.addGeneratedColumn(pp, new com.vaadin.ui.Table.ColumnGenerator() {
            @Override
            public Object generateCell(com.vaadin.ui.Table source, Object itemId, Object columnId) {
                if (codeMap.containsKey(itemId))
                    return codeMap.get(itemId);
                ProcRole pr = rolesDs.getItem((UUID) itemId);
                com.vaadin.ui.Component component = null;

                if (pr != null) {
                    if (PersistenceHelper.isNew(pr) && rolesTable.isEditable()) {
                        final UUID uuid = (UUID) itemId;
                        final com.vaadin.ui.TextField textField = new com.vaadin.ui.TextField();
                        textField.setValue(pr.getCode());
                        textField.addValueChangeListener(new Property.ValueChangeListener() {
                            @Override
                            public void valueChange(Property.ValueChangeEvent event) {
                                ProcRole procRole = rolesDs.getItem(uuid);
                                if (procRole != null) {
                                    (procRole).setCode(textField.getValue());
                                }
                            }
                        });
                        component = textField;
                    } else {
                        component = StringUtils.isNotBlank(pr.getCode()) ? new com.vaadin.ui.Label(pr.getCode()) : null;
                    }
                }
                if (component != null)
                    codeMap.put(itemId, component);
                return component;
            }
        });

        pp = rolesDs.getMetaClass().getPropertyPath("name");
        vTable.removeGeneratedColumn(pp);
        final Map<Object, com.vaadin.ui.Component> nameMap = new HashMap<>();
        vTable.addGeneratedColumn(pp, new com.vaadin.ui.Table.ColumnGenerator() {
            @Override
            public Object generateCell(com.vaadin.ui.Table source, Object itemId, Object columnId) {
                if (nameMap.containsKey(itemId))
                    nameMap.get(itemId);
                ProcRole pr =  rolesDs.getItem((UUID) itemId);
                com.vaadin.ui.Component component = null;
                if (pr != null) {
                    if (rolesTable.isEditable()) {
                        final Object uuid = itemId;
                        final com.vaadin.ui.TextField textField = new com.vaadin.ui.TextField();
                        textField.setValue(pr.getName());
                        textField.addValueChangeListener(new Property.ValueChangeListener() {
                            @Override
                            public void valueChange(Property.ValueChangeEvent event) {
                                ProcRole procRole = rolesDs.getItem((UUID) uuid);
                                if ((procRole) != null) {
                                    (procRole).setName(textField.getValue());
                                }
                            }
                        });
                        component = textField;
                    } else {
                        component = StringUtils.isNotBlank(pr.getName()) ? new com.vaadin.ui.Label(pr.getName()) : null;
                    }
                }
                nameMap.put(itemId, component);
                return component;
            }
        });

        pp = rolesDs.getMetaClass().getPropertyPath("multiUser");
        vTable.removeGeneratedColumn(pp);
        vTable.addGeneratedColumn(pp, new com.vaadin.ui.Table.ColumnGenerator() {
            @Override
            public Object generateCell(com.vaadin.ui.Table source, final Object itemId, Object columnId) {
                if (multiUserMap.containsKey(itemId))
                    return multiUserMap.get(itemId);
                ProcRole pr = rolesDs.getItem((UUID) itemId);
                com.vaadin.ui.Component component = null;
                if (pr != null){
                    final UUID uuid = (UUID) itemId;
                    final com.vaadin.ui.CheckBox checkBox = new com.vaadin.ui.CheckBox();
                    checkBox.setValue(pr.getMultiUser());
                    checkBox.setImmediate(true);
                    if (rolesTable.isEditable())
                        checkBox.setReadOnly(!isMultiUserEditable(pr) || BooleanUtils.isTrue(pr.getMultiUser()) && pr.getDefaultProcActors() != null &&
                                pr.getDefaultProcActors().size() > 1);
                    else
                        checkBox.setReadOnly(!rolesTable.isEditable());
                    checkBox.addValueChangeListener(new Property.ValueChangeListener() {
                        @Override
                        public void valueChange(Property.ValueChangeEvent event) {
                            ProcRole procRole = rolesDs.getItem(uuid);
                            rolesTable.setSelected(procRole);
                            if (procRole != null) {
                                procRole.setMultiUser(checkBox.getValue());
                            }
                            if (orderFillingTypeMap.containsKey(itemId)) {
                                com.vaadin.ui.Component c = (com.vaadin.ui.Component) orderFillingTypeMap.get(itemId);
                                if (c != null) c.setVisible(checkBox.getValue());
                            }
                        }
                    });
                    component = checkBox;
                }
                multiUserMap.put(itemId, component);
                return component;
            }
        });

        pp = rolesDs.getMetaClass().getPropertyPath("assignToCreator");
        vTable.removeGeneratedColumn(pp);
        vTable.addGeneratedColumn(pp, new com.vaadin.ui.Table.ColumnGenerator() {
            @Override
            public Object generateCell(com.vaadin.ui.Table source, final Object itemId, Object columnId) {
                if (assignToCreatorMap.containsKey(itemId))
                    return assignToCreatorMap.get(itemId);
                ProcRole pr = rolesDs.getItem((UUID) itemId);
                com.vaadin.ui.Component component = null;
                if (pr != null){
                    final UUID uuid = (UUID) itemId;
                    final com.vaadin.ui.CheckBox checkBox = new com.vaadin.ui.CheckBox();
                    checkBox.setValue(pr.getAssignToCreator());
                    if (rolesTable.isEditable()) {
                        if (pr.getMultiUser())
                            checkBox.setReadOnly(false);
                        else
                            checkBox.setReadOnly( pr.getDefaultProcActors() != null && pr.getDefaultProcActors().size() > 0);
                    } else
                        checkBox.setReadOnly(true);
                    checkBox.setImmediate(true);
                    checkBox.addValueChangeListener(new Property.ValueChangeListener() {
                        @Override
                        public void valueChange(Property.ValueChangeEvent event) {
                            ProcRole procRole = rolesDs.getItem(uuid);
                            rolesTable.setSelected(procRole);
                            if (procRole != null) {
                                procRole.setAssignToCreator(checkBox.getValue());
                            }
                        }
                    });
                    component = checkBox;
                }
                assignToCreatorMap.put(itemId, component);
                return component;
            }
        });

        pp = rolesDs.getMetaClass().getPropertyPath("role");
        vTable.removeGeneratedColumn(pp);
        final Map<Object, Object> roleMap = new HashMap<>();
        vTable.addGeneratedColumn(pp, new com.vaadin.ui.Table.ColumnGenerator() {
            @Override
            public Object generateCell(com.vaadin.ui.Table source, Object itemId, Object columnId) {
                if (roleMap.containsKey(itemId))
                    return roleMap.get(itemId);
                ProcRole pr = rolesDs.getItem((UUID) itemId);
                com.vaadin.ui.Component component = null;
                if (pr != null) {
                    if (rolesTable.isEditable()) {
                        final Object uuid = itemId;
                        WebLookupField usersLookup = new WebLookupField();
                        usersLookup.setOptionsDatasource(secRoles);
                        usersLookup.setValue(pr.getRole());
                        usersLookup.setWidth("100%");
                        usersLookup.setEditable(rolesTable.isEditable());
                        final com.vaadin.ui.AbstractSelect rolesSelect = (com.vaadin.ui.AbstractSelect) WebComponentsHelper.unwrap(usersLookup);
                        rolesSelect.addValueChangeListener(new Property.ValueChangeListener() {
                            @Override
                            public void valueChange(Property.ValueChangeEvent event) {
                                if (rolesSelect.getValue() != null) {
                                    Role role = secRoles.getItem(((Role) rolesSelect.getValue()).getUuid());
                                    ProcRole procRole = rolesDs.getItem((UUID) uuid);
                                    if (procRole != null) {
                                        procRole.setRole(role);
                                    }
                                }
                            }
                        });
                        return rolesSelect;
                    } else {
                        component = pr.getRole() != null ? new com.vaadin.ui.Label(pr.getRole().getInstanceName()) : null;
                    }
                }
                roleMap.put(itemId, component);
                return component;
            }
        });

        pp = rolesDs.getMetaClass().getPropertyPath("orderFillingType");
        vTable.removeGeneratedColumn(pp);
        vTable.addGeneratedColumn(pp, new com.vaadin.ui.Table.ColumnGenerator() {
            @Override
            public Object generateCell(com.vaadin.ui.Table source, Object itemId, Object columnId) {
                if (orderFillingTypeMap.containsKey(itemId))
                    return orderFillingTypeMap.get(itemId);
                ProcRole pr = rolesDs.getItem((UUID) itemId);
                com.vaadin.ui.Component component = null;
                if (pr != null) {
                    final Object uuid = itemId;
                    if (rolesTable.isEditable()) {
                        WebLookupField orderFillingTypeLookup = new WebLookupField();
                        Map<String, Object> types = new HashMap<>();
                        for (OrderFillingType oft: OrderFillingType.values()) {
                            types.put(messages.getMessage(oft), oft.getId());
                        }
                        orderFillingTypeLookup.setOptionsMap(types);
                        orderFillingTypeLookup.setValue(pr.getOrderFillingType());
                        orderFillingTypeLookup.setWidth("100%");
                        orderFillingTypeLookup.setVisible(pr.getMultiUser());
                        orderFillingTypeLookup.setEditable(rolesTable.isEditable());

                        final com.vaadin.ui.AbstractSelect orderFillingTypeSelect =
                                (com.vaadin.ui.AbstractSelect) WebComponentsHelper.unwrap(orderFillingTypeLookup);
                        orderFillingTypeSelect.setNullSelectionAllowed(false);
                        orderFillingTypeSelect.addValueChangeListener(new Property.ValueChangeListener() {
                            @Override
                            public void valueChange(Property.ValueChangeEvent event) {
                                ProcRole procRole = rolesDs.getItem((UUID) uuid);
                                if (procRole != null) {
                                    procRole.setOrderFillingType((String) orderFillingTypeSelect.getValue());
                                }
                            }
                        });
                        component = orderFillingTypeSelect;
                    } else {
                        component = StringUtils.isNotBlank(pr.getOrderFillingType()) ? new com.vaadin.ui.Label(messages.getMessage
                                (OrderFillingType.fromId(pr.getOrderFillingType()))) : null;
                    }
                }
                orderFillingTypeMap.put(itemId, component);
                return component;
            }
        });

        procDs.setItem(proc);
    }

    protected void enableCheckBox() {
        ProcRole pr = rolesDs.getItem();

        if (pr == null)
            return;

        if (BooleanUtils.isTrue(pr.getMultiUser()) && dpaDs.size() > 1) {
            if (multiUserMap.get(pr.getUuid())  != null) {
                ((com.vaadin.ui.Component) multiUserMap.get(pr.getUuid())).setReadOnly(true);
            }

        } else {
            if (dpaDs.size() > 0)
                if (multiUserMap.get(pr.getUuid()) != null) {
                ((com.vaadin.ui.Component) multiUserMap.get(pr.getUuid())).setReadOnly(!isMultiUserEditable(pr)
                        || BooleanUtils.isTrue(pr.getAssignToCreator()));
                }
            else if (multiUserMap.get(pr.getUuid()) != null) {
                    ((com.vaadin.ui.Component) multiUserMap.get(pr.getUuid())).setReadOnly(!isMultiUserEditable(pr));
                }
        }
        if (!BooleanUtils.isTrue(pr.getMultiUser()) && dpaDs.size() > 0) {
            if (assignToCreatorMap.get(pr.getUuid()) != null) {
                ((com.vaadin.ui.Component) assignToCreatorMap.get(pr.getUuid())).setReadOnly(true);
            }
        } else if (assignToCreatorMap.get(pr.getUuid()) != null) {
            ((com.vaadin.ui.Component) assignToCreatorMap.get(pr.getUuid())).setReadOnly(false);
        }
    }

    protected void enableDpaActions() {
        ProcRole item = rolesDs.getItem();

        enableCheckBox();

        for (Action action : dpaActions) {
            action.setEnabled(item != null);
        }
        if (item != null) {
            if (!BooleanUtils.isTrue(item.getMultiUser()) && (BooleanUtils.isTrue(item.getAssignToCreator() || !dpaDs.getItemIds().isEmpty())))
                createDpaAction.setEnabled(false);
            else
                createDpaAction.setEnabled(createDpaAction.isEnabled() &&
                        (item.getMultiUser() || dpaDs.getItemIds().isEmpty()));
        }
    }
}
