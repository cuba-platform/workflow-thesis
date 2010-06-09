/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 27.05.2010 12:31:13
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.MessageUtils;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.workflow.core.global.ProcRolePermissionType;
import com.haulmont.workflow.core.global.ProcRolePermissionValue;
import com.haulmont.workflow.core.global.WfConstants;

import javax.persistence.*;

@Entity(name = "wf$ProcRolePermission")
@Table(name = "WF_PROC_ROLE_PERMISSION")
public class ProcRolePermission extends StandardEntity {
    private static final long serialVersionUID = -7951136164637586925L;

    @Column(name = "STATE", length = 255)
    protected String state;

    @Column(name = "TYPE")
    protected Integer type;

    @Column(name = "VALUE")
    protected Integer value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_ROLE_FROM_ID")
    protected ProcRole procRoleFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_ROLE_TO_ID")
    protected ProcRole procRoleTo;

    @MetaProperty
    public String getLocState() {
        if (getState() == null)
            return "";
        if (procRoleFrom.getProc() != null) {
            String messagesPack = null;
            if (WfConstants.PROC_NOT_ACTIVE.equals(getState()))
                messagesPack = MessageUtils.getMessagePack();
            else
                messagesPack = procRoleFrom.getProc().getMessagesPack();
            return MessageUtils.loadString(messagesPack, "msg://" + getState());
        }
        return getState();
    }


    public String getState() {
        return state;                          
    }

    public void setState(String state) {
        this.state = state;
    }

    public ProcRolePermissionType getType() {
        return ProcRolePermissionType.fromId(type);
    }

    public void setType(ProcRolePermissionType type) {
        this.type = (type == null) ? null : type.getId();
    }

    public ProcRolePermissionValue getValue() {
        return ProcRolePermissionValue.fromId(value);
    }

    public void setValue(ProcRolePermissionValue value) {
        this.value = (value == null) ? null : value.getId();
    }

    public ProcRole getProcRoleFrom() {
        return procRoleFrom;
    }

    public void setProcRoleFrom(ProcRole procRoleFrom) {
        this.procRoleFrom = procRoleFrom;
    }

    public ProcRole getProcRoleTo() {
        return procRoleTo;
    }

    public void setProcRoleTo(ProcRole procRoleTo) {
        this.procRoleTo = procRoleTo;
    }
}
