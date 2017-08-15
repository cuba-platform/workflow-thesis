/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.usergroup;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.DialogAction.Type;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.gui.theme.ThemeConstantsManager;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.workflow.core.entity.UserGroup;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

/**
 * @author Gorbunkov
 * @version $Id$
 */
public class UserGroupBrowser extends AbstractWindow {

    @Inject
    protected Table usersTable;

    @Inject
    protected Table userGroupsTable;

    @Inject
    protected CollectionDatasource usersDs;

    @Inject
    protected CollectionDatasource<UserGroup, UUID> userGroupsDs;

    @Inject
    protected UserSession userSession;

    @Inject
    protected Security security;

    protected ClientConfig clientConfig;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        clientConfig = AppBeans.get(Configuration.class).getConfig(ClientConfig.class);
        CreateAction createAction = new CreateAction(userGroupsTable, WindowManager.OpenType.DIALOG);
        Map<String, Object> initialValues = new HashMap<>();
        initialValues.put("substitutedCreator", userSession.getCurrentOrSubstitutedUser());
        createAction.setInitialValues(initialValues);
        userGroupsTable.addAction(createAction);

        userGroupsTable.addAction(new EditAction(userGroupsTable, WindowManager.OpenType.DIALOG) {
            @Override
            protected void afterCommit(Entity entity) {
                DataService dataService = AppBeans.get(DataService.NAME);

                LoadContext loadContext = new LoadContext(entity.getMetaClass())
                        .setId(entity.getId())
                        .setView(userGroupsDs.getView());
                UserGroup committedGroup = dataService.load(loadContext);
                if (committedGroup != null) {
                    userGroupsDs.updateItem(committedGroup);
                } else {
                    userGroupsDs.refresh();
                }
            }
        });

        userGroupsTable.addAction(new RemoveAction(userGroupsTable));

        userGroupsDs.addListener(new CollectionDsListenerAdapter<UserGroup>() {
            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation, List items) {
                usersDs.refresh();
            }

            @Override
            public void itemChanged(Datasource<UserGroup> ds, @Nullable UserGroup prevItem, @Nullable UserGroup item) {
                if (item != null) {
                    boolean isEnabled = getUserGroupEditable(item);
                    userGroupsTable.getAction("edit").setEnabled(isEnabled);
                    userGroupsTable.getAction("remove").setEnabled(isEnabled);

                    MetaClass metaClass = userGroupsTable.getDatasource().getMetaClass();

                    usersTable.getAction("add").setEnabled(isEnabled &&
                            security.isEntityOpPermitted(metaClass, EntityOp.CREATE));
                    usersTable.getAction("remove").setEnabled(isEnabled &&
                            security.isEntityOpPermitted(metaClass, EntityOp.DELETE));
                }
            }
        });

        usersTable.addAction(new AbstractAction("add", clientConfig.getTableInsertShortcut()) {

            {
                ThemeConstantsManager thCM = AppBeans.get(ThemeConstantsManager.NAME);
                icon = thCM.getThemeValue("actions.Add.icon");
            }

            @Override
            public void actionPerform(Component component) {
                if (userGroupsTable.getSelected().size() == 0) {
                    showNotification(getMessage("selectUserGroup.msg"), IFrame.NotificationType.HUMANIZED);
                    return;
                }

                Map<String, Object> userBrowserParams = new HashMap<>();
                WindowParams.MULTI_SELECT.set(userBrowserParams, true);

                openLookup("sec$User.lookup", new Lookup.Handler() {
                    @Override
                    public void handleLookup(Collection items) {
                        addUsers(items);
                    }
                }, WindowManager.OpenType.THIS_TAB, userBrowserParams);
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage("actions.Add");
            }
        });

        usersTable.addAction(new DeleteUserAction());
    }

    protected void addUsers(Collection selectedUsers) {
        UserGroup userGroup = userGroupsDs.getItem();
        Set<User> users = userGroup.getUsers();
        if (CollectionUtils.isEmpty(users)) {
            users = new HashSet<>();
        }
        for (Object user : selectedUsers) {
            users.add((User) user);
        }
        userGroup.setUsers(users);
        CommitContext ctx = new CommitContext();
        ctx.getCommitInstances().add(userGroup);
        getDsContext().getDataSupplier().commit(ctx);
        userGroupsDs.refresh();
    }


    protected boolean getUserGroupEditable(UserGroup item) {
        return userSession.getRoles().contains("Administrators") ||
                userSession.getCurrentOrSubstitutedUser().equals(item.getSubstitutedCreator());
    }

    private class DeleteUserAction extends AbstractAction {

        public DeleteUserAction() {
            super("remove");
            setShortcut(clientConfig.getTableRemoveShortcut());

            ThemeConstantsManager thCM = AppBeans.get(ThemeConstantsManager.NAME);
            icon = thCM.getThemeValue("actions.Remove.icon");
        }

        @Override
        public void actionPerform(Component component) {
            User selectedUser = null;
            Set selected = usersTable.getSelected();
            if (selected.size() == 1) {
                selectedUser = (User) selected.iterator().next();
            }

            if (selectedUser == null) {
                showNotification(getMessage("selectUser.msg"), NotificationType.HUMANIZED);
                return;
            }
            final User finalSelectedUser = selectedUser;
            showOptionDialog(
                    getMessage("deleteFromGroup.dialogHeader"),
                    getMessage("deleteFromGroup.dialogMessage"),
                    MessageType.CONFIRMATION,
                    new Action[]{
                            new DialogAction(Type.YES) {
                                @Override
                                public void actionPerform(Component component) {
                                    removeUser(finalSelectedUser);
                                }
                            },
                            new DialogAction(Type.NO, Status.PRIMARY)
                    });
        }

        @Override
        public String getCaption() {
            return messages.getMainMessage("actions.Remove");
        }
    }

    protected void removeUser(User user) {
        UserGroup userGroup = userGroupsDs.getItem();
        userGroup.getUsers().remove(user);
        CommitContext ctx = new CommitContext();
        ctx.getCommitInstances().add(userGroup);
        Set commited = getDsContext().getDataSupplier().commit(ctx);
        usersDs.refresh();
        userGroup = (UserGroup) commited.iterator().next();
        userGroupsDs.updateItem(userGroup);
        userGroupsDs.setItem(userGroup);
    }
}