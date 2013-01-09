/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 30.07.2010 13:20:06
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.usergroup

import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.UserSessionProvider
import com.haulmont.cuba.gui.WindowManager
import com.haulmont.cuba.gui.components.Window.Lookup.Handler
import com.haulmont.cuba.gui.data.CollectionDatasource
import com.haulmont.cuba.gui.data.Datasource
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter
import com.haulmont.cuba.security.entity.EntityOp
import com.haulmont.cuba.security.entity.User
import com.haulmont.workflow.core.entity.UserGroup

import javax.inject.Inject

import com.haulmont.cuba.gui.components.*

class UserGroupBrowser extends AbstractWindow {
    protected Table userGroupsTable
    protected Table usersTable

    @Inject
    protected CollectionDatasource usersDs

    @Inject
    protected CollectionDatasource userGroupsDs

    def UserGroupBrowser(IFrame frame) {
        super(frame);
    }

    public void init(Map<String, Object> params) {
        super.init(params);

        userGroupsTable = getComponent('userGroupsTable')
        TableActionsHelper userGroupsHelper = [this, userGroupsTable]
        userGroupsHelper.createCreateAction(
                createValueProvider(),
                WindowManager.OpenType.DIALOG)
        userGroupsHelper.createEditAction(WindowManager.OpenType.DIALOG)
        userGroupsHelper.createRemoveAction()

        userGroupsDs.addListener([collectionChanged: { ds, operation ->
            usersDs.refresh()
        }] as CollectionDsListenerAdapter);

        usersTable = getComponent('usersTable')
        final userSession = UserSessionProvider.getUserSession();
        usersTable.addAction(new ActionAdapter("add", [
                actionPerform: {
                    if (userGroupsTable.getSelected().size() == 0) {
                        showNotification(getMessage('selectUserGroup.msg'), IFrame.NotificationType.HUMANIZED)
                        return
                    }
                    Map<String, Object> userBrowserParams = ['multiSelect': 'true']
                    openLookup('sec$User.lookup',
                            [
                                    handleLookup: {Collection<User> items ->
                                        UserGroup userGroup = userGroupsDs.getItem()
                                        if (userGroup.users == null) {
                                            boolean modified = userGroupsDs.modified
                                            userGroup.users = new HashSet<UserGroup>();
                                            userGroupsDs.modified = modified
                                        }
                                        items.each {user ->
                                            userGroup.users << user
                                        }
                                        CommitContext ctx = new CommitContext()
                                        ctx.commitInstances << userGroup
                                        Set commited = getDsContext().getDataService().commit(ctx)
                                        usersDs.refresh()
                                        userGroup = commited.iterator().next();
                                        userGroupsDs.updateItem(userGroup)
                                        userGroupsDs.setItem(userGroup)
                                    }
                            ] as Handler,
                            WindowManager.OpenType.THIS_TAB,
                            userBrowserParams)
                },
                getCaption: {
                    return getMessage('actions.Add')
                }
        ]))

        usersTable.addAction(new DeleteUserAction())

        userGroupsDs.addListener([
                itemChanged: {Datasource<UserGroup> ds, UserGroup prevItem, UserGroup item ->
                    if (item) {
                        boolean isEnabled = getUserGroupEditable(item)
                        userGroupsTable.getAction('edit').enabled = isEnabled
                        userGroupsTable.getAction('remove').enabled = isEnabled
                        usersTable.getAction('add').enabled = isEnabled &&
                                userSession.isEntityOpPermitted(userGroupsTable.getDatasource().getMetaClass(), EntityOp.CREATE)
                        usersTable.getAction('remove').enabled = isEnabled &&
                                userSession.isEntityOpPermitted(userGroupsTable.getDatasource().getMetaClass(), EntityOp.DELETE)
                    }
                }
        ] as DsListenerAdapter)
    }

    protected boolean getUserGroupEditable(UserGroup item) {
        return UserSessionProvider.userSession.roles.contains('Administrators') ||
                UserSessionProvider.userSession.currentOrSubstitutedUser.equals(item.substitutedCreator)
    }

    protected ValueProvider createValueProvider() {
        return new ValueProvider() {
            @Override
            Map<String, Object> getValues() {
                return ['substitutedCreator': UserSessionProvider.userSession.currentOrSubstitutedUser]
            }

            @Override
            Map<String, Object> getParameters() {
                return null
            }
        };
    }


    private class DeleteUserAction extends AbstractAction {

        public DeleteUserAction() {
            super("remove")
        }

        @Override
        public void actionPerform(Component component) {
            User selectedUser = null;
            Set selected = usersTable.getSelected()
            if (selected.size() == 1) {
                selectedUser = selected.iterator().next()
            }

            if (!selectedUser) {
                showNotification(getMessage('selectUser.msg'), IFrame.NotificationType.HUMANIZED)
                return
            }
            showOptionDialog(
                    getMessage('deleteFromGroup.dialogHeader'),
                    getMessage('deleteFromGroup.dialogMessage'),
                    IFrame.MessageType.CONFIRMATION,
                    [
                            new DialogAction(DialogAction.Type.YES) {
                                @Override
                                public void actionPerform(Component comp) {
                                    CollectionDatasource userGroupsDs = getDsContext().get("userGroupsDs")
                                    UserGroup userGroup = userGroupsDs.getItem()
                                    userGroup.users.remove(selectedUser)
                                    CommitContext ctx = new CommitContext()
                                    ctx.getCommitInstances().add(userGroup)
                                    Set commited = getDsContext().getDataService().commit(ctx)
                                    CollectionDatasource usersDs = getDsContext().get("usersDs")
                                    usersDs.refresh()
                                    userGroup = commited.iterator().next();
                                    userGroupsDs.updateItem(userGroup)
                                    userGroupsDs.setItem(userGroup)
                                }
                            },
                            new DialogAction(DialogAction.Type.NO)
                    ]
            )
        }

        @Override
        public String getCaption() {
            return getMessage('actions.Remove')
        }
    }
}
