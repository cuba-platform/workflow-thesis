/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 20.11.2009 19:01:55
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.proc

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

import com.haulmont.cuba.gui.data.DataService

import com.haulmont.cuba.gui.data.ValueListener
import com.haulmont.cuba.security.entity.User
import com.haulmont.cuba.web.gui.components.WebComponentsHelper
import com.haulmont.chile.core.model.MetaPropertyPath
import com.haulmont.cuba.core.global.PersistenceHelper
import com.vaadin.data.Property.ValueChangeEvent
import com.vaadin.data.Property.ValueChangeListener
import com.haulmont.cuba.security.entity.Role
import com.vaadin.data.Property
import com.haulmont.cuba.web.gui.components.WebLookupField
import java.util.*
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.BooleanUtils
import com.haulmont.workflow.core.entity.OrderFillingType
import com.haulmont.cuba.core.global.MessageProvider
import com.haulmont.cuba.web.gui.components.WebButton
import com.haulmont.cuba.gui.data.impl.CollectionPropertyDatasourceImpl
import com.haulmont.cuba.gui.data.CollectionDatasourceListener

public class ProcEditor extends AbstractEditor {

  protected Table rolesTable;
  protected Table permissionsTable;
  protected Datasource<Proc> procDs;
  protected Set<Tabsheet.Tab> initedTabs = new HashSet<Tabsheet.Tab>()
  protected CollectionDatasource<DefaultProcActor, UUID> dpaDs
  protected CollectionDatasource<Role, UUID> secRolesDs
  protected Map multiUserMap = new HashMap<UUID, com.vaadin.ui.Component>();
  protected Map assignToCreatorMap = new HashMap<UUID, com.vaadin.ui.Component>();
  protected Map orderFillingTypeMap = new HashMap<UUID, com.vaadin.ui.Component>();

  public ProcEditor(IFrame frame) {
    super(frame);
  }

  protected boolean isMultiUserEditable(ProcRole pr) {
    return true;
  }

  @Override
  public void init(Map<String, Object> params) {
    super.init(params)
    procDs = getDsContext().get("procDs")
    final CollectionDatasource<ProcRole, UUID> rolesDs = getDsContext().get("rolesDs")
    dpaDs  = getDsContext().get("dpaDs")
    secRolesDs = getDsContext().get("secRoles");
    secRolesDs.refresh();

//    final CheckBox permissionsEnabled = getComponent("permissionsEnabled");
//    final Component permissionsPane = getComponent("permissionsPane");
//    permissionsEnabled.addListener (new ValueListener(){
//
//      void valueChanged(Object source, String property, Object prevValue, Object value) {
//        if(permissionsPane != null)
//          permissionsPane.setVisible(permissionsEnabled.getValue());
//      }
//
//    });

    rolesTable = getComponent("rolesTable")
    TableActionsHelper rolesHelper = new TableActionsHelper(this, rolesTable)
    rolesHelper.createCreateAction([
            getParameters: { null },
            getValues: {
              Map<String, Object> values = new HashMap<String, Object>()
              values.put("proc", procDs.getItem())
              int order = findMaxSortOrder() + 1
              values.put("sortOrder", order)
              return values
            }
    ] as ValueProvider)
    rolesHelper.createRemoveAction(false)

    List dpaActions = []

    Table dpaTable = getComponent("dpaTable")
    TableActionsHelper dpaHelper = new TableActionsHelper(this, dpaTable)
    def createDpaAction = dpaHelper.createCreateAction(
            [
                    getParameters: {
                      List<UUID> userIds = new LinkedList<UUID> ();
                      for (UUID uuid : dpaDs.getItemIds()) {
                        DefaultProcActor dpa = dpaDs.getItem(uuid);
                        User user = dpa.getUser();
                        if (user)
                          userIds.add(user.getId());
                      }
                      return Collections.singletonMap("userIds", userIds)},
                    getValues: {
                      Map<String, Object> values = new HashMap<String, Object>()
                      values.put("procRole", rolesDs.getItem())
                      return values
                    }
            ] as ValueProvider,
            OpenType.DIALOG)
    dpaActions.add(createDpaAction)
    dpaActions.add(dpaHelper.createEditAction(OpenType.DIALOG, [
                    getParameters: {
                      List<UUID> userIds = new LinkedList<UUID> ();
                      for (UUID uuid : dpaDs.getItemIds()) {
                        DefaultProcActor dpa = dpaDs.getItem(uuid);
                        User user = dpa.getUser();
                        if (user && !dpa.equals(dpaDs.getItem()))
                          userIds.add(user.getId());
                      }
                      return Collections.singletonMap("userIds", userIds)},
                    getValues: {
                      null
                    }
            ] as ValueProvider))
    dpaActions.add(dpaHelper.createRemoveAction(false))

    dpaActions.each {
      it.setEnabled(false)
    }

    def enableCheckBox = {
      ProcRole pr = rolesDs.getItem();
      if (pr == null)
        return;
      if (BooleanUtils.isTrue(pr.getMultiUser()) && dpaDs.size() > 1) {
        ((com.vaadin.ui.Component) multiUserMap.get(pr.getUuid())).setReadOnly(true);
      } else {
        if (dpaDs.size() > 0)
          ((com.vaadin.ui.Component) multiUserMap.get(pr.getUuid())).setReadOnly(!isMultiUserEditable(pr) || !rolesTable.isEditable() && false || BooleanUtils.isTrue(pr.getAssignToCreator()));
        else
          ((com.vaadin.ui.Component) multiUserMap.get(pr.getUuid())).setReadOnly(!isMultiUserEditable(pr) || !rolesTable.isEditable() && false);
      }
      if (!BooleanUtils.isTrue(pr.getMultiUser()) && dpaDs.size() > 0) {
        ((com.vaadin.ui.Component) assignToCreatorMap.get(pr.getUuid())).setReadOnly(true);
      } else {
        ((com.vaadin.ui.Component) assignToCreatorMap.get(pr.getUuid())).setReadOnly(!rolesTable.isEditable() && false);
      }
    }

    def enableDpaActions = {
      ProcRole item = rolesDs.getItem()
      enableCheckBox()
      dpaActions.each { it.setEnabled(item != null) }
      if (item && !BooleanUtils.isTrue(item.getMultiUser()) && (BooleanUtils.isTrue(item.getAssignToCreator() || !dpaDs.getItemIds().isEmpty())))
        createDpaAction.setEnabled(false);
      else
        createDpaAction.setEnabled(createDpaAction.isEnabled() &&
              (item.getMultiUser() || dpaDs.getItemIds().isEmpty()))
    }



    dpaDs.addListener(
            [
                    collectionChanged: { ds, operation -> enableDpaActions() },
                    itemChanged:{ds, prevItem, item ->
                      enableCheckBox()
                    }
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
                    afterCommit: { CommitContext<Entity> context, Set<Entity> result ->
                    }
            ] as CommitListener
    )

      //todo rework mechanism
      /*List permissionsActions = []
      permissionsTable = getComponent("permissionsTable")
      if (permissionsTable != null) {
          TableActionsHelper permissionsTableHelper = new TableActionsHelper(this, permissionsTable)
          Action createPermissionsAction = permissionsTableHelper.createCreateAction(new ValueProvider() {

              Map<String, Object> getValues() {
                  return ['procRoleFrom': rolesDs.getItem()]
                  return null
              }

              Map<String, Object> getParameters() {
                  return ['proc': procDs.getItem()]
              }
          }, WindowManager.OpenType.DIALOG)

          Action editPermissionsAction = permissionsTableHelper.createEditAction(WindowManager.OpenType.DIALOG, ['proc': params['param$item']])
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
                          itemChanged: { ds, prevItem, item -> enableDpaActions(); enablePermissionsActions() },
                          valueChanged: { source, property, prevValue, value -> enableDpaActions() }
                  ] as DsListenerAdapter
          )
      }*/

    WebButton moveUp = (WebButton) getComponent("moveUp");
    moveUp.setAction(new AbstractAction("moveUp") {
      public void actionPerform(Component component) {
        Set selected = rolesTable.getSelected();
        if (selected.isEmpty())
          return;

        ProcRole curCr = (ProcRole) selected.iterator().next();
        UUID prevId = ((CollectionPropertyDatasourceImpl)rolesDs).prevItemId(curCr.getId());
        if (prevId == null)
          return;

        Integer tmp = curCr.getSortOrder();
        ProcRole prevCr = rolesDs.getItem(prevId);
        curCr.setSortOrder(prevCr.getSortOrder());
        prevCr.setSortOrder(tmp);

        sortRolesDs("sortOrder");

      }
    });

    WebButton moveDown = (WebButton) getComponent("moveDown");
    moveDown.setAction(new AbstractAction("moveDown") {
      public void actionPerform(Component component) {
        Set selected = rolesTable.getSelected();
        if (selected.isEmpty())
          return;

        ProcRole curCr = (ProcRole) selected.iterator().next();
        UUID nextId = ((CollectionPropertyDatasourceImpl) rolesDs).nextItemId(curCr.getId());
        if (nextId == null)
          return;

        Integer tmp = curCr.getSortOrder();
        ProcRole nextCr = rolesDs.getItem(nextId);
        curCr.setSortOrder(nextCr.getSortOrder());
        nextCr.setSortOrder(tmp);

        sortRolesDs("sortOrder");
      }
    });

    initLazyTabs()    
  }

  private void sortRolesDs(String property) {
    CollectionDatasource rolesDs = rolesTable.getDatasource()
    CollectionDatasource.Sortable.SortInfo sortInfo = new CollectionDatasource.Sortable.SortInfo();
    sortInfo.setOrder(CollectionDatasource.Sortable.Order.ASC);
    sortInfo.setPropertyPath(rolesDs.getMetaClass().getPropertyPath(property));
    def sortInfos = new CollectionDatasource.Sortable.SortInfo[1];
    sortInfos[0] = sortInfo;
    ((CollectionPropertyDatasourceImpl) rolesDs).sort(sortInfos);
    ((CollectionPropertyDatasourceImpl) rolesDs).forceCollectionChanged(CollectionDatasourceListener.Operation.REFRESH)
  }

  def int findMaxSortOrder() {
    int max = 0;
    CollectionDatasource rolesDs = rolesTable.getDatasource()
    for (UUID id: rolesDs.getItemIds()) {
      ProcRole pr = (ProcRole) rolesDs.getItem(id);
      if (pr.getSortOrder() != null && pr.getSortOrder() > max) {
        max = pr.getSortOrder();
      }
    }
    return max;
  }

  private void initLazyTabs() {
    Tabsheet tabsheet = getComponent("tabsheet")
    tabsheet.addListener([
            tabChanged : {Tabsheet.Tab newTab ->
              if (!initedTabs.contains(newTab)) {
                initedTabs << newTab
                if (newTab.name == 'stagesTab') {
                  initStagesTab();
                }
              }
            }
    ] as Tabsheet.TabChangeListener)
  }

  private void initStagesTab() {
    final Table stagesTable = getComponent("stagesTable");

    TableActionsHelper stagesTableHelper = new TableActionsHelper(this, stagesTable);
    stagesTableHelper.createCreateAction(new ValueProvider() {
      public Map<String, Object> getValues() {
        return Collections.<String, Object> singletonMap("proc", procDs.getItem())
      }

      public Map<String, Object> getParameters() {
        return null;
      }
    }, WindowManager.OpenType.THIS_TAB);

    stagesTableHelper.createEditAction(WindowManager.OpenType.THIS_TAB);
    stagesTableHelper.createRemoveAction(false);
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
    final CollectionDatasource rolesDs = rolesTable.getDatasource()
    final com.vaadin.ui.Table vTable = WebComponentsHelper.unwrap(rolesTable);
    MetaPropertyPath pp = rolesDs.getMetaClass().getPropertyEx('code');
    vTable.removeGeneratedColumn(pp);
    final Map codeMap = new HashMap<UUID, com.vaadin.ui.Component>()
    vTable.addGeneratedColumn pp, [
            generateCell: {table, itemId, columnId ->
              if (codeMap.containsKey(itemId))
                return codeMap.get(itemId);
              ProcRole pr = rolesDs.getItem(itemId);
              com.vaadin.ui.Component component = null;
              if (pr != null) {
                if (PersistenceHelper.isNew(pr) && rolesTable.isEditable()) {
                  final UUID uuid = itemId;
                  final com.vaadin.ui.TextField textField = new com.vaadin.ui.TextField()
                  textField.setValue(pr.getCode());
                  textField.addListener({ValueChangeEvent event ->
                    ((ProcRole) rolesDs.getItem(uuid)).setCode(textField.getValue());
                  } as ValueChangeListener);
                  component = textField
                } else {
                  component = StringUtils.isNotBlank(pr.getCode()) ? new com.vaadin.ui.Label(pr.getCode()) : null;
                }
              }
              if (component.getValue() != null)
                codeMap.put(itemId, component);
              return component;
            }
    ] as com.vaadin.ui.Table.ColumnGenerator;

    pp = rolesDs.getMetaClass().getPropertyEx('name');
    vTable.removeGeneratedColumn(pp);
    final Map nameMap = new HashMap<UUID, com.vaadin.ui.Component>();
    vTable.addGeneratedColumn(pp, [
            generateCell: {table, itemId, columnId ->
              if (nameMap.containsKey(itemId))
                nameMap.get(itemId);
              ProcRole pr = rolesDs.getItem(itemId);
              com.vaadin.ui.Component component = null;
              if (pr != null) {
                if (rolesTable.isEditable()) {
                  final Object uuid = itemId;
                  final com.vaadin.ui.TextField textField = new com.vaadin.ui.TextField()
                  textField.setValue(pr.getName());
                  textField.addListener ( {ValueChangeEvent event ->
                     ((ProcRole)rolesDs.getItem(uuid)).setName(textField.getValue());
                  } as ValueChangeListener);
                  component = textField;
                } else {
                  component = StringUtils.isNotBlank(pr.getName()) ? new com.vaadin.ui.Label(pr.getName()) : null;
                }
              }
              nameMap.put(itemId, component);
              return component;
            }
    ] as com.vaadin.ui.Table.ColumnGenerator);

    pp = rolesDs.getMetaClass().getPropertyEx('multiUser');
    vTable.removeGeneratedColumn(pp);
    vTable.addGeneratedColumn(pp, [
            generateCell: {table, itemId, columnId ->
              if (multiUserMap.containsKey(itemId))
                return multiUserMap.get(itemId)
              ProcRole pr = rolesDs.getItem(itemId);
              com.vaadin.ui.Component component = null;
              if (pr != null){
                final Object uuid = itemId;
                final com.vaadin.ui.CheckBox checkBox = new com.vaadin.ui.CheckBox()
                checkBox.setValue(pr.getMultiUser())
                checkBox.setImmediate(true)
                if (rolesTable.isEditable())
                  checkBox.setReadOnly(!isMultiUserEditable(pr) || BooleanUtils.isTrue(pr.getMultiUser()) && pr.getDefaultProcActors() != null && pr.getDefaultProcActors().size() > 1);
                else
                  checkBox.setReadOnly(!rolesTable.isEditable());
                checkBox.addListener ( {ValueChangeEvent event ->
                     ProcRole procRole = rolesDs.getItem(uuid);
                     rolesTable.setSelected(procRole);
                     procRole.setMultiUser(checkBox.getValue());
                     if (orderFillingTypeMap.containsKey(itemId)) {
                       com.vaadin.ui.Component c = (com.vaadin.ui.Component) orderFillingTypeMap.get(itemId);
                       if (c != null) c.setVisible(checkBox.getValue());
                     }
                  } as ValueChangeListener);
                component = checkBox;
              }
              multiUserMap.put(itemId, component);
              return component;
            }
    ] as com.vaadin.ui.Table.ColumnGenerator);

    pp = rolesDs.getMetaClass().getPropertyEx('assignToCreator');
    vTable.removeGeneratedColumn(pp);
    vTable.addGeneratedColumn(pp, [
            generateCell: {table, itemId, columnId ->
              if (assignToCreatorMap.containsKey(itemId))
                return assignToCreatorMap.get(itemId);
              ProcRole pr = rolesDs.getItem(itemId);
              com.vaadin.ui.Component component = null;
              if (pr != null){
                final UUID uuid = itemId;
                final com.vaadin.ui.CheckBox checkBox = new com.vaadin.ui.CheckBox()
                checkBox.setValue(pr.getAssignToCreator())
                if (rolesTable.isEditable()) {
                  if (pr.getMultiUser())
                    checkBox.setReadOnly(false);
                  else
                    checkBox.setReadOnly( pr.getDefaultProcActors() != null && pr.getDefaultProcActors().size() > 0);
                } else
                  checkBox.setReadOnly(true);
                checkBox.setImmediate(true)
                checkBox.addListener({ValueChangeEvent event ->
                  ProcRole procRole = rolesDs.getItem(uuid);
                  rolesTable.setSelected(procRole);
                  procRole.setAssignToCreator(checkBox.getValue());
                } as ValueChangeListener);
                component = checkBox;
              }
              assignToCreatorMap.put(itemId, component);
              return component;
            }
    ] as com.vaadin.ui.Table.ColumnGenerator);

    pp = rolesDs.getMetaClass().getPropertyEx('role');
    vTable.removeGeneratedColumn(pp);
    final Map roleMap = new HashMap<UUID, com.vaadin.ui.Component>();
    vTable.addGeneratedColumn(pp, [
            generateCell: {table, itemId, columnId ->
              if (roleMap .containsKey(itemId))
                return roleMap.get(itemId);
              ProcRole pr = rolesDs.getItem(itemId);
              com.vaadin.ui.Component component = null;
              if (pr != null) {
                if (rolesTable.isEditable()) {
                  final Object uuid = itemId;
                  WebLookupField usersLookup = new WebLookupField();
                  usersLookup.setOptionsDatasource(secRolesDs);
                  usersLookup.setValue(pr.getRole());
                  usersLookup.setWidth("100%");
                  usersLookup.setEditable(rolesTable.isEditable());
                  final com.vaadin.ui.Select rolesSelect = (com.vaadin.ui.Select) WebComponentsHelper.unwrap(usersLookup);
                  rolesSelect.addListener({ValueChangeEvent event ->
                    Role role = secRolesDs.getItem(rolesSelect.getValue());
                    ((ProcRole) rolesDs.getItem(uuid)).setRole(role);
                  } as ValueChangeListener);
                  return rolesSelect;
                } else {
                  component = pr.getRole() != null ? new com.vaadin.ui.Label(pr.getRole().getInstanceName()) : null;
                }
              }
              roleMap.put(itemId, component);
              return component;
            }
    ] as com.vaadin.ui.Table.ColumnGenerator);

    pp = rolesDs.getMetaClass().getPropertyEx('orderFillingType');
    vTable.removeGeneratedColumn(pp);
    vTable.addGeneratedColumn(pp, [
            generateCell: {table, itemId, columnId ->
              if (orderFillingTypeMap.containsKey(itemId))
                return orderFillingTypeMap.get(itemId)
              ProcRole pr = rolesDs.getItem(itemId);
              com.vaadin.ui.Component component = null;
              if (pr != null) {
                final Object uuid = itemId;
                if (rolesTable.isEditable()) {
                  WebLookupField orderFillingTypeLookup = new WebLookupField();
                  Map<String, Object> types = new HashMap<String, Object>();
                  for (OrderFillingType oft: OrderFillingType.values()) {
                    types.put(MessageProvider.getMessage(oft), oft.getId());
                  }
                  orderFillingTypeLookup.setOptionsMap(types);
                  orderFillingTypeLookup.setValue(pr.getOrderFillingType());
                  orderFillingTypeLookup.setWidth("100%");
                  orderFillingTypeLookup.setVisible(pr.getMultiUser());
                  orderFillingTypeLookup.setEditable(rolesTable.isEditable());

                  final com.vaadin.ui.Select orderFillingTypeSelect = (com.vaadin.ui.Select) WebComponentsHelper.unwrap(orderFillingTypeLookup);
                  orderFillingTypeSelect.setNullSelectionAllowed(false);
                  orderFillingTypeSelect.addListener({ValueChangeEvent event ->
                    ((ProcRole)rolesDs.getItem(uuid)).setOrderFillingType((String) orderFillingTypeSelect.getValue());
                  } as ValueChangeListener);
                  component = orderFillingTypeSelect;
                } else {
                  component = StringUtils.isNotBlank(pr.getOrderFillingType()) ? new com.vaadin.ui.Label(MessageProvider.getMessage(OrderFillingType.fromId(pr.getOrderFillingType()))) : null;
                }
              }
              orderFillingTypeMap.put(itemId, component);
              return component;
            }
    ] as com.vaadin.ui.Table.ColumnGenerator);

    DataService dataService = rolesDs.getDataService()
    java.util.List<ProcRole> roles = proc.roles.collect{dataService.reload(it, 'edit-w-permissions')}
    roles.removeAll({ProcRole role -> role.invisible })
    proc.roles = roles
    procDs.setItem(proc)
  }


}
