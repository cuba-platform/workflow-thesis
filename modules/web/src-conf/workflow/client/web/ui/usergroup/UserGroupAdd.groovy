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

import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.core.global.MessageProvider
import com.haulmont.cuba.gui.AppConfig
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl
import com.haulmont.cuba.security.entity.User
import com.haulmont.cuba.web.gui.components.WebComponentsHelper
import com.haulmont.cuba.web.toolkit.ui.TwinColumnSelect
import com.haulmont.cuba.web.toolkit.ui.TwinColumnSelect.OptionStyleGenerator
import com.haulmont.workflow.core.entity.UserGroup
import com.vaadin.ui.AbstractSelect
import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.security.entity.Role

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

    Role secRole = params.get("secRole");
    if (secRole) userGroupsDs.refresh(['secRole' : secRole])

    TwinColumnSelect twinColumnSelect = (TwinColumnSelect)WebComponentsHelper.unwrap(twinColumn)
    twinColumnSelect.setStyleGenerator([
      generateStyle : {AbstractSelect source, Object itemId, boolean selected ->
        Object currentItem = userGroupsDs.getItem(itemId)
        if (currentItem instanceof UserGroup) return 'marked'
        return '';
      }
      ] as OptionStyleGenerator)

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
