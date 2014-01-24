/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.app.proc;

import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.CollectionPropertyDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.DatasourceImpl;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.DefaultProcActor;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.entity.ProcRole;
import org.apache.commons.lang.BooleanUtils;

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
    protected CollectionDatasource<Role, UUID> secRolesDs;

    @Inject
    protected CollectionDatasource<ProcRole, UUID> rolesDs;

    @Inject
    protected Button moveUp;

    @Inject
    protected Button moveDown;

    @Inject
    protected Messages messages;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        createRolesTableActions();
        createDpaTableActions();

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
    }

    public void setItem(Entity item) {
        super.setItem(item);
        invisibleRolesFiltering();
    }

    protected void invisibleRolesFiltering() {
        List<ProcRole> roles = new ArrayList<>();
        for (ProcRole procRole : getItem().getRoles()) {
            if (BooleanUtils.isNotTrue(procRole.getInvisible())) {
                roles.add(procRole);
            }
        }
        getItem().setRoles(roles);
        ((DatasourceImpl<Proc>) procDs).setModified(false);
    }

    protected void createRolesTableActions() {
        CreateAction createAction = new CreateAction(rolesTable) {
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

        moveUp.setAction(new AbstractAction("moveUp") {
            @Override
            public void actionPerform(com.haulmont.cuba.gui.components.Component component) {
                Set selected = rolesTable.getSelected();
                if (selected.isEmpty())
                    return;

                ProcRole curCr = (ProcRole) selected.iterator().next();
                UUID prevId = ((CollectionPropertyDatasourceImpl<ProcRole, UUID>) rolesDs).prevItemId(curCr.getId());
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

    }

    protected void createDpaTableActions() {
        CreateAction createDpaAction = new CreateAction(dpaTable) {
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
        createDpaAction.setOpenType(WindowManager.OpenType.DIALOG);
        dpaTable.addAction(createDpaAction);

        EditAction dpaEditAction = new EditAction(dpaTable, WindowManager.OpenType.DIALOG) {
            @Override
            public Map<String, Object> getWindowParams() {
                List<UUID> userIds = new LinkedList<>();
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

        RemoveAction dpaRemoveAction = new RemoveAction(dpaTable, false);
        dpaTable.addAction(dpaRemoveAction);
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
        for (UUID id : rolesDs.getItemIds()) {
            ProcRole pr = rolesDs.getItem(id);
            if (pr != null) {
                if (pr.getSortOrder() != null && pr.getSortOrder() > max) {
                    max = pr.getSortOrder();
                }
            }
        }
        return max;
    }

    protected boolean isMultiUserEditable(ProcRole pr) {
        return true;
    }

    @Override
    protected void postValidate(ValidationErrors errors) {
        for (ProcRole procRole : rolesDs.getItems()) {
            if (!procRole.getMultiUser() && (procRole.getDefaultProcActors() != null && procRole.getDefaultProcActors().size() > 1)) {
                errors.add(formatMessage("proc.validation.error1", procRole.getName()));
            }

            if (!isMultiUserEditable(procRole)) {
                errors.add(formatMessage("proc.validation.error2", procRole.getName()));
            }

            if (!procRole.getMultiUser() && (procRole.getDefaultProcActors() != null && procRole.getDefaultProcActors().size() > 0) && procRole.getAssignToCreator()) {
                errors.add(formatMessage("proc.validation.error3", procRole.getName()));
            }
        }
    }
}
