/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 19.01.2010 15:43:09
 *
 * $Id$
 */
package workflow.client.web.ui.card

import com.haulmont.cuba.gui.components.AbstractEditor
import com.haulmont.cuba.gui.components.IFrame
import com.haulmont.cuba.core.entity.Entity
import com.haulmont.workflow.core.entity.ProcRole
import com.haulmont.cuba.gui.components.LookupField
import com.haulmont.cuba.security.entity.Role
import com.haulmont.cuba.gui.data.CollectionDatasource
import com.haulmont.cuba.gui.data.ValueListener
import com.haulmont.workflow.core.entity.CardRole
import com.haulmont.cuba.core.global.LoadContext
import com.haulmont.cuba.gui.ServiceLocator

class CardRoleEditor extends AbstractEditor{
  private ProcRole procRole
  private CollectionDatasource usersDs
  private Set<UUID> users;

  CardRoleEditor(IFrame frame) {
    super(frame);
  }

  protected void init(Map<String, Object> params) {
    super.init(params);
    
    procRole = params['param$procRole']
    users = params['param$users']
    usersDs = getDsContext().get('usersDs')
    
    LookupField roleLookup = getComponent('roleLookup')
    roleLookup.addListener([
            valueChanged: {Object source, String property, Object prevValue, Object value ->
              if (value == null) return;
              ProcRole procRole = (ProcRole) value
              initUserLookup(procRole)
            }
    ] as ValueListener)
  }

  void setItem(Entity item) {
    super.setItem(item);
    LookupField roleLookup = getComponent("roleLookup")
    roleLookup.setEnabled(false)
    roleLookup.value = procRole

    CardRole cardRole = (CardRole)getItem()
    if (!procRole){
      LoadContext ctx = new LoadContext(ProcRole.class).setId(cardRole.procRole.id).setView('browse')
      ProcRole loadedProcRole = ServiceLocator.getDataService().load(ctx)
      initUserLookup(loadedProcRole)
    }
  }

  private void initUserLookup(ProcRole procRole) {
    Role secRole = procRole.role
    String usersExclStr = ''
    if (users && !users.isEmpty()) {
      usersExclStr = ' u.id not in (:custom$users) '
    }
    if (secRole) {
      usersDs.setQuery('select u from sec$User u join u.userRoles ur where ur.role.id = :custom$secRole' +
              (usersExclStr.isEmpty() ? ' ' : ' and' + usersExclStr) + 'order by u.name')
    } else {
      usersDs.setQuery('select u from sec$User u' +
              (usersExclStr.isEmpty() ? ' ' : ' where' + usersExclStr) + 'order by u.name')
    }
    usersDs.refresh(['secRole': secRole, 'users': users])
  }

}
