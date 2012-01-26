/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 06.08.2010 14:13:21
 *
 * $Id$
 */
 package com.haulmont.workflow.web.ui.usergroup

import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.core.global.MessageProvider
import com.haulmont.cuba.gui.AppConfig
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl
import com.haulmont.cuba.security.entity.User

import com.haulmont.workflow.core.entity.UserGroup

import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.security.entity.Role
import com.haulmont.cuba.gui.components.TwinColumn.StyleProvider
import javax.inject.Inject

class UserGroupAdd extends AbstractWindow{
  private TwinColumn twinColumn
  private CollectionDatasourceImpl userGroupsDs
  private Set selectedUsers = []
  protected Role secRole

  @Inject
  private Button searchButton;

  @Inject
  private TextField searchText;

  def UserGroupAdd(IFrame frame) {
    super(frame);
  }

    public void init(Map<String, Object> params) {
    super.init(params);
    twinColumn = getComponent('twinColumn')
    userGroupsDs = getDsContext().get('userGroupsDs')

    secRole = params.get("secRole");
    if (secRole) userGroupsDs.refresh(['secRole' : secRole])

    twinColumn.styleProvider = [
            getItemIcon: {Entity item, boolean selected ->
              if (item instanceof UserGroup) return 'theme:icons/user-group-small.png'
              return null
            },
            getStyleName : {Entity item, Object property, boolean selected -> return null}
    ] as StyleProvider

    java.util.List<User> users = (java.util.List<User>) params.get("Users")
    if (users != null)
      twinColumn.setValue(users)

    addAction(new AbstractAction("windowCommit") {
        public void actionPerform(Component component) {
           Set values = (Set)twinColumn.getValue()
           processSelectedItems(values)
           close(COMMIT_ACTION_ID, true);
        }

        @Override
        public String getCaption() {
            return MessageProvider.getMessage(AppConfig.getMessagesPack(), "actions.Ok");
        }
    });

    addAction(new AbstractAction("windowClose") {
        public void actionPerform(Component component) {
            close("cancel");
        }

        @Override
        public String getCaption() {
            return MessageProvider.getMessage(AppConfig.getMessagesPack(), "actions.Cancel");
        }
    });

    searchButton.setAction(new AbstractAction("search") {
        public void actionPerform(Component component) {
            HashSet selectedItems = twinColumn.getValue();
            String requiredText = searchText.getValue();
            userGroupsDs.refresh(['selectedItems': selectedItems, 'requiredText': requiredText, 'secRole': secRole])
            twinColumn.setValue(selectedItems);
        }

        @Override
        public String getCaption() {
            return getMessage("actions.Apply");
        }
    });
  }

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
