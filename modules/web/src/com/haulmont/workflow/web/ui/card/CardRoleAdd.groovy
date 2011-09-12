/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 30.07.2010 16:27:38
 *
 * $Id$
 */
 package com.haulmont.workflow.web.ui.card

import com.haulmont.cuba.gui.components.IFrame
import com.haulmont.cuba.gui.components.LookupField
import com.haulmont.workflow.core.entity.UserGroup
import com.haulmont.workflow.core.entity.CardRole

import com.haulmont.cuba.gui.data.CollectionDatasource

import com.haulmont.cuba.gui.components.Label

import com.haulmont.cuba.gui.components.CheckBox
import com.haulmont.cuba.web.gui.components.WebComponentsHelper
import com.vaadin.data.Property

import com.haulmont.cuba.security.entity.User
import com.haulmont.cuba.security.entity.Role

class CardRoleAdd extends CardRoleEditor{
  private LookupField userGroupLookup
  private CollectionDatasource userGroupsDs
  private LookupField userLookup
  private Label userLabel
  private Label userGroupLabel
  private CheckBox userCb
  private CheckBox userGroupCb
  private List cardRoles = []
  List<User> invalidUsers = []

  def CardRoleAdd(IFrame frame) {
    super(frame);
  }

    public void init(Map<String, Object> params) {
    super.init(params);
    userGroupsDs = getDsContext().get('userGroupsDs')
    userCb = getComponent('userCb')
    userGroupCb = getComponent('userGroupCb')
    Label userLabel = getComponent('userLabel')
    Label userGroupLabel = getComponent('userGroupLabel')
    userLookup = getComponent('userLookup')
    userGroupLookup = getComponent('userGroupLookup')

    com.vaadin.ui.CheckBox vUserCb = (com.vaadin.ui.CheckBox)WebComponentsHelper.unwrap(userCb)
    com.vaadin.ui.CheckBox vUserGroupCb = (com.vaadin.ui.CheckBox)WebComponentsHelper.unwrap(userGroupCb)

    vUserCb.addListener([
            valueChange : {event ->
              if (vUserCb.value) {
                vUserGroupCb.value = false
                userGroupLabel.enabled = false
                userGroupLookup.enabled = false
                userLabel.enabled = true
                userLookup.enabled = true
              }
            }
    ] as Property.ValueChangeListener)

    vUserGroupCb.addListener([
            valueChange : {event ->
              if (vUserGroupCb.value) {
                vUserCb.value = false
                userLabel.enabled = false
                userLookup.enabled = false
                userGroupLabel.enabled = true
                userGroupLookup.enabled = true
              }
            }
    ] as Property.ValueChangeListener)

    vUserCb.value = true

    if (!this.@procRole.multiUser) userGroupCb.enabled = false

    Label separatorLabel = getComponent('separatorLabel')
    com.vaadin.ui.Label vSeparatorLabel = (com.vaadin.ui.Label)WebComponentsHelper.unwrap(separatorLabel)
    vSeparatorLabel.setContentMode(com.vaadin.ui.Label.CONTENT_XHTML)
//    vSeparatorLabel.setWidth(100, Sizeable.UNITS_PERCENTAGE)

//    com.vaadin.ui.Label vUserLabel = (com.vaadin.ui.Label)WebComponentsHelper.unwrap(userLabel)
//    com.vaadin.ui.Label vUserGroupLabel = (com.vaadin.ui.Label)WebComponentsHelper.unwrap(userGroupLabel)
  }


  def void commitAndClose() {
    if (userGroupLookup.value && userGroupCb.value) {
      UserGroup userGroup = (UserGroup)userGroupLookup.value
      CardRole item = (CardRole)getItem()
      userGroup.users.each{user ->
        List<Role> userSecRoles = user.userRoles.collect{userRole -> userRole.role}
        if (!procRole.role || userSecRoles.contains(procRole.role)) {
          CardRole newCardRole = new CardRole(
                  card: item.card,
                  procRole: item.procRole,
                  notifyByEmail: item.notifyByEmail,
                  notifyByCardInfo: item.notifyByCardInfo,
                  user: user
          )
          this.@cardRoles << newCardRole
        } else {
          this.@invalidUsers << user
        }
      }
    } else {
      this.@cardRoles << (CardRole)getItem()
    }
    close(COMMIT_ACTION_ID, true)
  }

  
  public List<CardRole> getCardRoles() {
    return cardRoles
//    return cardRoles.size() == 0 ? [getItem()] : cardRoles
  }


}
