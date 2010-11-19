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
import com.haulmont.cuba.gui.data.DsContext.CommitListener
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.gui.WindowManager
import com.haulmont.workflow.core.app.ProcRolePermissionsService
import com.haulmont.cuba.gui.ServiceLocator
import com.haulmont.cuba.gui.components.TableActionsHelper;
import com.haulmont.cuba.gui.components.TableActionsHelper
import com.haulmont.cuba.gui.data.DataService
import com.haulmont.cuba.core.global.LoadContext
import com.haulmont.workflow.core.entity.ProcRolePermission
import com.haulmont.cuba.gui.data.ValueListener;

public class ProcEditor extends AbstractEditor {

  private Table rolesTable;
  private Table permissionsTable;
  private Datasource<Proc> procDs;

  public ProcEditor(IFrame frame) {
    super(frame);
  }

  @Override
  protected void init(Map<String, Object> params) {
    super.init(params)
    procDs = getDsContext().get("procDs")
    final CollectionDatasource<ProcRole, UUID> rolesDs = getDsContext().get("rolesDs")
    final CollectionDatasource<DefaultProcActor, UUID> dpaDs = getDsContext().get("dpaDs")

    final CheckBox permissionsEnabled = getComponent("permissionsEnabled");
    final Component permissionsPane = getComponent("permissionsPane");
    permissionsEnabled.addListener (new ValueListener(){

      void valueChanged(Object source, String property, Object prevValue, Object value) {
        if(permissionsPane != null)
          permissionsPane.setVisible(permissionsEnabled.getValue());
      }

    });

    rolesTable = getComponent("rolesTable")
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

    dpaDs.addListener(
            [
                    collectionChanged: { ds, operation -> enableDpaActions() }
            ] as CollectionDsListenerAdapter
    )

    getDsContext().addListener(
            [
                    beforeCommit: { CommitContext<Entity> context ->
                      Proc p = context.getCommitInstances().find { it == procDs.getItem() }
                      if (p) {
                        if (p.cardTypes && !p.cardTypes.startsWith(','))
                          p.cardTypes = ',' + p.cardTypes
                        if (p.cardTypes && !p.cardTypes.endsWith(','))
                          p.cardTypes = p.cardTypes + ','
                      }
                    },
                    afterCommit: { CommitContext<Entity> context, Map<Entity, Entity> result ->
                    }
            ] as CommitListener
    )

    List permissionsActions = []
    permissionsTable = getComponent("permissionsTable")
    TableActionsHelper permissionsTableHelper = new TableActionsHelper(this, permissionsTable)
    Action createPermissionsAction = permissionsTableHelper.createCreateAction(new ValueProvider() {

      Map<String, Object> getValues() {
        return ['procRoleFrom' : rolesDs.getItem()]
        return null
      }

      Map<String, Object> getParameters() {
        return ['proc' : procDs.getItem()]
      }
    }, WindowManager.OpenType.DIALOG)

    Action editPermissionsAction = permissionsTableHelper.createEditAction(WindowManager.OpenType.DIALOG, ['proc' : params['param$item']])
    permissionsActions.add(createPermissionsAction)
    permissionsActions.add(editPermissionsAction)
    permissionsActions.add(permissionsTableHelper.createRemoveAction(false))

    permissionsActions.each {
      it.enabled = false
    }

    def enablePermissionsActions = {
      ProcRole item = rolesDs.getItem()
      permissionsActions.each { it.setEnabled(item != null) }
    }

    rolesDs.addListener(
            [
                    itemChanged: { ds, prevItem, item -> enableDpaActions(); enablePermissionsActions() } ,
                    valueChanged: { source, property, prevValue, value -> enableDpaActions() }
            ] as DsListenerAdapter
    )
  }

  def void commitAndClose() {
    CollectionDatasource permissionsDs = getDsContext().get("permissionsDs");
    if (permissionsDs.isModified()) {
      ProcRolePermissionsService procRolePermissionsService = ServiceLocator.lookup(ProcRolePermissionsService.NAME);
      procRolePermissionsService.clearPermissionsCache();
    }
    super.commitAndClose();
  }

  def void setItem(Entity item) {
    super.setItem(item);
    Proc proc = (Proc)getItem()
    CollectionDatasource rolesDs = rolesTable.getDatasource()
    DataService dataService = rolesDs.getDataService()
    List<ProcRole> roles = proc.roles.collect{dataService.reload(it, 'edit-w-permissions')}
    proc.roles = roles
    procDs.setItem(proc)
  }


}
