/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 30.07.2010 13:20:06
 *
 * $Id$
 */
package workflow.client.web.ui.usergroup

import com.haulmont.cuba.gui.components.AbstractWindow
import com.haulmont.cuba.gui.components.IFrame
import com.haulmont.cuba.gui.components.Table
import com.haulmont.cuba.gui.components.TableActionsHelper
import com.haulmont.cuba.gui.WindowManager
import com.haulmont.cuba.gui.components.ActionAdapter
import com.haulmont.cuba.gui.components.Window.Lookup.Handler
import com.haulmont.cuba.gui.data.CollectionDatasource

import com.haulmont.workflow.core.entity.UserGroup

import com.haulmont.cuba.security.entity.User
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.web.App
import com.haulmont.cuba.web.app.ui.security.user.browse.UserBrowser
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter

class UserGroupBrowser extends AbstractWindow{
  private Table userGroupsTable
  private Table usersTable

  def UserGroupBrowser(IFrame frame) {
    super(frame);
  }

  protected void init(Map<String, Object> params) {
    super.init(params);

    userGroupsTable = getComponent('userGroupsTable')
    TableActionsHelper userGroupsHelper = [this, userGroupsTable]
    userGroupsHelper.createCreateAction(WindowManager.OpenType.DIALOG)
    userGroupsHelper.createEditAction(WindowManager.OpenType.DIALOG)
    userGroupsHelper.createRemoveAction()

    CollectionDatasource userGroupsDs = getDsContext().get('userGroupsDs')
    CollectionDatasource usersDs = getDsContext().get('usersDs')

    userGroupsDs.addListener([collectionChanged:{ ds, operation ->
        usersDs.refresh()
      }] as CollectionDsListenerAdapter);

    usersTable = getComponent('usersTable')
    usersTable.addAction(new ActionAdapter("add", [
            actionPerform : {
              if (userGroupsTable.getSelected().size() == 0) {
                showNotification(getMessage('selectUserGroup.msg'), IFrame.NotificationType.HUMANIZED)
                return
              }
              Map<String, Object> userBrowserParams = ['multiselect': 'true']
              openLookup('sec$User.browse',
              [
                      handleLookup : {Collection<User> items ->
                        UserGroup userGroup = userGroupsDs.getItem()
                        items.each{user ->
                          userGroup.users << user
                        }
                        CommitContext ctx = new CommitContext()
                        ctx.commitInstances << userGroup
                        Map commited = getDsContext().getDataService().commit(ctx)
                        usersDs.refresh()
                        userGroupsDs.updateItem(commited[userGroup])
                        userGroupsDs.setItem(commited[userGroup])
                      }
              ] as Handler,
              WindowManager.OpenType.THIS_TAB,
              userBrowserParams)
            },
            getCaption : {
              return getMessage('actions.Add')
            }
    ]))

    usersTable.addAction(new ActionAdapter("remove", [
            actionPerform: {
              User selectedUser = null;
              Set selected = usersTable.getSelected()
              if (selected.size() == 1) {
                selectedUser = selected.iterator().next()
              }

              if (!selectedUser) {
                showNotification(getMessage('selectUser.msg'), IFrame.NotificationType.HUMANIZED)
                return
              }
              WindowManager wm = App.getInstance().getWindowManager()
              wm.showOptionDialog(
                      getMessage('deleteFromGroup.dialogHeader'),
                      getMessage('deleteFromGroup.dialogMessage'),
                      IFrame.MessageType.CONFIRMATION,
                      [
                              new ActionAdapter('ok', [
                                      actionPerform: {
                                        UserGroup userGroup = userGroupsDs.getItem(userGroupsDs.getItem().getId())
                                        userGroup.users.remove(selectedUser)
                                        CommitContext ctx = new CommitContext()
                                        ctx.getCommitInstances().add(userGroup)
                                        Map commited = getDsContext().getDataService().commit(ctx)
                                        usersDs.refresh()
                                        userGroupsDs.updateItem(commited[userGroup])
                                        userGroupsDs.setItem(commited[userGroup])
                                      },
                                      getIcon : {
                                        return "icons/ok.png"
                                      },
                                      getCaption : {
                                         return getMessage('actions.Yes')
                                      }
                              ]),
                              new ActionAdapter('cancel', [
                                      getIcon : {
                                        return "icons/cancel.png"
                                      },
                                      getCaption : {
                                         return getMessage('actions.No')
                                      }
                              ])

                      ] as ActionAdapter[])

            },
            getCaption: {
              return getMessage('actions.Remove')
            }
    ]))

//    CollectionDatasource userGroupsDs = getDsContext().get('userGroupsDs')
//    userGroupsDs.addListener([
//            itemChanged : {Datasource<UserGroup> ds, UserGroup prevItem, UserGroup item ->
//              if (!item) return
//              userGroupsDs.refresh(['userGroup' : item])
//            }
//    ] as DsListenerAdapter)
  }


}
