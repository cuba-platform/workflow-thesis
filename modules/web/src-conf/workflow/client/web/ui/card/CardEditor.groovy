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

import com.haulmont.cuba.gui.components.AbstractEditor
import com.haulmont.cuba.gui.components.IFrame
import com.haulmont.workflow.core.entity.Card
import com.haulmont.cuba.gui.data.Datasource
import com.haulmont.cuba.gui.components.Table
import com.haulmont.cuba.gui.components.TableActionsHelper
import com.haulmont.cuba.gui.components.ValueProvider
import com.haulmont.cuba.gui.components.Button
import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.core.entity.FileDescriptor
import com.haulmont.cuba.gui.components.LookupField
import com.haulmont.cuba.gui.data.ValueListener
import com.haulmont.cuba.gui.data.CollectionDatasource
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter
import com.haulmont.cuba.gui.data.CollectionDatasourceListener.Operation
import com.haulmont.cuba.web.app.FileDownloadHelper
import com.haulmont.cuba.gui.WindowManager
import workflow.client.web.ui.actions.ActionsFrame

public class CardEditor extends AbstractEditor {  

  private Table attachmentsTable
  private List rolesButtons

  def CardEditor(IFrame frame) {
    super(frame);
  }

  protected void init(Map<String, Object> params) {
    super.init(params);

    Datasource<Card> cardDs = getDsContext().get('cardDs')

    attachmentsTable = getComponent('attachmentsTable')
    TableActionsHelper attachmentsTH = new TableActionsHelper(this, attachmentsTable)
    attachmentsTH.createCreateAction([
            getParameters: { [:] },
            getValues: { ['card': cardDs.getItem(), 'file': new FileDescriptor()] }
    ] as ValueProvider, WindowManager.OpenType.DIALOG)
    attachmentsTH.createEditAction(WindowManager.OpenType.DIALOG)
    attachmentsTH.createRemoveAction(false)

    LookupField procLookup = getComponent('proc')
    procLookup.addListener({
      Object source, String property, Object prevValue, Object value ->
      if (value == null)
        enableRolesChange(false)
      else
        enableRolesChange(true)
    } as ValueListener)

    rolesButtons = [getComponent('createRole'), getComponent('editRole'), getComponent('removeRole')]

    Table rolesTable = getComponent('rolesTable')
    TableActionsHelper rolesTH = new TableActionsHelper(this, rolesTable)
    rolesTH.createCreateAction([
            getParameters: { ['proc': procLookup.getValue()] },
            getValues: { ['card': cardDs.getItem()] }
    ] as ValueProvider, WindowManager.OpenType.DIALOG)
    rolesTH.createEditAction(WindowManager.OpenType.DIALOG)
    rolesTH.createRemoveAction(false)

    CollectionDatasource rolesDs = getDsContext().get('rolesDs')
    rolesDs.addListener([collectionChanged: {
      CollectionDatasource ds, Operation operation ->
      procLookup.setEnabled(ds.getItemIds().isEmpty())
    }] as CollectionDsListenerAdapter)
  }

  public void setItem(Entity item) {
    super.setItem(item);

    enableRolesChange(false)
    FileDownloadHelper.initGeneratedColumn(attachmentsTable, 'file');

    ActionsFrame actionsFrame = getComponent('actions')
    actionsFrame.initActions(item)
  }

  private void enableRolesChange(boolean enable) {
    rolesButtons.each {Button btn -> btn.setEnabled(enable) }
  }

}