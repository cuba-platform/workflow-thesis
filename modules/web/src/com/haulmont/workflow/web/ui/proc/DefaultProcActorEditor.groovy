/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 26.02.2010 11:25:23
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.proc

import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.gui.components.AbstractEditor
import com.haulmont.cuba.gui.data.CollectionDatasource
import com.haulmont.cuba.security.entity.Role
import com.haulmont.workflow.core.entity.DefaultProcActor
import com.haulmont.cuba.gui.components.IFrame
import com.haulmont.cuba.gui.components.LookupField
import com.haulmont.cuba.gui.data.ValueListener

class DefaultProcActorEditor extends AbstractEditor{

  CollectionDatasource usersDs = getDsContext().get("usersDs")
  List<UUID> userIds;
  boolean isMulti

  public DefaultProcActorEditor(IFrame frame) {
    super(frame)
  }

    public void init(Map<String, Object> params) {
    super.init(params);
    userIds = params.get("userIds");
    if (userIds == null)
      userIds = new ArrayList<UUID>();
    isMulti = params["isMulti"]
  }

  public void setItem(Entity item) {
    super.setItem(item);
    DefaultProcActor dpa = (DefaultProcActor)getItem()
    Role secRole = dpa.procRole.role
    if (secRole) {
      usersDs.setQuery('select u from sec$User u join u.userRoles ur where ur.role.id = :custom$secRole ' +
              'and u.id not in (:custom$userIds) order by u.name ' +
              'and u.active = true ')
    } else {
      usersDs.setQuery('select u from sec$User u where u.id not in (:custom$userIds) and u.active = true order by u.name')
    }
    usersDs.refresh(['secRole': secRole, 'userIds':userIds])
    if (isMulti)
      initDpaSortOrder()
  }

  private void initDpaSortOrder() {
      def orderValues = []
      for (int i = 1; i <= userIds.size() + 1; i++)
          orderValues += i
      LookupField sortOrderField = getComponent("sortOrderField")
      sortOrderField.optionsList = orderValues
      sortOrderField.value = item.sortOrder
      sortOrderField.addListener(
              {Object source, String property, Object prevValue, Object value ->
                  item.sortOrder = value
              } as ValueListener)
  }
}
