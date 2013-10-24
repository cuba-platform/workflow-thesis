/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.ui.proc

import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.gui.components.AbstractEditor
import com.haulmont.cuba.gui.components.LookupField
import com.haulmont.cuba.gui.data.CollectionDatasource
import com.haulmont.cuba.gui.data.ValueListener
import com.haulmont.cuba.security.entity.Role
import com.haulmont.workflow.core.entity.DefaultProcActor

/**
 * @author gorbunkov
 * @version $Id$
 */
class DefaultProcActorEditor extends AbstractEditor{

  CollectionDatasource usersDs
  List<UUID> userIds;
  boolean isMulti

  public void init(Map<String, Object> params) {
    super.init(params);
    usersDs = getDsContext().get("usersDs")
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
              'and u.id not in (:custom$userIds) ' +
              'and u.active = true order by u.name')
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
      if (sortOrderField != null) {
          sortOrderField.optionsList = orderValues
          sortOrderField.value = item.sortOrder
          sortOrderField.addListener(
                  {Object source, String property, Object prevValue, Object value ->
                      item.sortOrder = value
                  } as ValueListener)
      }
  }
}
