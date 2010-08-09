/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 06.08.2010 14:13:21
 *
 * $Id$
 */
 package workflow.client.web.ui.usergroup

import com.haulmont.workflow.web.ui.base.action.AbstractForm
import com.haulmont.cuba.gui.components.AbstractWindow
import com.haulmont.cuba.gui.components.IFrame
import com.haulmont.cuba.gui.components.AbstractAction
import com.haulmont.cuba.gui.components.Component
import com.haulmont.cuba.core.global.MessageProvider
import com.haulmont.cuba.gui.AppConfig
import com.haulmont.cuba.gui.components.TwinColumn
import com.haulmont.workflow.core.entity.UserGroup
import com.haulmont.workflow.core.entity.ProcRole
import com.haulmont.workflow.core.entity.CardRole
import com.haulmont.cuba.security.entity.Role
import com.haulmont.cuba.security.entity.User
import com.haulmont.cuba.web.gui.components.WebComponentsHelper
import com.vaadin.ui.Window
import com.haulmont.cuba.gui.settings.Settings
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter
import com.haulmont.cuba.gui.data.Datasource
import com.vaadin.terminal.Resource
import com.vaadin.terminal.ThemeResource
import com.haulmont.cuba.core.entity.Entity

class UserGroupAdd extends AbstractWindow{
  private TwinColumn twinColumn
  private CollectionDatasourceImpl userGroupsDs
  private Set selectedUsers = []

  def UserGroupAdd(IFrame frame) {
    super(frame);
  }

  protected void init(Map<String, Object> params) {
    super.init(params);
    twinColumn = getComponent('twinColumn')
    userGroupsDs = getDsContext().get('userGroupsDs')
//    userGroupsDs.addListener([
//            stateChanged : {Datasource ds, Datasource.State prevState, Datasource.State state ->
//              if (state == Datasource.State.VALID) {
//                setUserGroupsIcons()
//              }
//            }
//    ] as DsListenerAdapter)

    addAction(new AbstractAction("windowCommit") {
        public void actionPerform(Component component) {
           Set values = (Set)twinColumn.getValue()
           processSelectedItems(values)
           close(COMMIT_ACTION_ID, true);
        }

        @Override
        public String getCaption() {
            return MessageProvider.getMessage(AppConfig.getInstance().getMessagesPack(), "actions.Ok");
        }
    });

    addAction(new AbstractAction("windowClose") {
        public void actionPerform(Component component) {
            close("cancel");
        }

        @Override
        public String getCaption() {
            return MessageProvider.getMessage(AppConfig.getInstance().getMessagesPack(), "actions.Cancel");
        }
    });
  }

//  private void setUserGroupsIcons() {
//    List userGroupDsItems = userGroupsDs.getItemIds().collect{itemId -> userGroupsDs.getItem(itemId)}
//    com.vaadin.ui.TwinColSelect vTwinColumn = (com.vaadin.ui.TwinColSelect)WebComponentsHelper.unwrap(twinColumn)
////    Resource userGroupIcon = new ThemeResource("icons/userGroup.png");
//    String groupCaption = getMessage('group')
//    userGroupDsItems.each{item ->
//      if (item instanceof UserGroup) {
////        vTwinColumn.setItemIcon(item.getId(), userGroupIcon)
//        vTwinColumn.setItemCaption(item.id, vTwinColumn.getItemCaption(item.id) + ' ' + groupCaption)
//      }
//    }
//  }

  private void processSelectedItems(Set selectedItems) {
    Set userGroups = selectedItems.findAll{Entity item -> item instanceof UserGroup}
    selectedItems.removeAll(userGroups)
    userGroups.each{UserGroup userGroup ->
      userGroup.users.each{user -> selectedUsers << user}
    }
    selectedUsers.addAll(selectedItems)
  }

  public Set<User> getSelectedUsers() {
    return selectedUsers
  }

}
