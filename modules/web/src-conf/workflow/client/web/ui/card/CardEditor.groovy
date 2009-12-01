/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 25.11.2009 11:43:34
 *
 * $Id$
 */
package workflow.client.web.ui.card

import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.core.entity.FileDescriptor
import com.haulmont.cuba.core.global.PersistenceHelper
import com.haulmont.cuba.gui.WindowManager
import com.haulmont.cuba.gui.WindowManager.OpenType
import com.haulmont.cuba.gui.data.CollectionDatasource
import com.haulmont.cuba.gui.data.CollectionDatasourceListener
import com.haulmont.cuba.gui.data.CollectionDatasourceListener.Operation
import com.haulmont.cuba.gui.data.Datasource
import com.haulmont.cuba.gui.data.ValueListener
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter
import com.haulmont.cuba.web.app.FileDownloadHelper
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.ProcRole
import java.util.List
import workflow.client.web.ui.actions.ActionsFrame
import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.security.entity.User
import com.haulmont.workflow.core.entity.CardRole

public class CardEditor extends AbstractEditor {

  private Datasource<Card> cardDs
  private CollectionDatasource<CardRole, UUID> rolesDs
  private CollectionDatasource<ProcRole, UUID> lookupRoleDs
  private Table attachmentsTable
  private List rolesActions
  private LookupField roleCreateLookup

  private String createRoleCaption

  def CardEditor(IFrame frame) {
    super(frame);
  }

  protected void init(Map<String, Object> params) {
    super.init(params);

    createRoleCaption = getMessage('createRoleCaption')
    cardDs = getDsContext().get('cardDs')
    rolesDs = getDsContext().get('rolesDs')
    lookupRoleDs = getDsContext().get('lookupRoleDs')
    attachmentsTable = getComponent('attachmentsTable')
    roleCreateLookup = getComponent('createRoleLookup')
    rolesActions = [roleCreateLookup, getComponent('editRole'), getComponent('removeRole')]

    TableActionsHelper attachmentsTH = new TableActionsHelper(this, attachmentsTable)
    attachmentsTH.createCreateAction([
            getParameters: { [:] },
            getValues: { ['card': cardDs.getItem(), 'file': new FileDescriptor()] }
    ] as ValueProvider, WindowManager.OpenType.DIALOG)
    attachmentsTH.createEditAction(WindowManager.OpenType.DIALOG)
    attachmentsTH.createRemoveAction(false)

    LookupField procLookup = getComponent('proc')
    procLookup.addListener({ Object source, String property, Object prevValue, Object value ->
      if (value == null)
        enableRolesChange(false)
      else
        enableRolesChange(true)
      lookupRoleDs.refresh([procId: value])
      initRoleCreateLookup()
    } as ValueListener)

    Table rolesTable = getComponent('rolesTable')
    TableActionsHelper rolesTH = new TableActionsHelper(this, rolesTable)
    rolesTH.createCreateAction([
            getParameters: { ['proc': procLookup.getValue()] },
            getValues: { ['card': cardDs.getItem()] }
    ] as ValueProvider, WindowManager.OpenType.DIALOG)
    rolesTH.createEditAction(WindowManager.OpenType.DIALOG)
    rolesTH.createRemoveAction(false)

    rolesDs.addListener([collectionChanged: { CollectionDatasource ds, Operation operation ->
      procLookup.setEnabled(ds.getItemIds().isEmpty())
      initRoleCreateLookup()
    }] as CollectionDsListenerAdapter)

    roleCreateLookup.addListener({Object source, String property, Object prevValue, Object value ->
      if (value && value != createRoleCaption) {
        def lookupHandler = { Collection items ->
          if (!items.isEmpty()) {
            User user = items.iterator().next()
            CardRole cr = new CardRole()
            cr.setProcRole(value)
            cr.setUser(user)
            cr.setCard(getItem())
            rolesDs.addItem(cr) 
          }
        }
        openLookup('sec$User.browse', lookupHandler as Window.Lookup.Handler, WindowManager.OpenType.THIS_TAB)

        roleCreateLookup.setValue(null) 
      }
    } as ValueListener)
  }

  public void setItem(Entity item) {
    super.setItem(item);

    enableRolesChange(false)
    FileDownloadHelper.initGeneratedColumn(attachmentsTable, 'file');

    ActionsFrame actionsFrame = getComponent('actions')

    if (PersistenceHelper.isNew(item)) {
      cardDs.addListener([
              valueChanged: {
                Object source, String property, Object prevValue, Object value ->
                if (property == 'proc') {
                  actionsFrame.initActions(source)
                }
              }
      ] as DsListenerAdapter)
    } else {
      actionsFrame.initActions(item)
    }
  }

  private void enableRolesChange(boolean enable) {
    rolesActions.each { Component comp -> comp.setEnabled(enable) }
  }

  private void initRoleCreateLookup() {
    // add ProcRole if it has multiUser == true or not added yet
    List items = getDsItems(lookupRoleDs)
            .findAll { ProcRole pr -> 
              pr.getMultiUser() || !getDsItems(rolesDs).find { CardRole cr -> cr.getProcRole() == pr }
            }
    items.add(0, createRoleCaption)
    roleCreateLookup.setOptionsList(items)
    roleCreateLookup.setNullOption(createRoleCaption)
  }

  private List getDsItems(CollectionDatasource ds) {
    ds.getItemIds().asList().collect { id -> ds.getItem(id) }
  }
}