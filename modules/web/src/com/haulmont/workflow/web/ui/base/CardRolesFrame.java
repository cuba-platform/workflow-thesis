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
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.app.LinkColumnHelper;
import com.haulmont.workflow.core.entity.*;
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

        final Table rolesTable = getComponent("rolesTable");
        TableActionsHelper rolesTH = new TableActionsHelper(this, rolesTable);

        final CollectionDatasource rolesTableDs = rolesTable.getDatasource();
        rolesTable.addAction(new AbstractAction("edit") {
            public void actionPerform(Component component) {
                Entity entity = rolesTableDs.getItem();
                Object users = getUsersByProcRole(((CardRole) entity).getProcRole());
                openEditor("wf$CardRole.edit", entity, OpenType.DIALOG,
                        Collections.singletonMap("users", users), rolesTableDs);
            }

            @Override
            public String getCaption() {
                return MessageProvider.getMessage(AppContext.getProperty(AppConfig.MESSAGES_PACK_PROP), "actions.Edit");
            }
        });

        rolesTableDs.addListener(new DsListenerAdapter() {
            @Override
            public void stateChanged(Datasource ds, Datasource.State prevState, Datasource.State state) {
                super.stateChanged(ds, prevState, state);
                if (state.equals(Datasource.State.VALID)) {
                    LinkColumnHelper.initColumn(rolesTable, "procRole.name", new LinkColumnHelper.Handler() {
                        public void onClick(final Entity entity) {
                            Object users = getUsersByProcRole(((CardRole) entity).getProcRole());
                            openEditor("wf$CardRole.edit", entity, OpenType.DIALOG,
                                    Collections.singletonMap("users", users), rolesTableDs);
                        }
                    });
                }
            }
        });

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
                ProcRole procRole = (ProcRole) value;
                Role secRole = procRole.getRole();

                Map<String, Object> params = new HashMap<String, Object>();
                params.put("procRole", procRole);
                params.put("secRole", secRole);
                params.put("proc", card.getProc());
                params.put("users", getUsersByProcRole(procRole));
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

        // if there is a role with AssignToCreator property set up, and this role is not assigned
        // by DefaultProcActor list, assign this role to the current user
        for (UUID procRoleId : procRolesDs.getItemIds()) {
            ProcRole procRole = procRolesDs.getItem(procRoleId);
            if (BooleanUtils.isTrue(procRole.getAssignToCreator())) {
                boolean found = false;
                for (UUID cardRoleId : cardRolesDs.getItemIds()) {
                    CardRole cardRole = cardRolesDs.getItem(cardRoleId);
                    if (procRole.equals(cardRole.getProcRole())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    CardRole cr = new CardRole();
                    cr.setProcRole(procRole);
                    cr.setCode(procRole.getCode());
                    cr.setUser(UserSessionClient.getUserSession().getCurrentOrSubstitutedUser());
                    cr.setCard(card);
                    cr.setNotifyByEmail(false);
                    cardRolesDs.addItem(cr);
                }
            }
        }
    }

    //todo gorbunkov review and refactor next two methods
    //setProcActor must delete other actors and set the user sent in param in case of multiUser role
    public void setProcActor(Proc proc, String roleCode, User user, boolean notifyByEmail) {
        CardRole cardRole = null;
        List<CardRole> cardRoles = card.getRoles();

        //If card role with given code exists, we'll find it
        if (cardRoles != null) {
           for (CardRole cr : cardRoles) {
               if (roleCode.equals(cr.getCode())) {
                   cardRole = cr;
                   break;
               }
           }
        }

        //If card role with given code doesn't exist we'll create a new one
        if (cardRole == null) {
            cardRole = new CardRole();

            ProcRole procRole = null;
            for (ProcRole pr : proc.getRoles()) {
                if (roleCode.equals(pr.getCode())) {
                    procRole = pr;
                }
            }
            if (procRole == null) return;

            cardRole.setProcRole(procRole);
            cardRole.setCode(roleCode);
            cardRole.setCard(card);
            cardRole.setNotifyByEmail(notifyByEmail);
            cardRolesDs.addItem(cardRole);
        }
        cardRole.setUser(user);
    }

    public void addProcActor(Proc proc, String roleCode, User user, boolean notifyByEmail) {
        ProcRole procRole = null;
        for (ProcRole pr : proc.getRoles()) {
            if (roleCode.equals(pr.getCode())) {
                procRole = pr;
            }
        }
        if (procRole == null) return;
        if (BooleanUtils.isTrue(procRole.getMultiUser()) && !procActorExists(roleCode, user)) {
            CardRole cardRole = new CardRole();
            cardRole.setProcRole(procRole);
            cardRole.setCode(roleCode);
            cardRole.setCard(card);
            cardRole.setNotifyByEmail(notifyByEmail);
            cardRole.setUser(user);
            cardRolesDs.addItem(cardRole);
        } else {
            setProcActor(proc, roleCode, user, notifyByEmail);
        }
    }

    private boolean procActorExists(String roleCode, User user) {
        List<CardRole> cardRoles = card.getRoles();
        if (cardRoles != null) {
           for (CardRole cr : cardRoles) {
               if (roleCode.equals(cr.getCode()) && cr.getUser().equals(user)) {
                   return true;
               }
           }
        }
        return false;
    }

    public void deleteAllActors() {
        Collection<UUID> uuidCollection = cardRolesDs.getItemIds();
        for (UUID itemId : uuidCollection) {
            CardRole item = cardRolesDs.getItem(itemId);
            cardRolesDs.removeItem(item);
        }
    }

    private void initCreateRoleLookup() {
        // add ProcRole if it has multiUser == true or hasn't been added yet
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

    private Set<UUID> getUsersByProcRole(ProcRole procRole) {
        if (procRole == null) {
            return null;
        }
        Set<UUID> res = new HashSet<UUID>();
        Collection<UUID> crIds = cardRolesDs.getItemIds();
        for (UUID crId : crIds) {
            CardRole cr = cardRolesDs.getItem(crId);
            if (procRole.equals(cr.getProcRole())) {
                res.add(cr.getUser().getId());
            }
        }
        return res;
    }

}
