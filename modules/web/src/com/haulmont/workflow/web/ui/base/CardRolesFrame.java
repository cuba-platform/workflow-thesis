/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.web.ui.base;

import com.google.common.base.Preconditions;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.ServiceLocator;
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
import com.haulmont.cuba.web.gui.components.WebLookupField;
import com.haulmont.workflow.core.app.ProcRolePermissionsService;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.global.ProcRolePermissionType;
import com.haulmont.workflow.core.global.TimeUnit;
import com.vaadin.data.Property;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.themes.BaseTheme;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

/**
 * @author krivopustov
 * @version $Id$
 */
public class CardRolesFrame extends AbstractFrame {
    protected Map<ProcRole, CollectionDatasource> procRoleUsers = new HashMap<>();

    public interface Listener {
        void afterInitDefaultActors(Proc proc);
    }

    private Set<Listener> listeners = new HashSet<>();

    protected Card card;
    private boolean enabled = true;
    protected CardProc cardProc;

    protected CollectionDatasource<CardRole, UUID> cardRolesDs;
    protected CollectionDatasource<ProcRole, UUID> procRolesDs;
    protected CardProcRolesDatasource tmpCardRolesDs;
    protected LookupField createRoleLookup;
    protected Table rolesTable;
    protected CheckBox showSortOrderCheckBox;
    protected CollectionDatasource rolesTableDs;

    protected List<Component> rolesActions = new ArrayList<>();

    protected String createRoleCaption;
    protected ProcRolePermissionsService procRolePermissionsService;
    protected Map<CardRole, CardRoleField> actorActionsFieldsMap = new HashMap<>();
    protected List<User> users;
    protected Map<Role, Collection<User>> roleUsersMap = new HashMap<>();
    private String requiredRolesCodesStr;
    private List deletedEmptyRoleCodes;
    protected boolean editable = true;
    private static final String[] extendedModeColumns = {"sortOrder", "duration", "timeUnit"};

    @Inject
    WebButton moveDown;
    @Inject
    WebButton moveUp;
    
    @Inject
    private Metadata metadata;
    
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

        showSortOrderCheckBox = getComponent("showSortOrder");

        initRolesTable();

        rolesTH.createRemoveAction(false);

        initMoveButtons();
        final com.vaadin.ui.Table vRolesTable = (com.vaadin.ui.Table) WebComponentsHelper.unwrap(rolesTable);
        final MetaPropertyPath mpp = rolesTable.getDatasource().getMetaClass().getPropertyPath("sortOrder");

        showSortOrderCheckBox.setValue(true);
        showSortOrderCheckBox.setVisible(false);

        rolesTable.addColumnCollapsedListener(new Table.ColumnCollapseListener() {
            @Override
            public void columnCollapsed(Table.Column collapsedColumn, boolean collapsed) {
                if (collapsedColumn != null) {
                    MetaPropertyPath m = (MetaPropertyPath) collapsedColumn.getId();
                    if ("sortOrder".equals(m.getMetaProperty().getName())) {
                        showSortOrderCheckBox.setValue(!collapsed);
                    }
                }
            }
        });

        showSortOrderCheckBox.addListener(new ValueListener() {
            @Override
            public void valueChanged(Object source, String property, Object prevValue, Object value) {
                for (String columnName : extendedModeColumns) {
                    MetaPropertyPath mpp = rolesTable.getDatasource().getMetaClass().getPropertyPath(columnName);
                    if (mpp != null) {
                        vRolesTable.setColumnCollapsed(mpp, !(Boolean) value);
                    }
                }
            }
        });

        procRolePermissionsService = getProcRolePermissionsService();

        tmpCardRolesDs.addListener(new CollectionDsListenerAdapter<CardRole>() {
            private static final long serialVersionUID = 1205336750221624070L;

            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation, List<CardRole> items) {
                initCreateRoleLookup();
                if (operation.equals(Operation.ADD)) tmpCardRolesDs.doSort();
            }

            Action editAction = rolesTable.getAction("edit");
            Action removeAction = rolesTable.getAction("remove");

            @Override
            public void itemChanged(Datasource<CardRole> ds, CardRole prevItem, CardRole item) {
                if (item == null) return;
//                editAction.setEnabled(procRolePermissionsService.isPermitted(item, getState(), ProcRolePermissionType.MODIFY));
                removeAction.setVisible(procRolePermissionsService.isPermitted(item, getState(), ProcRolePermissionType.REMOVE));
            }
        });

        rolesTH.addListener(new ListActionsHelper.Listener() {
            public void entityCreated(Entity entity) {

            }

            public void entityEdited(Entity entity) {

            }

            public void entityRemoved(Set<Entity> entity) {
                if (entity != null) {
                    for (Object item : entity) {
                        actorActionsFieldsMap.remove(item);
                        CardRole cardRole = (CardRole) item;
                        if (cardRole.getUser() != null) {
//                            refreshFieldsWithRole(cardRole);
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
                CardRole cardRole = metadata.create(CardRole.class);
                cardRole.setProcRole(procRole);
                cardRole.setCode(procRole.getCode());
                cardRole.setNotifyByEmail(true);
                cardRole.setNotifyByCardInfo(true);
                cardRole.setCard(card);
                assignNextSortOrder(cardRole);
                assignDurationAndTimeUnit(cardRole);
                tmpCardRolesDs.addItem(cardRole);

                createRoleLookup.setValue(null);
            }
        });
    }

    private CardRole getCardRoleDependsOnSortOrder(CardRole curCr, boolean forward) {
        UUID id;
        if (forward)
            id = tmpCardRolesDs.nextItemId(curCr.getId());
        else
            id = tmpCardRolesDs.prevItemId(curCr.getId());

        if (id == null) return null;
        CardRole cardRole = tmpCardRolesDs.getItem(id);
        if (!cardRole.getProcRole().equals(curCr.getProcRole())) return null;
        if (!cardRole.getSortOrder().equals(curCr.getSortOrder())) {
            return cardRole;
        } else {
            return getCardRoleDependsOnSortOrder(cardRole, forward);
        }
    }

    private void initRolesTable() {
        final ProcRolePermissionsService procRolePermissionsService = ServiceLocator.lookup(ProcRolePermissionsService.NAME);
        final com.vaadin.ui.Table vRolesTable = (com.vaadin.ui.Table) WebComponentsHelper.unwrap(rolesTable);
        vRolesTable.setPageLength(5);
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
//                refreshFieldsWithRole(cardRole);

                return cardRoleField;
            }
        });

        addSortOrderColumn(vRolesTable);

        initDurationColumns();
        initRolesTableBooleanColumn("notifyByEmail", procRolePermissionsService, vRolesTable);
        initRolesTableBooleanColumn("notifyByCardInfo", procRolePermissionsService, vRolesTable);
    }

    private void initDurationColumns() {
        tmpCardRolesDs.addListener(new CollectionDsListenerAdapter<CardRole>() {
            @Override
            public void valueChanged(CardRole source, String property, Object prevValue, Object value) {
                if ("duration".equals(property)) {
                    for (UUID uuid : tmpCardRolesDs.getItemIds()) {
                        CardRole cr = tmpCardRolesDs.getItem(uuid);
                        if (cr.getSortOrder() != null && cr.getSortOrder().equals(source.getSortOrder())
                                && cr.getProcRole() != null && cr.getProcRole().equals(source.getProcRole())) {
                            cr.setDuration(source.getDuration());
                        }
                    }
                } else if ("timeUnit".equals(property)) {
                    for (UUID uuid : tmpCardRolesDs.getItemIds()) {
                        CardRole cr = tmpCardRolesDs.getItem(uuid);
                        if (cr.getSortOrder() != null && cr.getSortOrder().equals(source.getSortOrder())
                                && cr.getProcRole() != null && cr.getProcRole().equals(source.getProcRole())) {
                            TimeUnit timeUnit = source.getTimeUnit();
                            if (timeUnit != null)
                                cr.setTimeUnit(timeUnit);
                        }
                    }
                } else if ("sortOrder".equals(property)) {
                    assignDurationAndTimeUnit(source);
                }
            }
        });
    }

    private void initMoveButtons() {
            moveUp.setAction(new AbstractAction("moveUp") {

                @Override
                public void actionPerform(Component component) {
                    Set<CardRole> selected = rolesTable.getSelected();
                    if (selected.isEmpty())
                        return;

                    CardRole curCr = selected.iterator().next();
                    CardRole prevCr = getCardRoleDependsOnSortOrder(curCr, false);
                    if (prevCr != null) {
                        Integer tmp = curCr.getSortOrder();
                        curCr.setSortOrder(prevCr.getSortOrder());
                        prevCr.setSortOrder(tmp);

                        tmpCardRolesDs.doSort();
                    }
                }
            });

            moveDown.setAction(new AbstractAction("moveDown") {

                @Override
                public void actionPerform(Component component) {
                    Set<CardRole> selected = rolesTable.getSelected();
                    if (selected.isEmpty())
                        return;

                    CardRole curCr = selected.iterator().next();
                    CardRole nextCr = getCardRoleDependsOnSortOrder(curCr, true);
                    if (nextCr != null) {
                        Integer tmp = curCr.getSortOrder();
                        curCr.setSortOrder(nextCr.getSortOrder());
                        nextCr.setSortOrder(tmp);

                        tmpCardRolesDs.doSort();
                    }
                }
            });
    }

    private void addSortOrderColumn(com.vaadin.ui.Table vRolesTable) {
        MetaPropertyPath sortOrderProperty = rolesTableDs.getMetaClass().getPropertyPath("sortOrder");
        vRolesTable.addGeneratedColumn(sortOrderProperty, new com.vaadin.ui.Table.ColumnGenerator() {

            public com.vaadin.ui.Component generateCell(com.vaadin.ui.Table source, Object itemId, Object columnId) {
                CardRole cardRole = tmpCardRolesDs.getItem((UUID) itemId);
                com.vaadin.ui.Component component = null;
                if (cardRole != null && cardRole.getProcRole().getMultiUser()) {
                    if (editable) {
                        final UUID uuid = (UUID) itemId;
                        WebLookupField orderLookup = new WebLookupField();
                        orderLookup.setOptionsList(getAllowRangeForProcRole(cardRole.getProcRole()));
                        orderLookup.setValue(cardRole.getSortOrder());
                        orderLookup.setWidth("100%");
                        final AbstractSelect orderSelect = (AbstractSelect) WebComponentsHelper.unwrap(orderLookup);
                        orderSelect.setNullSelectionAllowed(false);

                        orderLookup.addListener(new ValueListener() {
                            public void valueChanged(Object source, String property, Object prevValue, final Object value) {
                                (tmpCardRolesDs.getItem(uuid)).setSortOrder((Integer) orderSelect.getValue());
                                tmpCardRolesDs.doSort();
                            }
                        });
                        component = orderSelect;
                    } else {
                        component = new com.vaadin.ui.Label(cardRole.getSortOrder().toString());
                    }
                }
                return component;
            }
        });
    }

    private com.haulmont.cuba.gui.components.Button createAddGroupButton(final CardRole cardRole) {
        final CardRolesFrame crf = this;
        com.haulmont.cuba.gui.components.Button addUserGroupButton = new WebButton();
        addUserGroupButton.setIcon("select/img/user-group-button.png");
        addUserGroupButton.setStyleName(BaseTheme.BUTTON_LINK);
        com.vaadin.ui.Button vAddUserGroupButton = (com.vaadin.ui.Button) WebComponentsHelper.unwrap(addUserGroupButton);
        final Class userGroupAddClass = ScriptingProvider.loadClass("com.haulmont.workflow.web.ui.usergroup.UserGroupAdd");
        vAddUserGroupButton.addListener(new com.vaadin.ui.Button.ClickListener() {
            private static final long serialVersionUID = -3820323886456571938L;

            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                Map<String, Object> params = new HashMap<>();
                params.put("secRole", cardRole.getProcRole().getRole());

                List<User> users = new ArrayList<>();
                for (Object o : tmpCardRolesDs.getItemIds()) {
                    CardRole cr = tmpCardRolesDs.getItem((UUID) o);
                    if (cr.getCode().equals(cardRole.getCode()) && cr.getUser() != null) {
                        users.add(cr.getUser());
                    }
                }
                params.put("Users", users);
                App.getInstance().getWindowManager().getDialogParams().setWidth(680);
                final Window window = crf.openWindow("wf$UserGroup.add", WindowManager.OpenType.DIALOG, params);
                window.addListener(new Window.CloseListener() {
                    private static final long serialVersionUID = -4182051025753394757L;

                    public void windowClosed(String actionId) {
                        if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                            Set<User> selectedUsers;
                            Set<User> validUsers = new HashSet<>();
                            Set<User> invalidUsers = new HashSet<>();
                            try {
                                selectedUsers = (Set<User>) userGroupAddClass.getMethod("getSelectedUsers").invoke(window);
                            } catch (Exception e) {
                                throw new IllegalStateException("Can't invoke UserGroupAdd.getCardRoles(): " + e);
                            }

                            User oldUser = cardRole.getUser();
                            //if code is right worked, then this comment need to REMOVE
                            /*cardRole.setUser(null);
                            tmpCardRolesDs.updateItem(cardRole);
                            for (Object o : new ArrayList(tmpCardRolesDs.getItemIds())) {
                                CardRole cr = tmpCardRolesDs.getItem((UUID) o);
                                if (cr.getCode().equals(cardRole.getCode()) && !cardRole.getId().equals(cr.getId())) {
                                    tmpCardRolesDs.removeItem(cr);
                                }
                            }*/

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

                            ProcRole procRole = cardRole.getProcRole();
                            String code = cardRole.getCode();

                            for (Object o : new ArrayList(tmpCardRolesDs.getItemIds())) {
                                CardRole cr = tmpCardRolesDs.getItem((UUID) o);
                                if (cr.getCode().equals(cardRole.getCode()) && cr.getProcRole().getMultiUser() && !selectedUsers.contains(cr.getUser()))
                                    tmpCardRolesDs.removeItem(cr);
                            }

                            if (!validUsers.isEmpty()) {
                                if (oldUser == null) {
                                    oldUser = validUsers.iterator().next();
                                    CardRole cr = metadata.create(CardRole.class);
                                    cr.setUser(oldUser);
                                    cr.setProcRole(cardRole.getProcRole());
                                    cr.setCode(cardRole.getCode());
                                    cr.setNotifyByEmail(true);
                                    cr.setNotifyByCardInfo(true);
                                    cr.setCard(card);
                                    assignNextSortOrder(cr);
                                    tmpCardRolesDs.addItem(cr);
                                    validUsers.remove(oldUser);
                                } else {
                                    if (validUsers.contains(oldUser)){
                                        cardRole.setUser(oldUser);
                                        actorActionsFieldsMap.get(cardRole).setValue(oldUser);
                                        validUsers.remove(oldUser);
                                        tmpCardRolesDs.updateItem(cardRole);
                                    }
                                }
                            }
                            List<CardRole> cardRolesToAdd = createCardRoles(validUsers, procRole, code);

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

        vAddUserGroupButton.setVisible(cardRole.getProcRole().getMultiUser()
                /*&& procRolePermissionsService.isPermitted(card, cardRole.getProcRole(), getState(), ProcRolePermissionType.ADD)*/);
        return addUserGroupButton;

    }

    //fills users datasource with users with necessary security role (if set)
    //and not added to process actors as member of current procRole
    protected void fillUsersOptionsDs(CardRole cardRole, CollectionDatasource usersDs) {
        if (usersDs == null)
            throw new RuntimeException("usersDs cannot be null");
        Role secRole = cardRole.getProcRole().getRole();
        /*Set<User> addedUsersOfProcRole = getUsersByProcRole(cardRole.getProcRole());
        if (cardRole.getUser() != null) {
            addedUsersOfProcRole.remove(cardRole.getUser());
        }*/

        Collection<User> dsItems;
        if (secRole == null) {
            dsItems = getUsers();//ListUtils.removeAll(getUsers(), addedUsersOfProcRole);
        } else {
            dsItems = getRoleUsers(secRole);// ListUtils.removeAll(getRoleUsers(secRole), addedUsersOfProcRole);
        }

        for (User u : dsItems) {
            usersDs.addItem(u);
        }
    }

    /**
     * Find all users, who have a given role
     *
     * @param secRole
     * @return collection of users
     */
    protected Collection<User> getRoleUsers(Role secRole) {
        Collection<User> roleUsers = roleUsersMap.get(secRole);

        if (roleUsers == null) {
            String view = View.MINIMAL;
            LoadContext ctx = new LoadContext(User.class).setView(view);
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

    protected CollectionDatasource createUserOptionsDs(CardRole cardRole) {
        CollectionDatasource usersDs = procRoleUsers.get(cardRole.getProcRole());
        if (usersDs == null) {
            MetaClass metaClass = MetadataProvider.getSession().getClass(User.class);
            usersDs = new DsBuilder(getDsContext())
                    .setMetaClass(metaClass)
                    .setId("usersDs")
                    .setViewName("_minimal")
                    .buildCollectionDatasource();
            ((DatasourceImpl) usersDs).valid();
            fillUsersOptionsDs(cardRole, usersDs);
            procRoleUsers.put(cardRole.getProcRole(), usersDs);
        }
        return usersDs;
    }

    private boolean userInRole(User user, Role role) {
        if (user.getUserRoles() == null) return false;
        for (UserRole userRole : user.getUserRoles()) {
            if (userRole.getRole().equals(role)) return true;
        }
        return false;
    }

    private List<CardRole> createCardRoles(Set<User> users, ProcRole procRole, String code) {
        List<CardRole> cardRoles = new ArrayList<CardRole>();
        for (User user : users) {
            boolean isUserInList = false;
            //check for user in list
            for (UUID itemId : tmpCardRolesDs.getItemIds()) {
                if (user.equals(tmpCardRolesDs.getItem(itemId).getUser())) {
                    isUserInList = true;
                    break;
                }
            }
            if (!isUserInList) {
                CardRole cr = metadata.create(CardRole.class);
                cr.setUser(user);
                cr.setProcRole(procRole);
                cr.setCode(code);
                cr.setNotifyByEmail(true);
                cr.setNotifyByCardInfo(true);
                cr.setCard(card);
                cardRoles.add(cr);
                assignNextSortOrder(cr);
                tmpCardRolesDs.addItem(cr);
            }
        }
        return cardRoles;
    }

    protected String generateUserCaption(User user) {
        return user.getInstanceName();
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
                boolean enabled = true; //procRolePermissionsService.isPermitted(cardRole, getState(), ProcRolePermissionType.MODIFY);
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
                Collections.sort(list, tmpCardRolesDs.createEntityComparator());
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
    }

    public void procChanged(Proc proc) {
        procChanged(proc, null);
    }

    public void procChanged(Proc proc, Card card) {
        procRolesDs.refresh(Collections.<String, Object>singletonMap("procId", proc));
        initCreateRoleLookup();
        tmpCardRolesDs.fillForProc(proc);

        for (Component component : rolesActions) {
            component.setEnabled(proc != null && isEnabled());
        }

        if (proc != null && proc.getJbpmProcessKey().equals("EndorsementFull")) {
            moveDown.setVisible(true);
            moveUp.setVisible(true);
        } else {
            moveDown.setVisible(false);
            moveUp.setVisible(false);
        }

        if (proc != null) {
            com.vaadin.ui.Table vRolesTable = ((com.vaadin.ui.Table) WebComponentsHelper.unwrap(rolesTable));
            if (BooleanUtils.isTrue(proc.getCombinedStagesEnabled())) {
                showSortOrderCheckBox.setVisible(true);
                if (rolesTable.getColumn("sortOrder") == null) {
                    try {
                        addSortOrderColumn(vRolesTable);
                    } catch (IllegalArgumentException iae) {
                    }
                    if (vRolesTable.getColumnHeader("sortOrder") != null) {
                        MetaPropertyPath mpp = rolesTable.getDatasource().getMetaClass().getPropertyPath("sortOrder");
                        vRolesTable.setColumnHeader(mpp, MessageProvider.getMessage(CardRolesFrame.class, "sortOrder"));
                    }
                }
            } else {
                showSortOrderCheckBox.setValue(false);
                showSortOrderCheckBox.setVisible(false);
                if (vRolesTable.getColumnHeader("sortOrder") != null) {
                    vRolesTable.removeGeneratedColumn("sortOrder");
                }
                if (rolesTable.getColumn("sortOrder") != null) {
                    rolesTable.removeGeneratedColumn("sortOrder");
                    rolesTable.removeColumn(rolesTable.getColumn("sortOrder"));
                }
            }
        }
    }

    public void initDefaultActors(Proc proc) {
        //if (!tmpCardRolesDs.getItemIds().isEmpty())
        //    return;

        LoadContext ctx = new LoadContext(DefaultProcActor.class);
        ctx.setQueryString("select a from wf$DefaultProcActor a where a.procRole.proc.id = :procId and a.user.deleteTs is null")
                .addParameter("procId", proc.getId());
        ctx.setView("edit");
        List<DefaultProcActor> dpaList = ServiceLocator.getDataService().loadList(ctx);
        for (DefaultProcActor dpa : dpaList) {
            addProcActor(proc, dpa.getProcRole().getCode(), dpa.getUser(), dpa.getSortOrder(), dpa.getNotifyByEmail(), true);
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
                boolean addAssignedActor = true;
                boolean singleUserIsNull = false;
                for (UUID cardRoleId : tmpCardRolesDs.getItemIds()) {
                    CardRole cardRole = tmpCardRolesDs.getItem(cardRoleId);
                    if (procRole.equals(cardRole.getProcRole())) {
                        found = true;
                        if (BooleanUtils.isTrue(procRole.getMultiUser())) {
                            if (addAssignedActor)
                                addAssignedActor = !UserSessionProvider.getUserSession().getCurrentOrSubstitutedUser().equals(cardRole != null ? cardRole.getUser() : null);
                        } else {
                            if (cardRole.getUser() == null) {
                                addAssignedActor = !UserSessionProvider.getUserSession().getCurrentOrSubstitutedUser().equals(cardRole != null ? cardRole.getUser() : null);
                                singleUserIsNull = true;
                            }
                            break;
                        }
                    }
                }
                if (!found || (BooleanUtils.isTrue(procRole.getMultiUser()) || singleUserIsNull) && addAssignedActor) {
                    addProcActor(procRole.getProc(), procRole.getCode(), UserSessionProvider.getUserSession().getCurrentOrSubstitutedUser(), true);
                }
            }
        }
    }

    /*private void assignNextSortOrder(CardRole cr) {
        if (cr.getSortOrder() != null)
            return;
        List<CardRole> cardRoles = getAllCardRolesWithProcRole(cr.getProcRole());
        if (cardRoles.size() == 0) {
            cr.setSortOrder(1);
        } else if (cr.getProcRole().getMultiUser()) {
            int max = getMaxSortOrderInCardRoles(cardRoles);
            if (OrderFillingType.fromId(cr.getProcRole().getOrderFillingType()).equals(OrderFillingType.PARALLEL)) {
                cr.setSortOrder(max);
            }
            if (OrderFillingType.fromId(cr.getProcRole().getOrderFillingType()).equals(OrderFillingType.SEQUENTIAL)) {
                cr.setSortOrder(max + 1);
            }
        }
    }*/

    private void assignNextSortOrder(CardRole cr) {
        if (cr.getSortOrder() != null)
            return;
        List<CardRole> cardRoles = getAllCardRolesWithProcRole(cr.getProcRole());
        if (cardRoles.size() == 0) {
            if (cr.getProcRole().getProc().getJbpmProcessKey().equals("EndorsementFull"))
                cr.setSortOrder(cr.getProcRole().getSortOrder());
            else
                cr.setSortOrder(1);
        } else if (cr.getProcRole().getMultiUser()) {
            int max = getMaxSortOrderInCardRoles(cardRoles);
            if (OrderFillingType.fromId(cr.getProcRole().getOrderFillingType()).equals(OrderFillingType.PARALLEL)) {
                cr.setSortOrder(max);
                changeSortOrderByAllParallelCardRoles(max);
            }
            if (OrderFillingType.fromId(cr.getProcRole().getOrderFillingType()).equals(OrderFillingType.SEQUENTIAL)) {
                int parallelGroupNumb = getParallelGroupNumberCardRoles();
                if (parallelGroupNumb >= max )
                    cr.setSortOrder(parallelGroupNumb + 1);
                else
                    cr.setSortOrder(max + 1);
            }
        }
    }

    private void assignDurationAndTimeUnit(CardRole cardRole) {
        for (UUID uuid : tmpCardRolesDs.getItemIds()) {
            CardRole cr = tmpCardRolesDs.getItem(uuid);
            if (!cr.equals(cardRole) && cr.getSortOrder() != null && cr.getSortOrder().equals(cardRole.getSortOrder())
                    && cr.getProcRole() != null && cr.getProcRole().equals(cardRole.getProcRole())) {
                cardRole.setDuration(cr.getDuration());
                TimeUnit timeUnit = cr.getTimeUnit();
                if (timeUnit != null)
                    cardRole.setTimeUnit(timeUnit);
                break;
            }
        }
    }

    private int getParallelGroupNumberCardRoles() {
        for (UUID id : tmpCardRolesDs.getItemIds()) {
            CardRole role = tmpCardRolesDs.getItem(id);
            if (OrderFillingType.fromId(role.getProcRole().getOrderFillingType()).equals(OrderFillingType.PARALLEL))
                return role.getSortOrder();
        }
        return 0;
    }

    private void changeSortOrderByAllParallelCardRoles(int sortOrder) {
        for (UUID id : tmpCardRolesDs.getItemIds()) {
            CardRole role = tmpCardRolesDs.getItem(id);
            if (OrderFillingType.fromId(role.getProcRole().getOrderFillingType()).equals(OrderFillingType.PARALLEL))
                role.setSortOrder(sortOrder);
        }
    }

    private List<CardRole> getAllCardRolesWithProcRole(ProcRole pr) {
        List<CardRole> cardRoles = new ArrayList<CardRole>();
        for (UUID id : tmpCardRolesDs.getItemIds()) {
            CardRole cr = tmpCardRolesDs.getItem(id);
            if (cr.getProcRole().equals(pr)) {
                cardRoles.add(cr);
            }
        }
        return cardRoles;
    }

    private List<Integer> getAllowRangeForProcRole(ProcRole pr) {
        List<Integer> range = new ArrayList<Integer>();
        List<CardRole> cardRoles = getAllCardRolesWithProcRole(pr);
        if (cardRoles.size() == 1) {
            range.add(cardRoles.get(0).getSortOrder());
        } else {
            int min = getMinSortOrderInCardRoles(cardRoles);
            int max = cardRoles.size() - 1; //getMaxSortOrderInCardRoles(cardRoles);
            for (int i = min - 1; i <= max + 1; i++) {
                if (i > 0) range.add(i);
            }
        }
        return range;
    }

    private int getMaxSortOrderInCardRoles(List<CardRole> roles) {
        int max = 0;
        for (CardRole role : roles) {
            if (role.getSortOrder() != null && role.getSortOrder() > max)
                max = role.getSortOrder();
        }
        return max > roles.size() ? roles.size() : max;
    }

    private int getMinSortOrderInCardRoles(List<CardRole> roles) {
        if (roles == null || roles.size() == 1) return 0;
        return 1;
    }

    //todo gorbunkov review and refactor next two methods
    //setProcActor must delete other actors and set the user sent in param in case of multiUser role
    public void setProcActor(Proc proc, ProcRole procRole, User user, boolean notifyByEmail, boolean notifyByCardInfo) {
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
            cardRole = metadata.create(CardRole.class);

            if (proc.getRoles() == null) {
                proc = getDsContext().getDataService().reload(proc, "edit");
            }

            cardRole.setProcRole(procRole);
            cardRole.setCode(procRole.getCode());
            cardRole.setCard(card);
            cardRole.setNotifyByEmail(notifyByEmail);
            cardRole.setNotifyByCardInfo(notifyByCardInfo);
            assignNextSortOrder(cardRole);
            cardRole.setUser(user);
            tmpCardRolesDs.addItem(cardRole);
        } else {
            cardRole.setUser(user);
            cardRole.setNotifyByEmail(notifyByEmail);
            cardRole.setNotifyByCardInfo(notifyByCardInfo);
        }
    }

    public void addProcActor(Proc proc, String roleCode, User user, boolean notifyByEmail) {
        addProcActor(proc, roleCode, user, notifyByEmail, true);
    }

    public void addProcActor(Proc proc, String roleCode, User user, boolean notifyByEmail, boolean notifyByCardInfo) {
        addProcActor(proc, roleCode, user, null, notifyByEmail, notifyByCardInfo);
    }

    public void addProcActor(Proc proc, String roleCode, User user, Integer sortOrder, boolean notifyByEmail, boolean notifyByCardInfo) {
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
        if (procActorExists(procRole, user))
            removeProcActor(procRole.getCode(), user);
        if (BooleanUtils.isTrue(procRole.getMultiUser())) {
            CardRole cardRole = metadata.create(CardRole.class);
            cardRole.setProcRole(procRole);
            cardRole.setCode(roleCode);
            cardRole.setCard(card);
            cardRole.setNotifyByEmail(notifyByEmail);
            cardRole.setNotifyByCardInfo(notifyByCardInfo);
            cardRole.setUser(user);
            cardRole.setSortOrder(sortOrder);
            assignNextSortOrder(cardRole);
            tmpCardRolesDs.addItem(cardRole);
        } else {
            setProcActor(proc, procRole, user, notifyByEmail, notifyByCardInfo);
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
                if (procRole.equals(cr.getProcRole()) && (cr.getUser() != null && cr.getUser().equals(user) || user == null)) {
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
            if (isNeedRole(pr)) {
                options.add(pr);
            }
        }
        options.add(0, createRoleCaption);
        createRoleLookup.setOptionsList(options);
        createRoleLookup.setNullOption(createRoleCaption);
    }

    protected boolean isNeedRole(ProcRole pr) {
        return BooleanUtils.isTrue(pr.getMultiUser()) || !alreadyAdded(pr);
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
                action.setVisible(enabled);
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
        this.editable = editable;

        for (Action action : rolesTable.getActions()) {
            action.setVisible(editable);
        }
        com.vaadin.ui.Table vRolesTable = (com.vaadin.ui.Table) WebComponentsHelper.unwrap(rolesTable);
        vRolesTable.setReadOnly(!editable);
        vRolesTable.setEditable(editable);
        createRoleLookup.setEditable(editable);
        for (CardRoleField cardRoleField : actorActionsFieldsMap.values()) {
            cardRoleField.setEditable(editable);
        }
    }
	
    private void refreshFieldsWithRole(CardRole cardRole) {
        for (CardRole cr : actorActionsFieldsMap.keySet()) {
            if (cr.getCode().equals(cardRole.getCode()) && !cr.equals(cardRole)) {
                CardRoleField cardRoleField = actorActionsFieldsMap.get(cr);
                CardRole role = tmpCardRolesDs.getItem(cr.getId());
                cardRoleField.initField(role == null ? cr : role, cardRoleField.getValue());
            }
        }
    }

    public void fillMissingRoles() {
        Set<String> requiredRolesCodes = getRequiredRolesCodes(false/*cardRolesDs.getItemIds().size() == 0*/);
        for (Object itemId : cardRolesDs.getItemIds()) {
            CardRole cardRole = (CardRole) cardRolesDs.getItem((UUID) itemId);
            requiredRolesCodes.remove(cardRole.getCode());
        }

        Proc proc = card.getProc();
        proc = cardRolesDs.getDataSupplier().reload(proc, "edit");

        for (String roleCode : requiredRolesCodes) {
            if (!roleCode.contains("|"))
                addProcActor(proc, roleCode, null, true, true);
            else {
                String[] codes = roleCode.split("\\s*[|]\\s*");
                if (!containsAnyRoleCode(codes)) {
                    for (String code : codes) {
                        addProcActor(proc, code, null, true, true);
                    }
                }
            }
        }
    }

    private boolean containsAnyRoleCode(String[] roleCodes) {
        if (roleCodes != null && tmpCardRolesDs.getItemIds() != null)
            for (UUID uuid : tmpCardRolesDs.getItemIds()) {
                CardRole cardRole = tmpCardRolesDs.getItem(uuid);
                for (String code : roleCodes) {
                    if (code.equals(cardRole.getCode()))
                        return true;
                }
            }
        return false;
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
            CardRole cardRole = (CardRole) cardRolesDs.getItem((UUID) itemId);
            if (cardRole.getUser() == null && (deletedEmptyRoleCodes == null ||
                    !deletedEmptyRoleCodes.contains(cardRole.getCode()))) {
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

    public void setDeletedEmptyRoleCodes(List deletedEmptyRoleCodes) {
        this.deletedEmptyRoleCodes = deletedEmptyRoleCodes;
    }

    private Set<String> getRequiredRolesCodes(boolean isAll) {
        if (StringUtils.isNotEmpty(requiredRolesCodesStr)) {
            String[] s = requiredRolesCodesStr.split(isAll ? "\\s*[,|]\\s*" : "\\s*,\\s*");
            return new LinkedHashSet<String>(Arrays.asList(s));
        }
        return Collections.emptySet();
    }

    public void setRequiredRolesCodesStr(String requiredRolesCodesStr) {
        this.requiredRolesCodesStr = requiredRolesCodesStr;
    }

    public static class CardProcRolesDatasource extends CollectionDatasourceImpl<CardRole, UUID> {

        protected CollectionDatasource<CardRole, UUID> cardRolesDs;
        private CollectionDatasource<CardRole, UUID> activeDs;
        private Map<UUID, CollectionDatasource<CardRole, UUID>> dsRegistry = new HashMap<UUID, CollectionDatasource<CardRole, UUID>>();
        public boolean fill;

        @Override
        public void setup(DsContext dsContext, DataSupplier dataSupplier, String id, MetaClass metaClass, @Nullable View view) {
            super.setup(dsContext, dataSupplier, id, metaClass, view);
            cardRolesDs = dsContext.get("cardRolesDs");
            this.activeDs = this.cardRolesDs;
        }

        public void setCard(Card card) {
            if (card == null)
                this.activeDs = this.cardRolesDs;
            else
                this.activeDs = getDsInternal(card);
        }

        @Override
        public void addItem(CardRole item) throws UnsupportedOperationException {
            super.addItem(item);
            if (!fill)
                activeDs.addItem(item);
        }

        @Override
        public void removeItem(CardRole item) throws UnsupportedOperationException {
            super.removeItem(item);
            if (!fill)
                activeDs.removeItem(item);
        }

        @Override
        protected Comparator<CardRole> createEntityComparator() {
            return new Comparator<CardRole>() {
                public int compare(CardRole cr1, CardRole cr2) {
                    int s1 = cr1.getProcRole().getSortOrder() == null ? 0 : cr1.getProcRole().getSortOrder();
                    int s2 = cr2.getProcRole().getSortOrder() == null ? 0 : cr2.getProcRole().getSortOrder();
                    if (s1 == s2) {
                        s1 = cr1.getSortOrder() == null ? 0 : cr1.getSortOrder();
                        s2 = cr2.getSortOrder() == null ? 0 : cr2.getSortOrder();
                    }
                    if (s1 == s2) {
                        String str1 = "";
                        String str2 = "";
                        if (cr1.getUser() != null)
                            str1 = cr1.getUser().getName() == null ? "" : cr1.getUser().getName();
                        if (cr2.getUser() != null)
                            str2 = cr2.getUser().getName() == null ? "" : cr2.getUser().getName();
                        return str1.compareTo(str2);
                    }
                    return s1 - s2;
                }
            };
        }

        @Override
        public void doSort() {
            super.doSort();
            fireCollectionChanged(CollectionDatasourceListener.Operation.REFRESH, Collections.<Entity>emptyList());
        }

        public void fillForProc(Proc proc) {
            fill = true;
            try {
                removeAll();
                if (proc != null) {
                    for (UUID id : activeDs.getItemIds()) {
                        CardRole cardRole = activeDs.getItem(id);
                        if (BooleanUtils.isNotTrue(cardRole.getProcRole().getInvisible()) && cardRole.getProcRole().getProc().equals(proc))
                            addItem(cardRole);
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

        private void removeAll() {
            List<UUID> items = new ArrayList<>(getItemIds());
            for (UUID id : items)
                removeItem(getItem(id));
        }

        private CollectionDatasource<CardRole, UUID> getDsInternal(Card card) {
            CollectionDatasource<CardRole, UUID> ds = dsRegistry.get(card.getId());
            if (ds == null) {
                DsBuilder dsBuilder = new DsBuilder(cardRolesDs.getDsContext())
                        .setJavaClass(CardRole.class)
                        .setViewName("transition-form")
                        .setId("cardRoles" + card.getId().toString() + "Ds");
                ds = dsBuilder.buildCollectionDatasource();
                ds.setQuery("select cr from wf$CardRole cr where cr.card.id = :custom$card");
                ds.refresh(Collections.<String, Object>singletonMap("card", card));
                dsRegistry.put(card.getId(), ds);
            }
            return ds;
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
            AbstractSelect usersSelect = (AbstractSelect) WebComponentsHelper.unwrap(usersLookup);
            usersSelect.addListener(new Property.ValueChangeListener() {

                public void valueChange(Property.ValueChangeEvent event) {
                    Property eventProperty = event.getProperty();
                    User selectedUser = (User) eventProperty.getValue();
                    CardRole cr = tmpCardRolesDs.getItem(cardRole.getId());
                    if (cr != null)
                        cr.setUser(selectedUser);
                    else
                        cardRole.setUser(selectedUser);
//                    refreshFieldsWithRole(cardRole);
                }
            });

            usersSelect.setItemCaptionMode(AbstractSelect.ItemCaptionMode.EXPLICIT);
            for (Object itemId : usersDs.getItemIds()) {
                User user = (User) usersDs.getItem(itemId);
                String userCaption = generateUserCaption(user);
                usersSelect.setItemCaption(user, userCaption);
            }

            final com.vaadin.ui.Table vRolesTable = (com.vaadin.ui.Table) WebComponentsHelper.unwrap(rolesTable);
            boolean enabled = true;// procRolePermissionsService.isPermitted(cardRole, getState(), ProcRolePermissionType.MODIFY);
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

    public Map<CardRole, CardRoleField> getActorActionsFieldsMap() {
        return actorActionsFieldsMap;
    }

}
