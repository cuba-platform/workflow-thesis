/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.gui.app.base;

import com.google.common.base.Preconditions;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.CollectionDatasource.Operation;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.DatasourceImpl;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.entity.UserRole;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.app.cardroles.CardRolesFrameWorker;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.global.TimeUnit;
import com.haulmont.workflow.gui.app.usergroup.UserGroupAdd;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

public class CardRolesFrame extends AbstractFrame {

    protected Map<ProcRole, CollectionDatasource> procRoleUsers = new HashMap<>();
    protected boolean isInactiveRoleVisible = true;

    public interface Listener {
        void afterInitDefaultActors(Proc proc);
    }

    public interface Companion {
        void setTableColumnHeader(Table table, Object columnId, String header);

        void setTableVisibleColumns(Table table, Object[] visibleColumns);

        Object[] getVisibleColumns(Table table);

        void setLookupNullSelectionAllowed(LookupField lookupField, boolean value);
    }

    protected Set<Listener> listeners = new HashSet<>();

    protected Card card;
    protected boolean enabled = true;
    protected CardProc cardProc;

    @Inject
    protected CollectionDatasource<CardRole, UUID> cardRolesDs;

    @Inject
    protected CollectionDatasource<ProcRole, UUID> procRolesDs;

    @Inject
    protected CardProcRolesDatasource tmpCardRolesDs;

    @Inject
    protected LookupField createRoleLookup;

    @Inject
    protected Table rolesTable;

    @Inject
    protected Button moveDown;

    @Inject
    protected Button moveUp;

    @Inject
    protected Metadata metadata;

    @Inject
    protected Scripting scripting;

    @Inject
    protected ComponentsFactory componentsFactory;

    @Inject
    protected CardRolesFrameWorker cardRolesFrameWorker;

    protected Companion companion;

    protected List<Component> rolesActions = new ArrayList<>();
    protected String createRoleCaption;
    protected Map<CardRole, LookupPickerField> actorFieldsMap = new HashMap<>();
    protected List<User> users;
    protected Map<Role, Collection<User>> roleUsersMap = new HashMap<>();
    protected String requiredRolesCodesStr;
    protected List deletedEmptyRoleCodes;
    protected boolean editable = true;
    protected boolean combinedStagesEnabled;
    protected Proc currentProcess;

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void init() {
        tmpCardRolesDs.valid();

        Preconditions.checkState(cardRolesDs != null, "Enclosing window must declare declare 'cardRolesDs' datasource");

        createRoleCaption = getMessage("createRoleCaption");

        rolesActions.add(createRoleLookup);
        rolesActions.add(getComponent("removeRole"));

        companion = getCompanion();

        rolesTable.removeAction(rolesTable.getAction(RemoveAction.ACTION_ID));
        RemoveAction removeAction = createRemoveAction();
        rolesTable.addAction(removeAction);

        initRolesTable();

        initMoveButtons();

        tmpCardRolesDs.addCollectionChangeListener(e -> {
            initCreateRoleLookup();

            if (e.getOperation() == Operation.REMOVE && !tmpCardRolesDs.fill)
                normalizeSortOrders();

            if (e.getOperation() == Operation.ADD || e.getOperation() == Operation.REMOVE) {
                tmpCardRolesDs.doSort();
            }
        });

        createRoleLookup.addValueChangeListener(e -> {
            if ((e.getValue() == null) || createRoleCaption.equals(e.getValue())) {
                return;
            }

            ProcRole procRole = (ProcRole) e.getValue();
            tmpCardRolesDs.addItem(createCardRole(procRole, null, true, true));
        });
    }

    protected RemoveAction createRemoveAction() {
        return new RemoveAction(rolesTable, false) {
            @Override
            protected void afterRemove(Set selected) {
                //noinspection unchecked
                afterCardRolesRemove(selected);
            }
        };
    }

    protected void afterCardRolesRemove(Set<CardRole> cardRoles) {
        if (CollectionUtils.isNotEmpty(cardRoles)) {
            for (CardRole cr : cardRoles) {
                actorFieldsMap.remove(cr);
            }
        }
    }

    public CardRole createCardRole(ProcRole procRole, User user, boolean notifyByEmail, boolean notifyByCardInfo) {
        return createCardRole(procRole, user, null, notifyByEmail, notifyByCardInfo);
    }

    public CardRole createCardRole(ProcRole procRole, User user, Integer sortOrder,
                                   boolean notifyByEmail, boolean notifyByCardInfo) {
        CardRole cardRole = metadata.create(CardRole.class);
        cardRole.setProcRole(procRole);
        cardRole.setCode(procRole.getCode());
        cardRole.setNotifyByEmail(notifyByEmail);
        cardRole.setNotifyByCardInfo(notifyByCardInfo);
        cardRole.setCard(card);
        cardRole.setUser(user);
        cardRole.setSortOrder(sortOrder);
        assignNextSortOrder(cardRole);
        assignDurationAndTimeUnit(cardRole);
        return cardRole;
    }

    protected CardRole getCardRoleDependsOnSortOrder(CardRole curCr, boolean forward) {
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

    protected void initRolesTable() {
        rolesTable.addGeneratedColumn("user", new Table.ColumnGenerator<CardRole>() {
            @Override
            public Component generateCell(CardRole cardRole) {
                return generateUserFieldComponent(cardRole);
            }
        });

        initSortOrderColumn();
        initDurationColumns();
    }

    protected Component generateUserFieldComponent(CardRole cardRole) {
        LookupPickerField cardRoleField = actorFieldsMap.get(cardRole);
        if (cardRoleField != null) {
            cardRoleField.setValue(cardRole.getUser());
        } else {
            cardRoleField = fillActorActionsFieldsMap(cardRole);
        }
        return cardRoleField;
    }

    protected LookupPickerField fillActorActionsFieldsMap(CardRole cardRole) {
        LookupPickerField cardRoleField = initCardRoleField(cardRole, cardRole.getUser());
        actorFieldsMap.put(cardRole, cardRoleField);
        return cardRoleField;
    }

    protected void initDurationColumns() {
        tmpCardRolesDs.addItemPropertyChangeListener(e -> {
            if ("duration".equals(e.getProperty())) {
                for (UUID uuid : tmpCardRolesDs.getItemIds()) {
                    CardRole cr = tmpCardRolesDs.getItem(uuid);
                    if (cr.getSortOrder() != null && cr.getSortOrder().equals(e.getItem().getSortOrder())
                            && cr.getProcRole() != null && cr.getProcRole().equals(e.getItem().getProcRole()) && isDurationEditable(cr)) {
                        cr.setDuration(e.getItem().getDuration());
                    }
                }
            } else if ("timeUnit".equals(e.getProperty())) {
                for (UUID uuid : tmpCardRolesDs.getItemIds()) {
                    CardRole cr = tmpCardRolesDs.getItem(uuid);
                    if (cr.getSortOrder() != null && cr.getSortOrder().equals(e.getItem().getSortOrder())
                            && cr.getProcRole() != null && cr.getProcRole().equals(e.getItem().getProcRole()) && isTimeUnitEditable(cr)) {
                        TimeUnit timeUnit = e.getItem().getTimeUnit();
                        if (timeUnit != null)
                            cr.setTimeUnit(timeUnit);
                    }
                }
            } else if ("sortOrder".equals(e.getProperty())) {
                assignDurationAndTimeUnit(e.getItem());
            }
        });
    }

    protected void initMoveButtons() {
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

    protected void initSortOrderColumn() {
        rolesTable.addGeneratedColumn("sortOrder", new Table.ColumnGenerator<CardRole>() {
            @Override
            public Component generateCell(final CardRole cardRole) {
                return getSortOrderGeneratedComponent(cardRole);
            }
        });
    }

    protected Action createAddGroupAction(final CardRole cardRole) {
        final CardRolesFrame crf = this;
        Action addUserGroupAction = new AbstractAction("addUserGroup") {

            @Override
            public void actionPerform(Component component) {
                getDialogParams()
                        .setWidth("835px")
                        .setHeight("505px");

                Map<String, Object> params = getUsergroupAddParams(cardRole);
                Window window = crf.openWindow("wf$UserGroup.add", WindowManager.OpenType.DIALOG, params);
                window.addCloseListener(new AddUserGroupWindowCloseListener(window, cardRole));
            }
        };
        addUserGroupAction.setIcon("icons/wf-user-group-button.png");
        addUserGroupAction.setVisible(cardRole.getProcRole().getMultiUser());
        return addUserGroupAction;

    }

    protected Map<String, Object> getUsergroupAddParams(CardRole cardRole) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("secRole", cardRole.getProcRole().getRole());

        List<User> users = new ArrayList<User>();
        for (Object o : tmpCardRolesDs.getItemIds()) {
            CardRole cr = tmpCardRolesDs.getItem((UUID) o);
            if (cr.getCode().equals(cardRole.getCode()) && cr.getUser() != null) {
                users.add(cr.getUser());
            }
        }
        params.put("Users", users);
        return params;
    }

    protected Set<User> getSelectedUsers(CardRole cardRole, Window window) {
        try {
            UserGroupAdd userGroup = (UserGroupAdd) window;
            return userGroup.getSelectedUsers();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            query.setParameter("role", secRole);
            List<User> loadedUsers = getDsContext().getDataSupplier().loadList(ctx);
            if (loadedUsers == null) {
                roleUsers = new ArrayList<>();
            }
            roleUsers = new ArrayList<>(loadedUsers);
            roleUsersMap.put(secRole, roleUsers);
        }

        return roleUsers;
    }

    //we'll read users list only once and will use this list while filling users option datasources
    protected List<User> getUsers() {
        if (users == null) {
            LoadContext ctx = new LoadContext(User.class).setView(View.MINIMAL);
            ctx.setQueryString("select u from sec$User u order by u.name");
            List<User> loadedUsers = getDsContext().getDataSupplier().loadList(ctx);
            users = new ArrayList<>(loadedUsers);
        }
        return users;
    }

    protected CollectionDatasource createUserOptionsDs(CardRole cardRole) {
        CollectionDatasource usersDs = procRoleUsers.get(cardRole.getProcRole());
        if (usersDs == null) {
            MetaClass metaClass = metadata.getSession().getClass(User.class);
            usersDs = DsBuilder.create(getDsContext())
                    .setMetaClass(metaClass)
                    .setId("usersDs")
                    .setViewName("_minimal")
                    .buildCollectionDatasource();
            ((DatasourceImpl) usersDs).valid();
            usersDs.setAllowCommit(false);
            fillUsersOptionsDs(cardRole, usersDs);
            procRoleUsers.put(cardRole.getProcRole(), usersDs);
        }
        return usersDs;
    }

    protected boolean userInRole(User user, Role role) {
        if (user.getUserRoles() == null) return false;
        for (UserRole userRole : user.getUserRoles()) {
            if (userRole.getRole().equals(role)) return true;
        }
        return false;
    }

    protected List<CardRole> createCardRoles(Set<User> users, ProcRole procRole, String code) {
        List<CardRole> cardRoles = new ArrayList<>();
        for (User user : users) {
            CardRole cr = createCardRole(procRole, user, true, true);
            cardRoles.add(cr);
            tmpCardRolesDs.addItem(cr);
        }
        return cardRoles;
    }

    protected String generateUserCaption(User user) {
        return user.getInstanceName();
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
                        if (!tmpCardRolesDs.containsItem(cardRole.getUuid())
                                && BooleanUtils.isNotTrue(cardRole.getProcRole().getInvisible())) {

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
                        if (!procRolesDs.containsItem(procRole.getId()) && (procRole.getInvisible() == null || !procRole.getInvisible())) {
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
        currentProcess = proc;
        procRolesDs.refresh(Collections.singletonMap("procId", proc));
        initCreateRoleLookup();
        tmpCardRolesDs.fillForProc(proc);

        for (Component component : rolesActions) {
            component.setEnabled(proc != null && isEnabled());
        }

        boolean visible = isMoveCardRoleButtonsVisible(proc);
        moveDown.setVisible(visible);
        moveUp.setVisible(visible);

        if (proc != null) {
            //todo implement companion for desktop or wait for ability to manage columns through Table interface, null comparison looks ugly
            Object[] visibleColumns = (companion == null) ? new Object[]{} : companion.getVisibleColumns(rolesTable);

            MetaClass cardRoleMetaClass = metadata.getExtendedEntities().getEffectiveMetaClass(CardRole.class);

            MetaPropertyPath sortOrderMpp = cardRoleMetaClass.getPropertyPath("sortOrder");
            MetaPropertyPath durationMpp = cardRoleMetaClass.getPropertyPath("duration");
            final MetaPropertyPath timeUnitMpp = cardRoleMetaClass.getPropertyPath("timeUnit");

            if (BooleanUtils.isTrue(proc.getCombinedStagesEnabled()) || combinedStagesEnabled) {
                if (!ArrayUtils.contains(visibleColumns, sortOrderMpp)) {
                    visibleColumns = ArrayUtils.add(visibleColumns, sortOrderMpp);
                    if (companion != null)
                        companion.setTableColumnHeader(rolesTable, sortOrderMpp, messages.getMessage(CardRole.class, "CardRole.sortOrder"));
                }
            } else {
                visibleColumns = ArrayUtils.removeElement(visibleColumns, sortOrderMpp);
            }

            if (isProcessDurationEnabled(proc)) {
                if (!ArrayUtils.contains(visibleColumns, durationMpp)) {
                    visibleColumns = ArrayUtils.add(visibleColumns, durationMpp);
                    if (companion != null)
                        companion.setTableColumnHeader(rolesTable, durationMpp, messages.getMessage(CardRole.class, "CardRole.duration"));
                }
                rolesTable.addGeneratedColumn("duration", new Table.ColumnGenerator() {
                    @Override
                    public Component generateCell(Entity entity) {
                        return generateDurationComponent((CardRole) entity);
                    }
                });
                rolesTable.addGeneratedColumn("timeUnit", new Table.ColumnGenerator() {
                    @Override
                    public Component generateCell(Entity entity) {
                        return generateTimeUnitComponent((CardRole) entity, timeUnitMpp);
                    }
                });
                if (!ArrayUtils.contains(visibleColumns, timeUnitMpp)) {
                    visibleColumns = ArrayUtils.add(visibleColumns, timeUnitMpp);
                    if (companion != null)
                        companion.setTableColumnHeader(rolesTable, timeUnitMpp, messages.getMessage(CardRole.class, "CardRole.timeUnit"));
                }
            } else {
                visibleColumns = ArrayUtils.removeElement(visibleColumns, durationMpp);
                visibleColumns = ArrayUtils.removeElement(visibleColumns, timeUnitMpp);
            }

            if (companion != null)
                companion.setTableVisibleColumns(rolesTable, visibleColumns);
        }
    }

    protected boolean isProcessDurationEnabled(Proc proc) {
        return BooleanUtils.isTrue(proc.getDurationEnabled());
    }

    protected TextField generateDurationComponent(final CardRole cardRole) {
        final TextField durationTF = componentsFactory.createComponent(TextField.class);
        durationTF.setWidth("60px");
        if (cardRole != null && cardRole.getDuration() != null)
            durationTF.setValue(cardRole.getDuration().toString());
        durationTF.addValueChangeListener(getDurationValueListener(cardRole, durationTF));
        return durationTF;
    }

    protected Component.ValueChangeListener getDurationValueListener(CardRole cardRole, TextField field) {
        return new DurationValueListener(cardRole, field);
    }

    protected class DurationValueListener implements Component.ValueChangeListener {

        protected CardRole cardRole;
        protected TextField field;

        public DurationValueListener(CardRole cardRole, TextField field) {
            this.cardRole = cardRole;
            this.field = field;
        }

        @Override
        public void valueChanged(ValueChangeEvent e) {
            try {
                setValue(e.getValue());
            } catch (NumberFormatException ex) {
                String msg = messages.getMessage("com.haulmont.cuba.gui", "validationFail");
                String caption = messages.getMessage("com.haulmont.cuba.gui", "validationFail.caption");
                showNotification(caption, msg, NotificationType.TRAY);
                setValue(e.getPrevValue());
            }
        }

        protected void setValue(@Nullable Object value) throws NumberFormatException {
            if (value != null) {
                Integer duration = Integer.parseInt((String) value);
                if (cardRole != null)
                    cardRole.setDuration(duration);
            } else {
                if (cardRole != null)
                    cardRole.setDuration(null);
            }
        }
    }

    protected LookupField generateTimeUnitComponent(final CardRole cardRole, MetaPropertyPath timeUnitMpp) {
        final LookupField timeUnitField = componentsFactory.createComponent(LookupField.class);
        timeUnitField.setOptionsList(timeUnitMpp.getRange().asEnumeration().getValues());
        timeUnitField.setWidth("60");
        if (companion != null) {
            companion.setLookupNullSelectionAllowed(timeUnitField, false);
        } else {
            timeUnitField.setRequired(true);
        }
        if (cardRole != null && cardRole.getTimeUnit() != null) {
            timeUnitField.setValue(cardRole.getTimeUnit());
        }
        timeUnitField.addValueChangeListener(getTimeUnitValueListener(cardRole));

        return timeUnitField;
    }

    protected Component.ValueChangeListener getTimeUnitValueListener(CardRole cardRole) {
        return new TimeUnitValueListener(cardRole);
    }

    protected class TimeUnitValueListener implements Component.ValueChangeListener {
        protected CardRole cardRole;

        public TimeUnitValueListener(CardRole cardRole) {
            this.cardRole = cardRole;
        }

        @Override
        public void valueChanged(ValueChangeEvent e) {
            if (cardRole != null)
                cardRole.setTimeUnit((TimeUnit) e.getValue());
        }
    }

    protected Component getSortOrderGeneratedComponent(final CardRole cardRole) {
        if (cardRole != null && cardRole.getProcRole().getMultiUser()) {
            if (editable) {
                LookupField orderLookup = componentsFactory.createComponent(LookupField.class);
                orderLookup.setOptionsList(getAllowRangeForProcRole(cardRole.getProcRole()));
                orderLookup.setValue(cardRole.getSortOrder());
                orderLookup.setWidth("100%");
                if (companion != null) {
                    companion.setLookupNullSelectionAllowed(orderLookup, false);
                } else {
                    orderLookup.setRequired(true);
                }

                orderLookup.addValueChangeListener(getSortOrderValueListener(cardRole));
                return orderLookup;
            } else {
                Label label = componentsFactory.createComponent(Label.class);
                label.setValue(cardRole.getSortOrder() != null ? cardRole.getSortOrder().toString() : "");
                return label;
            }
        }
        return null;
    }

    protected Component.ValueChangeListener getSortOrderValueListener(final CardRole cardRole) {
        return new SortOrderValueListener(cardRole);
    }

    protected class SortOrderValueListener implements Component.ValueChangeListener {
        protected CardRole cardRole;

        public SortOrderValueListener(CardRole cardRole) {
            this.cardRole = cardRole;
        }

        @Override
        public void valueChanged(ValueChangeEvent e) {
            cardRole.setSortOrder((Integer) e.getValue());
            normalizeSortOrders();
            tmpCardRolesDs.doSort();
        }
    }

    //needed for extension
    protected boolean isMoveCardRoleButtonsVisible(Proc proc) {
        return true;
    }

    public void initDefaultActors(Proc proc) {
        //if (!tmpCardRolesDs.getItemIds().isEmpty())
        //    return;

        LoadContext ctx = new LoadContext(DefaultProcActor.class);
        ctx.setQueryString("select a from wf$DefaultProcActor a where a.procRole.proc.id = :procId and a.user.deleteTs is null " +
                "and a.user.active = true")
                .setParameter("procId", proc.getId());
        ctx.setView("edit");
        List<DefaultProcActor> dpaList = getDsContext().getDataSupplier().loadList(ctx);
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
        WfService wfService = AppBeans.get(WfService.NAME);
        UserSessionSource userSessionSource = AppBeans.get(UserSessionSource.NAME);
        User user = userSessionSource.getUserSession().getCurrentOrSubstitutedUser();
        for (UUID procRoleId : procRolesDs.getItemIds()) {
            ProcRole procRole = procRolesDs.getItem(procRoleId);
            if (BooleanUtils.isTrue(procRole.getAssignToCreator()) && wfService.isCurrentUserContainsRole(procRole.getRole())) {
                boolean found = false;
                boolean addAssignedActor = true;
                boolean singleUserIsNull = false;
                CardRole foundCardRole = null;
                for (UUID cardRoleId : tmpCardRolesDs.getItemIds()) {
                    CardRole cardRole = tmpCardRolesDs.getItem(cardRoleId);
                    if (procRole.equals(cardRole.getProcRole())) {
                        found = true;
                        foundCardRole = cardRole;
                        if (BooleanUtils.isTrue(procRole.getMultiUser())) {
                            if (addAssignedActor) {
                                addAssignedActor = !user.equals(cardRole.getUser());
                            }
                        } else {
                            if (cardRole.getUser() == null) {
                                addAssignedActor = true;
                                singleUserIsNull = true;
                            }
                            break;
                        }
                    }
                }
                if (!found || (procRole.getMultiUser() || singleUserIsNull) && addAssignedActor) {
                    if (foundCardRole != null) {
                        addProcActor(procRole.getProc(), procRole.getCode(), user,
                                foundCardRole.getNotifyByEmail(), foundCardRole.getNotifyByCardInfo());
                    } else {
                        addProcActor(procRole.getProc(), procRole.getCode(), user, true, true);
                    }
                }
            }
        }
    }

    protected void assignDurationAndTimeUnit(CardRole cardRole) {
        for (UUID uuid : tmpCardRolesDs.getItemIds()) {
            CardRole cr = tmpCardRolesDs.getItem(uuid);
            if (!cr.equals(cardRole) && cr.getSortOrder() != null && cr.getSortOrder().equals(cardRole.getSortOrder())
                    && cr.getProcRole() != null && cr.getProcRole().equals(cardRole.getProcRole())) {
                if (isDurationEditable(cardRole)) {
                    cardRole.setDuration(cr.getDuration());
                }
                if (isTimeUnitEditable(cardRole)) {
                    TimeUnit timeUnit = cr.getTimeUnit();
                    if (timeUnit != null)
                        cardRole.setTimeUnit(timeUnit);
                }
                break;
            }
        }
    }

    protected List<Integer> getAllowRangeForProcRole(ProcRole pr) {
        List<Integer> range = new ArrayList<>();
        List<CardRole> cardRoles = cardRolesFrameWorker.getAllCardRolesWithProcRole(pr, getTmpCardRoles());
        if (cardRoles.size() == 1) {
            range.add(cardRoles.get(0).getSortOrder());
        } else {
            int min = getMinSortOrderInCardRoles(cardRoles);
            int max = cardRolesFrameWorker.getMaxSortOrderInCardRoles(cardRoles);
            for (int i = min - 1; i <= (max == cardRoles.size() ? max : max + 1); i++) {
                if (i > 0) range.add(i);
            }
        }
        return range;
    }

    protected int getMinSortOrderInCardRoles(List<CardRole> roles) {
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
            cardRole = createCardRole(procRole, user, notifyByEmail, notifyByCardInfo);
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
            proc = getDsContext().getDataSupplier().reload(proc, "edit");
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
            tmpCardRolesDs.addItem(createCardRole(procRole, user, sortOrder, notifyByEmail, notifyByCardInfo));
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

    protected boolean procActorExists(ProcRole procRole, User user) {
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
        for (UUID itemId : new ArrayList<>(uuidCollection)) {
            CardRole item = tmpCardRolesDs.getItem(itemId);
            tmpCardRolesDs.removeItem(item);
        }
    }

    protected void initCreateRoleLookup() {
        // add ProcRole if it has multiUser == true or hasn't been added yet
        createRoleLookup.setValue(createRoleCaption);
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
        Set<String> visibleRoles = tmpCardRolesDs.getVisibleRoles();
        return (visibleRoles == null || visibleRoles.contains(pr.getCode())) && (BooleanUtils.isTrue(pr.getMultiUser()) || !alreadyAdded(pr));
    }

    protected boolean alreadyAdded(ProcRole pr) {
        for (CardRole cr : getDsItems(tmpCardRolesDs)) {
            if (cr.getProcRole().equals(pr))
                return true;
        }
        return false;
    }

    protected <T extends Entity<UUID>> List<T> getDsItems(CollectionDatasource<T, UUID> ds) {
        List<T> items = new ArrayList<>();
        for (UUID id : ds.getItemIds()) {
            items.add(ds.getItem(id));
        }
        return items;
    }

    protected Set<User> getUsersByProcRole(ProcRole procRole) {
        if (procRole == null) {
            return null;
        }
        Set<User> res = new HashSet<>();
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

    protected boolean isDurationEditable(CardRole cardRole) {
        return true;
    }

    protected boolean isTimeUnitEditable(CardRole cardRole) {
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (rolesTable.getActions() != null)
            for (Action action : rolesTable.getActions())
                action.setVisible(enabled);

        for (Component action : rolesActions) {
            action.setEnabled(enabled);
        }
    }

    public void setCardProc(CardProc cardProc) {
        this.cardProc = cardProc;
    }

    protected Proc getProc() {
        return ((cardProc == null) || (cardProc.getProc() == null)) ? card.getProc() : cardProc.getProc();
    }

    protected String getState() {
        return (cardProc == null) ? card.getState() : cardProc.getState();
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        for (Action action : rolesTable.getActions())
            action.setVisible(editable);

        rolesTable.setEditable(editable);

        createRoleLookup.setEditable(editable);
        for (LookupPickerField cardRoleField : actorFieldsMap.values()) {
            cardRoleField.setEditable(editable);
            Action addUserGroupAction = cardRoleField.getAction("addUserGroup");
            if (addUserGroupAction != null) {
                addUserGroupAction.setVisible(editable);
            }
        }
    }

    protected void refreshFieldsWithRole(CardRole cardRole) {
        for (CardRole cr : actorFieldsMap.keySet()) {
            if (cr.getCode().equals(cardRole.getCode()) && !cr.equals(cardRole)) {
                LookupPickerField cardRoleField = actorFieldsMap.get(cr);
                CardRole role = tmpCardRolesDs.getItem(cr.getId());
                cardRoleField = initCardRoleField(role == null ? cr : role, cardRoleField.<User>getValue());
            }
        }
    }

    public void fillMissingRoles() {
        Set<String> requiredRolesCodes = getRequiredRolesCodes(false/*cardRolesDs.getItemIds().size() == 0*/);
        for (Object itemId : cardRolesDs.getItemIds()) {
            CardRole cardRole = cardRolesDs.getItem((UUID) itemId);
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

    protected boolean containsAnyRoleCode(String[] roleCodes) {
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
        Set<CardRole> cardRoles = new HashSet<>();
        for (UUID uuid : cardRolesDs.getItemIds()) {
            cardRoles.add(cardRolesDs.getItem(uuid));
        }
        return cardRolesFrameWorker.getEmptyRolesNames(card, cardRoles, requiredRolesCodesStr, deletedEmptyRoleCodes);
    }

    public void setDeletedEmptyRoleCodes(List deletedEmptyRoleCodes) {
        this.deletedEmptyRoleCodes = deletedEmptyRoleCodes;
    }

    protected Set<String> getRequiredRolesCodes(boolean isAll) {
        if (StringUtils.isNotEmpty(requiredRolesCodesStr)) {
            String[] s = requiredRolesCodesStr.split(isAll ? "\\s*[,|]\\s*" : "\\s*,\\s*");
            return new LinkedHashSet<>(Arrays.asList(s));
        }
        return Collections.emptySet();
    }

    public void setRequiredRolesCodesStr(String requiredRolesCodesStr) {
        this.requiredRolesCodesStr = requiredRolesCodesStr;
    }

    public static class CardProcRolesDatasource extends CollectionDatasourceImpl<CardRole, UUID> {

        protected CollectionDatasource<CardRole, UUID> cardRolesDs;
        private CollectionDatasource<CardRole, UUID> activeDs;
        private Map<UUID, CollectionDatasource<CardRole, UUID>> dsRegistry = new HashMap<>();
        public boolean fill;
        protected Set<String> visibleRoles;

        @Override
        public void setup(DsContext dsContext, DataSupplier dataSupplier, String id, MetaClass metaClass, @Nullable View view) {
            super.setup(dsContext, dataSupplier, id, metaClass, view);
            cardRolesDs = (CollectionDatasource<CardRole, UUID>) dsContext.get("cardRolesDs");
            this.activeDs = this.cardRolesDs;
        }

        public void setVisibleRoles(Set<String> visibleRoles) {
            this.visibleRoles = visibleRoles;
        }

        public Set<String> getVisibleRoles() {
            return visibleRoles;
        }

        @Override
        public void addItem(CardRole item) throws UnsupportedOperationException {
            if (CollectionUtils.isEmpty(visibleRoles) || visibleRoles.contains(item.getCode())) {
                beforeItemAdded(item);
                super.addItem(item);
                if (!fill)
                    cardRolesDs.addItem(item);
            }
        }

        protected void beforeItemAdded(CardRole item) {
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
                @Override
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

            fireCollectionChanged(Operation.REFRESH, Collections.emptyList());
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

        protected void removeAll() {
            List<UUID> items = new ArrayList<>(getItemIds());
            for (UUID id : items)
                removeItem(getItem(id));
        }

        protected CollectionDatasource<CardRole, UUID> getDsInternal(Card card) {
            CollectionDatasource<CardRole, UUID> ds = dsRegistry.get(card.getId());
            if (ds == null) {
                DsBuilder dsBuilder = DsBuilder.create(cardRolesDs.getDsContext())
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

    public LookupPickerField initCardRoleField(final CardRole cardRole, User value) {
        final CollectionDatasource<User, UUID> usersDs = createUserOptionsDs(cardRole);
        if (value != null) {
            Role role = cardRole.getProcRole().getRole();
            if (!usersDs.containsItem(value.getId()) && isInactiveRoleVisible)
                usersDs.includeItem(value);
            else if (usersDs.containsItem(value.getId()) &&
                    roleUsersMap.containsKey(role) &&
                    !roleUsersMap.get(role).contains(value)) {
                usersDs.excludeItem(value);
            }
        }
        LookupPickerField pickerField = componentsFactory.createComponent(LookupPickerField.class);
        pickerField.setOptionsDatasource(usersDs);
        pickerField.setValue(value);
        pickerField.setWidth("100%");

        pickerField.addValueChangeListener(getCardRoleFieldValueListener(cardRole));

        pickerField.setEditable(rolesTable.isEditable());

        if (cardRole.getProcRole().getMultiUser() && rolesTable.isEditable()) {
            Action addGroupAction = createAddGroupAction(cardRole);
            pickerField.addAction(addGroupAction);
        }

        return pickerField;
    }

    protected ValueChangeListener getCardRoleFieldValueListener(final CardRole cardRole) {
        return e -> {
            User selectedUser = (User) e.getValue();
            CardRole cr = tmpCardRolesDs.getItem(cardRole.getId());
            if (cr != null) {
                cr.setUser(selectedUser);
            } else {
                cardRole.setUser(selectedUser);
            }
        };
    }

    public void setCombinedStagesEnabled(boolean combinedStagesEnabled) {
        this.combinedStagesEnabled = combinedStagesEnabled;
    }

    public Map<CardRole, LookupPickerField> getActorFieldsMap() {
        return actorFieldsMap;
    }

    protected class AddUserGroupWindowCloseListener implements Window.CloseListener {

        protected Window window;
        protected CardRole cardRole;

        public AddUserGroupWindowCloseListener(Window window, CardRole cardRole) {
            this.window = window;
            this.cardRole = cardRole;
        }

        @Override
        public void windowClosed(String actionId) {
            if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                Set<User> validUsers = new HashSet<>();
                Set<User> invalidUsers = new HashSet<>();

                Set<User> selectedUsers = getSelectedUsers(cardRole, window);

                User oldUser = cardRole.getUser();
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
                    if (isNeedRemoveCardRole(cr, selectedUsers))
                        tmpCardRolesDs.removeItem(cr);
                }

                if (!validUsers.isEmpty()) {
                    if (oldUser == null) {
                        oldUser = validUsers.iterator().next();
                        tmpCardRolesDs.addItem(createCardRole(procRole, oldUser, true, true));
                        validUsers.remove(oldUser);
                    } else {
                        if (validUsers.contains(oldUser)) {
                            cardRole.setUser(oldUser);
                            actorFieldsMap.get(cardRole).setValue(oldUser);
                            validUsers.remove(oldUser);
                            tmpCardRolesDs.updateItem(cardRole);
                        }
                    }
                } else {
                    if (isNeedCreateEmptyCardRole(selectedUsers)) {
                        tmpCardRolesDs.addItem(createCardRole(procRole, null, true, true));
                    }
                }
                List<CardRole> cardRolesToAdd = createCardRoles(validUsers, procRole, code);

                if (!invalidUsers.isEmpty()) {
                    String usersList = buildInvalidUserString(invalidUsers);
                    String invalidUsersMessage;
                    if (invalidUsers.size() == 1)
                        invalidUsersMessage = messages.formatMessage(getClass(), "invalidUser.message", usersList, cardRole.getProcRole().getName());
                    else
                        invalidUsersMessage = messages.formatMessage(getClass(), "invalidUsers.message", usersList, cardRole.getProcRole().getName());

                    showNotification("", invalidUsersMessage, Frame.NotificationType.WARNING);
                }
            }
        }

        protected String buildInvalidUserString(Set<User> invalidUsers) {
            String usersList = "";
            for (User user : invalidUsers) {
                usersList += user.getName() + ", ";
            }
            return usersList.substring(0, usersList.length() - 2);
        }

        protected boolean isNeedRemoveCardRole(CardRole cr, Set<User> selectedUsers) {
            return cr.getCode().equals(cardRole.getCode()) && cr.getProcRole().getMultiUser()
                    && !selectedUsers.contains(cr.getUser());
        }

        protected boolean isNeedCreateEmptyCardRole(Set<User> selectedUsers) {
            return selectedUsers.size() == 0;
        }
    }

    public void setInactiveRoleVisible(boolean isInactiveRoleVisible) {
        this.isInactiveRoleVisible = isInactiveRoleVisible;
    }

    protected Collection<CardRole> getTmpCardRoles() {
        return new ArrayList<>(tmpCardRolesDs.fill ? tmpCardRolesDs.activeDs.getItems() : tmpCardRolesDs.getItems());
    }

    /**
     * This is obsolete method.<br/>
     * Use injected {@link CardRolesFrameWorker} interface.
     */
    @Deprecated
    protected void assignNextSortOrder(CardRole cr) {
        cardRolesFrameWorker.assignNextSortOrder(cr, getTmpCardRoles());
    }

    /**
     * This is obsolete method.<br/>
     * Use injected {@link CardRolesFrameWorker} interface.
     */
    @Deprecated
    protected void normalizeSortOrders() {
        cardRolesFrameWorker.normalizeSortOrders(currentProcess, combinedStagesEnabled,
                procRolesDs.getItems(), getTmpCardRoles());
    }
}
