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

class DefaultProcActorEditor extends AbstractEditor{

  CollectionDatasource usersDs = getDsContext().get("usersDs")

  public DefaultProcActorEditor(IFrame frame) {
    super(frame)
  }

  public void setItem(Entity item) {
    super.setItem(item);
    DefaultProcActor dpa = (DefaultProcActor)getItem()
    Role secRole = dpa.procRole.role
    if (secRole) {
      usersDs.setQuery('select u from sec$User u join u.userRoles ur where ur.role.id = :custom$secRole order by u.name')
    } else {
      usersDs.setQuery('select u from sec$User u order by u.name')
    }
    usersDs.refresh(['secRole': secRole])
  }


}
