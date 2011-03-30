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
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.*;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.gui.data.impl.DatasourceImpl;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.entity.UserRole;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.gui.components.WebActionsField;
import com.haulmont.cuba.web.gui.components.WebButton;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.workflow.core.app.ProcRolePermissionsService;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.global.ProcRolePermissionType;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.themes.BaseTheme;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.List;

public class CardRolesFrame extends AbstractFrame {

    public interface Listener {
        void afterInitDefaultActors(Proc proc);
    }

    private Log log = LogFactory.getLog(CardRolesFrame.class);

    private Set<Listener> listeners = new HashSet<Listener>();

    protected Card card;
    private boolean enabled = true;
    protected CardProc cardProc;

    protected CollectionDatasource<CardRole, UUID> cardRolesDs;
    protected CollectionDatasource<ProcRole, UUID> procRolesDs;
    protected CardProcRolesDatasource tmpCardRolesDs;
    protected LookupField createRoleLookup;
    protected Table rolesTable;
    protected CollectionDatasource rolesTableDs;
    protected List<Component> rolesActions = new ArrayList<Component>();

    protected String createRoleCaption;
    protected ProcRolePermissionsService procRolePermissionsService;
    protected Map<CardRole, CardRoleField> actorActionsFieldsMap = new HashMap<CardRole, CardRoleField>();
    protected List<User> users;
    protected Map<Role, Collection<User>> roleUsersMap = new HashMap<Role, Collection<User>>();
    private String requiredRolesCodesStr;

    public CardRolesFrame(IFrame frame) {
        super(frame);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void init() {
        cardRolesDs = getDsContext().get("cardRolesDs");
        tmpCardRolesDs = getDsContext().get("tmpCardRolesDs");
        tmpCardRolesDs.valid();

        Preconditions.checkState(cardRolesDs != null, "Enclosing window must declare declare 'cardRolesDs' datasource");

        procRolesDs = getDsContext().get("procRolesDs");
        createRoleCaption = getMessage("createRoleCaption");
        createRoleLookup = getComponent("createRoleLookup");

        rolesActions.add(createRoleLookup);
        rolesActions.add(getComponent("removeRole"));

        rolesTable = getComponent("rolesTable");
        TableActionsHelper rolesTH = new TableActionsHelper(this, rolesTable);

        rolesTableDs = rolesTable.getDatasource();

        initRolesTable();

        rolesTH.createRemoveAction(false);

        rolesTable.addAction(new AbstractAction("moveUp") {
            private static final long serialVersionUID = 3616849995887047615L;

            public void actionPerform(Component component) {
                Set selected = rolesTable.getSelected();
                if (selected.isEmpty())
                    return;

                CardRole curCr = (CardRole) selected.iterator().next();
                UUID prevId = tmpCardRolesDs.prevItemId(curCr.getId());
                if (prevId == null)
                    return;

                Integer tmp = curCr.getSortOrder();
                CardRole prevCr = tmpCardRolesDs.getItem(prevId);
                curCr.setSortOrder(prevCr.getSortOrder());
                prevCr.setSortOrder(tmp);

                tmpCardRolesDs.doSort();
            }
        });

        rolesTable.addAction(new AbstractAction("moveDown") {
            private static final long serialVersionUID = 6060776970724140731L;

            public void actionPerform(Component component) {
                Set selected = rolesTable.getSelected();
                if (selected.isEmpty())
                    return;

                CardRole curCr = (CardRole) selected.iterator().next();
                UUID nextId = tmpCardRolesDs.nextItemId(curCr.getId());
                if (nextId == null)
                    return;

                Integer tmp = curCr.getSortOrder();
                CardRole nextCr = tmpCardRolesDs.getItem(nextId);
                curCr.setSortOrder(nextCr.getSortOrder());
                nextCr.setSortOrder(tmp);

                tmpCardRolesDs.doSort();
            }
        });

        procRolePermissionsService = getProcRolePermissionsService();

        tmpCardRolesDs.addListener(new CollectionDsListenerAdapter<CardRole>() {
            private static final long serialVersionUID = 1205336750221624070L;

            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation) {
                initCreateRoleLookup();
                
            }

            Action editAction = rolesTable.getAction("edit");
            Action removeAction = rolesTable.getAction("remove");

            @Override
            public void itemChanged(Datasource<CardRole> ds, CardRole prevItem, CardRole item) {
                if (item == null) return;
//                editAction.setEnabled(procRolePermissionsService.isPermitted(item, getState(), ProcRolePermissionType.MODIFY));
                removeAction.setEnabled(procRolePermissionsService.isPermitted(item, getState(), ProcRolePermissionType.REMOVE));
            }
        });

        rolesTH.addListener(new ListActionsHelper.Listener() {
            public void entityCreated(Entity entity) {

            }

            public void entityEdited(Entity entity) {

            }

            public void entityRemoved(Set<Entity> entity) {
                if (entity != null) {
                    for(Object item: entity) {
                        actorActionsFieldsMap.remove(item);
                        CardRole cardRole = (CardRole) item;
                        if (cardRole.getUser() != null) {
                            refreshFieldsWithRole(cardRole);
                        }
                    }
                }
            }
        });

        createRoleLookup.addListener(new ValueListener() {
            private static final long serialVersionUID = -5741123364065365382L;

            public void valueChanged(Object source, String property, Object prevValue, final Object value) {
                if ((value == null) || createRoleCaption.equals(value))
                    return;

                final ProcRole procRole = (ProcRole) value;
                CardRole cardRole = new CardRole();
                cardRole.setProcRole(procRole);
                cardRole.setCode(procRole.getCode());
                cardRole.setNotifyByEmail(true);
                cardRole.setNotifyByCardInfo(true);
                cardRole.setCard(card);
                assignNextSortOrder(cardRole);
                tmpCardRolesDs.addItem(cardRole);

                createRoleLookup.setValue(null);
            }
        });
    }

    private void initRolesTable() {
        final ProcRolePermissionsService procRolePermissionsService = ServiceLocator.lookup(ProcRolePermissionsService.NAME);
        final com.vaadin.ui.Table vRolesTable = (com.vaadin.ui.Table) WebComponentsHelper.unwrap(rolesTable);
        MetaPropertyPath userProperty = rolesTableDs.getMetaClass().getPropertyEx("user");
        vRolesTable.addGeneratedColumn(userProperty, new com.vaadin.ui.Table.ColumnGenerator() {
            private static final long serialVersionUID = -4911659211968894944L;

            public com.vaadin.ui.Component generateCell(com.vaadin.ui.Table source, Object itemId, Object columnId) {
                CardRole cardRole = tmpCardRolesDs.getItem((UUID) itemId);

                CardRoleField cardRoleField = actorActionsFieldsMap.get(cardRole);
                if (cardRoleField != null) {
                    cardRoleField.setValue(cardRole.getUser());
                    return cardRoleField;
                }

                cardRoleField = new CardRoleField();
                actorActionsFieldsMap.put(cardRole, cardRoleField.initField(cardRole, cardRole.getUser()));
                refreshFieldsWithRole(cardRole);

                return cardRoleField;
            }
        });

        initRolesTableBooleanColumn("notifyByEmail", procRolePermissionsService, vRolesTable);
        initRolesTableBooleanColumn("notifyByCardInfo", procRolePermissionsService, vRolesTable);
    }

    private com.haulmont.cuba.gui.components.Button createAddGroupButton(final CardRole cardRole) {
        final CardRolesFrame crf = this;
        com.haulmont.cuba.gui.components.Button addUserGroupButton = new WebButton();
        addUserGroupButton.setIcon("select/img/user-group-button.png");
        addUserGroupButton.setStyleName(BaseTheme.BUTTON_LINK);
        com.vaadin.ui.Button vAddUserGroupButton = (com.vaadin.ui.Button)WebComponentsHelper.unwrap(addUserGroupButton);
        final Class userGroupAddClass = ScriptingProvider.loadClass("workflow.client.web.ui.usergroup.UserGroupAdd");
        vAddUserGroupButton.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = -3820323886456571938L;

            public void buttonClick(Button.ClickEvent event) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("secRole", cardRole.getProcRole().getRole());
                App.getInstance().getWindowManager().getDialogParams().setWidth(680);
                final Window window = crf.openWindow("wf$UserGroup.add", WindowManager.OpenType.DIALOG, params);
                window.addListener(new Window.CloseListener() {
                    private static final long serialVersionUID = -4182051025753394757L;

                    public void windowClosed(String actionId) {
                        if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                            Set<User> selectedUsers = null;
                            Set<User> validUsers = new HashSet<User>();
                            Set<User> invalidUsers = new HashSet<User>();
                            try {
                                selectedUsers = (Set<User>) userGroupAddClass.getMethod("getSelectedUsers").invoke(window);
                            } catch (Exception e) {
                                throw new IllegalStateException("Can't invoke UserGroupAdd.getCardRoles(): " + e);
                            }

                            Role secRole = cardRole.getProcRole().getRole();
                            for (User user : selectedUsers) {
                                if (!procActorExists(cardRole.getProcRole(), user)
                                        || ((cardRole.getUser() != null) && cardRole.getUser().equals(user))) {
                                    validUsers.add(user);
                                    if ((secRole != null) && !(userInRole(user, secRole))) {
                                        invalidUsers.add(user);
                                        validUsers.remove(user);
                                    }
                                }
                            }
                            User oneOfValidUsers = null;
                            if (!validUsers.isEmpty()) {
                                oneOfValidUsers = validUsers.iterator().next();
                            }
                            if (oneOfValidUsers != null) {
                                cardRole.setUser(oneOfValidUsers);
                                actorActionsFieldsMap.get(cardRole).setValue(oneOfValidUsers);
                                validUsers.remove(oneOfValidUsers);
                                tmpCardRolesDs.updateItem(cardRole);
                            }
                            List<CardRole> cardRolesToAdd = createCardRoles(validUsers, cardRole);

                            if (!invalidUsers.isEmpty()) {
                                String usersList = "";
                                for (User user : invalidUsers) {
                                    usersList += user.getName() + ", ";
                                }
                                usersList = usersList.substring(0, usersList.length() - 2);
                                String invalidUsersMessage;
                                if (invalidUsers.size() == 1)
                                    invalidUsersMessage = MessageProvider.formatMessage(getClass(), "invalidUser.message", usersList, cardRole.getProcRole().getName());
                                else
                                    invalidUsersMessage = MessageProvider.formatMessage(getClass(), "invalidUsers.message", usersList, cardRole.getProcRole().getName());

                                showNotification("", invalidUsersMessage, IFrame.NotificationType.WARNING);
                            }

                        }
                    }
                });
            }
        });

        vAddUserGroupButton.setEnabled(cardRole.getProcRole().getMultiUser()
         && procRolePermissionsService.isPermitted(card, cardRole.getProcRole(), getState(), ProcRolePermissionType.ADD));
        return addUserGroupButton;

    }

    //fills users datasource with users with necessary security role (if set)
    //and not added to process actors as member of current procRole
    protected void fillUsersOptionsDs(CardRole cardRole, CollectionDatasource usersDs) {
        if (usersDs == null)
            throw new RuntimeException("usersDs cannot be null");
        Role secRole = cardRole.getProcRole().getRole();
        Set<User> addedUsersOfProcRole = getUsersByProcRole(cardRole.getProcRole());
        if (cardRole.getUser() != null) {
            addedUsersOfProcRole.remove(cardRole.getUser().getId());
        }

        Collection<User> dsItems;
        if (secRole == null) {
            dsItems = ListUtils.removeAll(getUsers(), addedUsersOfProcRole);
        } else {
            dsItems = ListUtils.removeAll(getRoleUsers(secRole), addedUsersOfProcRole);
        }

        for (User u : dsItems) {
            usersDs.addItem(u);
        }
    }

    /**
     * Find all users, who have a given role
     * @param secRole
     * @return collection of users
     */
    private Collection<User> getRoleUsers(Role secRole) {
        Collection<User> roleUsers = roleUsersMap.get(secRole);

        if (roleUsers == null) {
            LoadContext ctx = new LoadContext(User.class).setView(View.MINIMAL);
            LoadContext.Query query = ctx.setQueryString("select u from sec$User u join u.userRoles ur where ur.role.id = :role order by u.name");
            query.addParameter("role", secRole);
            List<User> loadedUsers = ServiceLocator.getDataService().loadList(ctx);
            if (loadedUsers == null) {
                roleUsers = new ArrayList<User>();
            }
            roleUsers = new ArrayList<User>(loadedUsers);
            roleUsersMap.put(secRole, roleUsers);
        }

        return roleUsers;
    }

    //we'll read users list only once and will use this list while filling users option datasources
    protected List<User> getUsers() {
        if (users == null) {
            LoadContext ctx = new LoadContext(User.class).setView(View.MINIMAL);
            ctx.setQueryString("select u from sec$User u order by u.name");
            List<User> loadedUsers = ServiceLocator.getDataService().loadList(ctx);
            users = new ArrayList<User>(loadedUsers);
        }
        return users;
    }

    private CollectionDatasource createUserOptionsDs(CardRole cardRole) {
        MetaClass metaClass = MetadataProvider.getSession().getClass(User.class);
        CollectionDatasource usersDs = new DsBuilder(getDsContext())
                .setMetaClass(metaClass)
                .setId("usersDs")
                .setViewName("_minimal")
                .buildCollectionDatasource();
        ((DatasourceImpl)usersDs).valid();
        fillUsersOptionsDs(cardRole, usersDs);
        return usersDs;
    }

    private boolean userInRole(User user, Role role) {
       if (user.getUserRoles() == null) return false;
       for (UserRole userRole : user.getUserRoles()) {
           if (userRole.getRole().equals(role)) return true;
       }
       return false;
    }

    private List<CardRole> createCardRoles(Set<User> users, CardRole cardRole) {
        List<CardRole> cardRoles = new ArrayList<CardRole>();
        for (User user : users) {
            CardRole cr = new CardRole();
            cr.setUser(user);
            cr.setProcRole(cardRole.getProcRole());
            cr.setCode(cardRole.getCode());
            cr.setNotifyByEmail(true);
            cr.setNotifyByCardInfo(true);
            cr.setCard(card);
            cardRoles.add(cr);
            assignNextSortOrder(cr);
            tmpCardRolesDs.addItem(cr);
        }
        return cardRoles;
    }

    private void initRolesTableBooleanColumn(final String propertyName,
                                             final ProcRolePermissionsService procRolePermissionsService,
                                             final com.vaadin.ui.Table vRolesTable) {
        MetaPropertyPath propertyPath = rolesTableDs.getMetaClass().getPropertyEx(propertyName);
        vRolesTable.removeGeneratedColumn(propertyPath);
        vRolesTable.addGeneratedColumn(propertyPath, new com.vaadin.ui.Table.ColumnGenerator() {
            private static final long serialVersionUID = 5205263712948328595L;

            public com.vaadin.ui.Component generateCell(com.vaadin.ui.Table source, Object itemId, Object columnId) {
                final CardRole cardRole = tmpCardRolesDs.getItem((UUID) itemId);
                Property property = source.getItem(itemId).getItemProperty(columnId);
                boolean value = (Boolean) property.getValue();
                com.vaadin.ui.CheckBox checkBox = new com.vaadin.ui.CheckBox();
                checkBox.setValue(value);
                boolean enabled = procRolePermissionsService.isPermitted(cardRole, getState(), ProcRolePermissionType.MODIFY);
                checkBox.setEnabled(enabled);
                checkBox.addListener(new Property.ValueChangeListener() {
                    private static final long serialVersionUID = -116654070578891424L;

                    public void valueChange(Property.ValueChangeEvent event) {
                        Property property = event.getProperty();
                        Boolean value = (Boolean) property.getValue();
                        cardRole.setValue(propertyName, value);
                    }
                });
                checkBox.setReadOnly(vRolesTable.isReadOnly());
                return checkBox;
            }
        });
    }

    public void setCard(final Card card) {
        Preconditions.checkArgument(card != null, "Card is null");

        this.card = card;
        if (PersistenceHelper.isNew(card)) {
            if (card.getRoles() != null) {
                ArrayList<CardRole> list = (ArrayList<CardRole>) card.getRoles();
                tmpCardRolesDs.fill = true;
                if (list != null)
                    for (CardRole cardRole : list) {
                        if (!tmpCardRolesDs.containsItem(cardRole.getUuid())) {
                            assignNextSortOrder(cardRole);
                            tmpCardRolesDs.addItem(cardRole);
                        }
                    }
                tmpCardRolesDs.fill = false;
            }
            if (card.getProc() != null) {
                List<ProcRole> roleList = card.getProc().getRoles();
                if (roleList != null) {
                    for (ProcRole procRole : roleList) {
                        if (!procRolesDs.containsItem(procRole.getUuid()) && (procRole.getInvisible() == null || !procRole.getInvisible())) {
                            procRolesDs.addItem(procRole);
                        }
                    }
                }
                initCreateRoleLookup();
            }
        }
//        for (Component component : rolesActions) {
//            component.setEnabled(card.getProc() != null);
//        }
    }

    public void procChanged(Proc proc) {
        procRolesDs.refresh(Collections.<String, Object>singletonMap("procId", proc));
        initCreateRoleLookup();
        tmpCardRolesDs.fillForProc(proc);

        for (Component component : rolesActions) {
            component.setEnabled(proc != null && isEnabled());
        }
    }

    public void initDefaultActors(Proc proc) {
        if (!tmpCardRolesDs.getItemIds().isEmpty())
            return;

        LoadContext ctx = new LoadContext(DefaultProcActor.class);
        ctx.setQueryString("select a from wf$DefaultProcActor a where a.procRole.proc.id = :procId and a.user.deleteTs is null")
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
            assignNextSortOrder(cr);
            tmpCardRolesDs.addItem(cr);
        }

        initAssignedToCreatorActors();

        for (Listener listener : listeners) {
            listener.afterInitDefaultActors(proc);
        }

    }

    public void initAssignedToCreatorActors() {
        // if there is a role with AssignToCreator property set up, and this role is not assigned,
        // assign this role to the current user
        WfService wfService = ServiceLocator.lookup(WfService.NAME);
        for (UUID procRoleId : procRolesDs.getItemIds()) {
            ProcRole procRole = procRolesDs.getItem(procRoleId);
            if (BooleanUtils.isTrue(procRole.getAssignToCreator()) && wfService.isCurrentUserContainsRole(procRole.getRole())) {
                boolean found = false;
                for (UUID cardRoleId : tmpCardRolesDs.getItemIds()) {
                    CardRole cardRole = tmpCardRolesDs.getItem(cardRoleId);
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
                    cr.setNotifyByEmail(true);
                    assignNextSortOrder(cr);
                    tmpCardRolesDs.addItem(cr);
                }
            }
        }
    }

    private void assignNextSortOrder(CardRole cr) {
        UUID lastId = tmpCardRolesDs.lastItemId();
        if (lastId == null)
            cr.setSortOrder(1);
        else {
            CardRole lastItem = tmpCardRolesDs.getItem(lastId);
            if (lastItem.getSortOrder() == null)
                cr.setSortOrder(1);
            else
                cr.setSortOrder(tmpCardRolesDs.getItem(lastId).getSortOrder() + 1);
        }
    }

    //todo gorbunkov review and refactor next two methods
    //setProcActor must delete other actors and set the user sent in param in case of multiUser role
    public void setProcActor(Proc proc, ProcRole procRole, User user, boolean notifyByEmail) {
        if (proc == null)
            throw new IllegalArgumentException("Proc is null, check all required processes are deployed");

        CardRole cardRole = null;
        List<CardRole> cardRoles = card.getRoles();

        //If card role assiciated with procRole exists, we'll find it
        if (cardRoles != null) {
            for (CardRole cr : cardRoles) {
                if (procRole.equals(cr.getProcRole())) {
                    cardRole = cr;
                    break;
                }
            }
        }

        //If card role with given code doesn't exist we'll create a new one
        if (cardRole == null) {
            cardRole = new CardRole();

            if (proc.getRoles() == null) {
                proc = getDsContext().getDataService().reload(proc, "edit");
            }

            cardRole.setProcRole(procRole);
            cardRole.setCode(procRole.getCode());
            cardRole.setCard(card);
            cardRole.setNotifyByEmail(notifyByEmail);
            assignNextSortOrder(cardRole);
            cardRole.setUser(user);
            tmpCardRolesDs.addItem(cardRole);
        } else {
            cardRole.setUser(user);
        }
    }

    public void addProcActor(Proc proc, String roleCode, User user, boolean notifyByEmail) {
        ProcRole procRole = null;

        if (proc.getRoles() == null) {
            proc = getDsContext().getDataService().reload(proc, "edit");
        }

        for (ProcRole pr : proc.getRoles()) {
            if (roleCode.equals(pr.getCode())) {
                procRole = pr;
            }
        }
        if (procRole == null)
            return;

        if (BooleanUtils.isTrue(procRole.getMultiUser()) && !procActorExists(procRole, user)) {
            CardRole cardRole = new CardRole();
            cardRole.setProcRole(procRole);
            cardRole.setCode(roleCode);
            cardRole.setCard(card);
            cardRole.setNotifyByEmail(notifyByEmail);
            cardRole.setUser(user);
            assignNextSortOrder(cardRole);
            tmpCardRolesDs.addItem(cardRole);
        } else {
            setProcActor(proc, procRole, user, notifyByEmail);
        }
    }

    public void removeProcActor(String roleCode, User user) {
        List<CardRole> cardRoles = getDsItems(tmpCardRolesDs);
        for (CardRole cardRole : cardRoles) {
            if ((cardRole.getUser() != null) && (cardRole.getUser().equals(user)) && (cardRole.getCode().equals(roleCode))) {
                tmpCardRolesDs.removeItem(cardRole);
                return;
            }
        }
    }

    private boolean procActorExists(ProcRole procRole, User user) {
        List<CardRole> cardRoles = getDsItems(tmpCardRolesDs);
        if (cardRoles != null) {
            for (CardRole cr : cardRoles) {
                if (procRole.equals(cr.getProcRole()) && (cr.getUser() != null && cr.getUser().equals(user))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void deleteAllActors() {
        Collection<UUID> uuidCollection = tmpCardRolesDs.getItemIds();
        for (UUID itemId : new ArrayList<UUID>(uuidCollection)) {
            CardRole item = tmpCardRolesDs.getItem(itemId);
            tmpCardRolesDs.removeItem(item);
        }
    }

    protected void initCreateRoleLookup() {
        // add ProcRole if it has multiUser == true or hasn't been added yet
        List options = new ArrayList();
        for (ProcRole pr : getDsItems(procRolesDs)) {
            if ((BooleanUtils.isTrue(pr.getMultiUser()) || !alreadyAdded(pr))
                    && procRolePermissionsService.isPermitted(card, pr, getState(), ProcRolePermissionType.ADD)) {
                options.add(pr);
            }
        }
        options.add(0, createRoleCaption);
        createRoleLookup.setOptionsList(options);
        createRoleLookup.setNullOption(createRoleCaption);
    }

    protected boolean alreadyAdded(ProcRole pr) {
        for (CardRole cr : getDsItems(tmpCardRolesDs)) {
            if (cr.getProcRole().equals(pr))
                return true;
        }
        return false;
    }

    protected <T extends Entity<UUID>> List<T> getDsItems(CollectionDatasource<T, UUID> ds) {
        List<T> items = new ArrayList<T>();
        for (UUID id : ds.getItemIds()) {
            items.add(ds.getItem(id));
        }
        return items;
    }

    private Set<User> getUsersByProcRole(ProcRole procRole) {
        if (procRole == null) {
            return null;
        }
        Set<User> res = new HashSet<User>();
        Collection<UUID> crIds = tmpCardRolesDs.getItemIds();
        for (UUID crId : crIds) {
            CardRole cr = tmpCardRolesDs.getItem(crId);
            if (procRole.equals(cr.getProcRole()) && cr.getUser() != null) {
                res.add(cr.getUser());
            }
        }
        return res;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (rolesTable.getActions() != null) {
            for (Action action : rolesTable.getActions()) {
                action.setEnabled(enabled);
            }
        }
        for (Component action : rolesActions) {
            action.setEnabled(enabled);
        }
    }

    protected ProcRolePermissionsService getProcRolePermissionsService() {
        return ServiceLocator.lookup(ProcRolePermissionsService.NAME);
    }

    public void setCardProc(CardProc cardProc) {
        this.cardProc = cardProc;
    }

    private Proc getProc() {
        return ((cardProc == null) || (cardProc.getProc() == null)) ? card.getProc() : cardProc.getProc();
    }

    private String getState() {
        return (cardProc == null) ? card.getState() : cardProc.getState();
    }

    public void setEditable(boolean editable) {
        for (Action action: rolesTable.getActions()) {
            action.setEnabled(editable);
        }
        WebComponentsHelper.unwrap(rolesTable).setReadOnly(!editable);
        createRoleLookup.setEditable(editable);
        for(CardRoleField cardRoleField: actorActionsFieldsMap.values()) {
            cardRoleField.setEditable(editable);
        }
    }
    
    private void refreshFieldsWithRole(CardRole cardRole) {
        for (CardRole cr : actorActionsFieldsMap.keySet()) {
            if (cr.getCode().equals(cardRole.getCode()) && !cr.equals(cardRole)) {
                CardRoleField cardRoleField = actorActionsFieldsMap.get(cr);
                cardRoleField.initField(cr, cardRoleField.getValue());
            }
        }
    }

    public void fillMissingRoles() {
        Set<String> requiredRolesCodes = getRequiredRolesCodes(cardRolesDs.getItemIds().size() == 0);
        for (Object itemId : cardRolesDs.getItemIds()) {
            CardRole cardRole = (CardRole) cardRolesDs.getItem((UUID)itemId);
            requiredRolesCodes.remove(cardRole.getCode());
        }

        Proc proc = card.getProc();
        proc = cardRolesDs.getDataService().reload(proc, "edit");

        for (String roleCode : requiredRolesCodes) {
            if (!roleCode.contains("|"))
                addProcActor(proc, roleCode, null, true);
        }
    }

    public Set<String> getEmptyRolesNames() {
        Set<String> emptyRolesNames = new HashSet<String>();
        Map<String, String> procRolesNames = new HashMap<String, String>();
        List<ProcRole> procRoles = card.getProc().getRoles();
        if (procRoles == null) {
            LoadContext ctx = new LoadContext(ProcRole.class);
            LoadContext.Query query = ctx.setQueryString("select pr from wf$ProcRole pr where pr.proc.id = :proc");
            query.addParameter("proc", card.getProc());
            procRoles = ServiceLocator.getDataService().loadList(ctx);
        }
        for (ProcRole procRole : procRoles) {
            procRolesNames.put(procRole.getCode(), procRole.getName());
        }

        //if we removed required role from datasource
        Set<String> emptyRequiredRolesCodes = getRequiredRolesCodes(false);

        Set<String> requiredRolesChoiceCodes = new HashSet<String>();
        for (String requiredRoleCode : emptyRequiredRolesCodes) {
            if (requiredRoleCode.contains("|"))
                requiredRolesChoiceCodes.add(requiredRoleCode);
        }

        for (Object itemId : cardRolesDs.getItemIds()) {
            CardRole cardRole = (CardRole) cardRolesDs.getItem((UUID)itemId);
            if (cardRole.getUser() == null) {
                emptyRolesNames.add(procRolesNames.get(cardRole.getCode()));
            }

            if (!requiredRolesChoiceCodes.isEmpty()) {
                String choiceRole = null;
                for (String requiredRolesChoiceCode : requiredRolesChoiceCodes) {
                    String[] roles = requiredRolesChoiceCode.split("\\|");
                    if (Arrays.binarySearch(roles, cardRole.getCode()) >= 0) {
                        choiceRole = requiredRolesChoiceCode;
                        break;
                    }
                }

                if (choiceRole != null) {
                    requiredRolesChoiceCodes.remove(choiceRole);
                    emptyRequiredRolesCodes.remove(choiceRole);
                }
            }

            emptyRequiredRolesCodes.remove(cardRole.getCode());
        }

        for (String roleCode : emptyRequiredRolesCodes) {
            if (roleCode.contains("|")) {
                String formattingCode = "";
                String orStr = " " + MessageProvider.getMessage(TransitionForm.class, "actorNotDefined.or") + " ";
                String[] roles = roleCode.split("\\|");
                for (String role : roles) {
                    formattingCode += procRolesNames.get(role) + orStr;
                }

                if (formattingCode.endsWith(orStr))
                    formattingCode = formattingCode.substring(0, formattingCode.lastIndexOf(orStr));

                emptyRolesNames.add(formattingCode);
            } else {
                emptyRolesNames.add(procRolesNames.get(roleCode));
            }
        }

        return emptyRolesNames;
    }

    private Set<String> getRequiredRolesCodes(boolean isAll) {
        if (requiredRolesCodesStr != null) {
            String[] s = requiredRolesCodesStr.split(isAll ? "\\s*[,|]\\s*" : "\\s*,\\s*");
            return new LinkedHashSet<String>(Arrays.asList(s));
        }
        return Collections.emptySet();
    }

    public void setRequiredRolesCodesStr(String requiredRolesCodesStr) {
        this.requiredRolesCodesStr = requiredRolesCodesStr;
    }

    public static class CardProcRolesDatasource extends CollectionDatasourceImpl<CardRole, UUID> {
        private static final long serialVersionUID = 2186196099900027571L;
        private CollectionDatasource<CardRole, UUID> cardRolesDs = getDsContext().get("cardRolesDs");
        public boolean fill;

        public CardProcRolesDatasource(DsContext context, DataService dataservice, String id, MetaClass metaClass, String viewName) {
            super(context, dataservice, id, metaClass, viewName);
            cardRolesDs = getDsContext().get("cardRolesDs");
        }

        @Override
        public void addItem(CardRole item) throws UnsupportedOperationException {

            super.addItem(item);
            if (!fill)
                cardRolesDs.addItem(item);
        }

        @Override
        public void removeItem(CardRole item) throws UnsupportedOperationException {
            super.removeItem(item);
            if (!fill)
                cardRolesDs.removeItem(item);
        }

        @Override
        protected Comparator<CardRole> createEntityComparator() {
            return new Comparator<CardRole>() {
                public int compare(CardRole cr1, CardRole cr2) {
                    int s1 = cr1.getSortOrder() == null ? 0 : cr1.getSortOrder();
                    int s2 = cr2.getSortOrder() == null ? 0 : cr2.getSortOrder();
                    return s1 - s2;
                }
            };
        }

        @Override
        public void doSort() {
            super.doSort();
            forceCollectionChanged(CollectionDatasourceListener.Operation.REFRESH);
        }

        public void fillForProc(Proc proc) {
            fill = true;
            try {
                for (UUID id : new ArrayList<UUID>(getItemIds())) {
                    removeItem(getItem(id));
                }
                if (proc != null) {
                    for (UUID id : cardRolesDs.getItemIds()) {
                        CardRole cardRole = cardRolesDs.getItem(id);
                        if (!cardRole.getProcRole().getInvisible() && cardRole.getProcRole().getProc().equals(proc)) {
                            addItem(cardRole);
                        }
                    }
                }
                doSort();
                setModified(false);
            } finally {
                fill = false;
            }
        }

        @Override
        public void setSuspended(boolean suspended) {
            super.setSuspended(false);
        }
    }

    private class CardRoleField extends CustomComponent {

        private static final long serialVersionUID = 20978973521879151L;
        private WebActionsField actionsField;
        private com.haulmont.cuba.gui.components.Button addGroupButton;

        private CardRoleField() {

        }

        CardRoleField initField(final CardRole cardRole, Object value) {
            final CollectionDatasource usersDs = createUserOptionsDs(cardRole);

            actionsField = new WebActionsField();
            actionsField.enableButton(ActionsField.DROPDOWN, true);
            actionsField.setOptionsDatasource(usersDs);
            actionsField.setValue(value);
            actionsField.setWidth("100%");
            addGroupButton = null;
//            actionsField.setHeight("25px");
            LookupField usersLookup = actionsField.getLookupField();
            com.vaadin.ui.Select usersSelect = (com.vaadin.ui.Select) WebComponentsHelper.unwrap(usersLookup);
            usersSelect.addListener(new Property.ValueChangeListener() {
                private static final long serialVersionUID = 8930855523874560855L;

                public void valueChange(Property.ValueChangeEvent event) {
                    Property eventProperty = event.getProperty();
                    User selectedUser = (User) usersDs.getItem(eventProperty.getValue());
                    cardRole.setUser(selectedUser);
                    refreshFieldsWithRole(cardRole);
                }
            });

            final com.vaadin.ui.Table vRolesTable = (com.vaadin.ui.Table) WebComponentsHelper.unwrap(rolesTable);
            boolean enabled = procRolePermissionsService.isPermitted(cardRole, getState(), ProcRolePermissionType.MODIFY);
            usersSelect.setReadOnly(vRolesTable.isReadOnly() || !enabled);

            if (cardRole.getProcRole().getMultiUser() && !vRolesTable.isReadOnly()) {
                addGroupButton = createAddGroupButton(cardRole);
                actionsField.addButton(addGroupButton);
            }

            WebComponentsHelper.unwrap(actionsField).setReadOnly(vRolesTable.isReadOnly());

            setCompositionRoot(WebComponentsHelper.unwrap(actionsField));

            return this;
        }

        Object getValue() {
            return actionsField.getValue();
        }

        void setValue(Object value) {
            actionsField.setValue(value);
        }

        void setEditable(boolean editable) {
            actionsField.setEditable(editable);
            if (addGroupButton != null) {
                addGroupButton.setVisible(editable);
            }
        }
    }

}
