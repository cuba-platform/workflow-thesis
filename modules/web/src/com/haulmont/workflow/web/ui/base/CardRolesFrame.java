/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 02.12.2009 10:11:47
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base;

import com.google.common.base.Preconditions;
import com.haulmont.cuba.core.entity.Entity;
import static com.haulmont.cuba.gui.WindowManager.OpenType;

import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;

import java.util.*;
import java.util.List;

public class CardRolesFrame extends AbstractFrame {

    private Card card;

    private CollectionDatasource<CardRole, UUID> cardRolesDs;
    private CollectionDatasource<ProcRole, UUID> procRolesDs;
    private LookupField createRoleLookup;
    protected List<Component> rolesActions = new ArrayList<Component>();

    private String createRoleCaption;

    public CardRolesFrame(IFrame frame) {
        super(frame);
    }

    public void init() {
        cardRolesDs = getDsContext().get("cardRolesDs");
        Preconditions.checkState(cardRolesDs != null, "Enclosing window must declare declare 'cardRolesDs' datasource");

        procRolesDs = getDsContext().get("procRolesDs");
        createRoleCaption = getMessage("createRoleCaption");
        createRoleLookup = getComponent("createRoleLookup");

        rolesActions.add(createRoleLookup);
        rolesActions.add(getComponent("editRole"));
        rolesActions.add(getComponent("removeRole"));

        Table rolesTable = getComponent("rolesTable");
        TableActionsHelper rolesTH = new TableActionsHelper(this, rolesTable);
        rolesTH.createEditAction(OpenType.DIALOG);
        rolesTH.createRemoveAction(false);
    }

    public void setCard(final Card card) {
        Preconditions.checkArgument(card != null, "Card is null");

        this.card = card;
        for (Component component : rolesActions) {
            component.setEnabled(card.getProc() != null);
        }

        createRoleLookup.addListener(new ValueListener() {
            public void valueChanged(Object source, String property, Object prevValue, final Object value) {
                if ((value == null) || createRoleCaption.equals(value))
                    return;

                CardRole cr = new CardRole();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("procRole", (ProcRole) value);
                params.put("proc", card.getProc());
                final Window.Editor cardRoleEditor = openEditor("wf$CardRole.edit", cr, OpenType.DIALOG, params);
                cardRoleEditor.addListener(new Window.CloseListener() {
                    public void windowClosed(String actionId) {
                        if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                            CardRole cardRole = (CardRole)cardRoleEditor.getItem();
                            cardRole.setCode(cardRole.getProcRole().getCode());
                            cardRolesDs.addItem(cardRole);
                            cardRole.setCard(card);
                        }
                    }
                });

                createRoleLookup.setValue(null);
            }
        });

        cardRolesDs.addListener(new CollectionDsListenerAdapter<CardRole>() {
            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation) {
                initCreateRoleLookup();
            }
        });
    }

    public void procChanged(Proc proc) {
        procRolesDs.refresh(Collections.<String, Object>singletonMap("procId", proc));
        initCreateRoleLookup();

        for (Component component : rolesActions) {
            component.setEnabled(proc != null);
        }
    }

    public void initDefaultActors(Proc proc) {
        if (!cardRolesDs.getItemIds().isEmpty())
            return;

        LoadContext ctx = new LoadContext(DefaultProcActor.class);
        ctx.setQueryString("select a from wf$DefaultProcActor a where a.procRole.proc.id = :procId")
            .addParameter("procId", proc.getId());
        ctx.setView("edit");
        List<DefaultProcActor> dpaList = ServiceLocator.getDataService().loadList(ctx);
        for (DefaultProcActor dpa : dpaList) {
            CardRole cr = new CardRole();
            cr.setProcRole(dpa.getProcRole());
            cr.setCode(dpa.getProcRole().getCode());
            cr.setUser(dpa.getUser());
            cr.setCard(card);
            cr.setNotifyByEmail(dpa.getNotifyByEmail());
            cardRolesDs.addItem(cr);
        }
    }

    private void initCreateRoleLookup() {
        // add ProcRole if it has multiUser == true or not added yet
        List options = new ArrayList();
        for (ProcRole pr : getDsItems(procRolesDs)) {
            if (BooleanUtils.isTrue(pr.getMultiUser()) || !alreadyAdded(pr)) {
                options.add(pr);
            }
        }
        options.add(0, createRoleCaption);
        createRoleLookup.setOptionsList(options);
        createRoleLookup.setNullOption(createRoleCaption);
    }

    private boolean alreadyAdded(ProcRole pr) {
        for (CardRole cr : getDsItems(cardRolesDs)) {
            if (cr.getProcRole().equals(pr))
                return true;
        }
        return false;
    }

    private <T extends Entity<UUID>> List<T> getDsItems(CollectionDatasource<T, UUID> ds) {
        List<T> items = new ArrayList<T>();
        for (UUID id : ds.getItemIds()) {
            items.add(ds.getItem(id));
        }
        return items;
    }

}
