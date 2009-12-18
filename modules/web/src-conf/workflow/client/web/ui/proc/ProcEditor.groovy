/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 20.11.2009 19:01:55
 *
 * $Id$
 */
package workflow.client.web.ui.proc

import java.util.List
import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.data.Datasource
import com.haulmont.workflow.core.entity.Proc
import com.haulmont.workflow.core.entity.ProcRole
import com.haulmont.cuba.gui.data.CollectionDatasource
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter
import com.haulmont.cuba.gui.WindowManager.OpenType
import com.haulmont.workflow.core.entity.DefaultProcActor
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter

public class ProcEditor extends AbstractEditor {

  public ProcEditor(IFrame frame) {
    super(frame);
  }

  @Override
  protected void init(Map<String, Object> params) {
    super.init(params)
    final Datasource<Proc> procDs = getDsContext().get("procDs")
    final CollectionDatasource<ProcRole, UUID> rolesDs = getDsContext().get("rolesDs")
    final CollectionDatasource<DefaultProcActor, UUID> dpaDs = getDsContext().get("dpaDs")

    Table rolesTable = getComponent("rolesTable")
    TableActionsHelper rolesHelper = new TableActionsHelper(this, rolesTable)
    rolesHelper.createCreateAction([
            getParameters: { null },
            getValues: {
              Map<String, Object> values = new HashMap<String, Object>()
              values.put("proc", procDs.getItem())
              return values
            }
    ] as ValueProvider)
    rolesHelper.createEditAction()
    rolesHelper.createRemoveAction(false)

    List dpaActions = []

    Table dpaTable = getComponent("dpaTable")
    TableActionsHelper dpaHelper = new TableActionsHelper(this, dpaTable)
    def createDpaAction = dpaHelper.createCreateAction(
            [
                    getParameters: { null },
                    getValues: {
                      Map<String, Object> values = new HashMap<String, Object>()
                      values.put("procRole", rolesDs.getItem())
                      return values
                    }
            ] as ValueProvider,
            OpenType.DIALOG)
    dpaActions.add(createDpaAction)
    dpaActions.add(dpaHelper.createEditAction(OpenType.DIALOG))
    dpaActions.add(dpaHelper.createRemoveAction(false))

    dpaActions.each {
      it.setEnabled(false)
    }

    def enableDpaActions = {
      ProcRole item = rolesDs.getItem()
      dpaActions.each { it.setEnabled(item != null) }
      createDpaAction.setEnabled(createDpaAction.isEnabled() &&
              (item.getMultiUser() || dpaDs.getItemIds().isEmpty()))
    }

    rolesDs.addListener(
            [
                    itemChanged: { ds, prevItem, item -> enableDpaActions() } ,
                    valueChanged: { source, property, prevValue, value -> enableDpaActions() }
            ] as DsListenerAdapter
    )

    dpaDs.addListener(
            [
                    collectionChanged: { ds, operation -> enableDpaActions() }
            ] as CollectionDsListenerAdapter
    )
  }

}
