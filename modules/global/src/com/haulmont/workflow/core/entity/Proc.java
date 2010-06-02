/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 17.11.2009 13:45:38
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.Aggregation;
import com.haulmont.chile.core.annotations.NamePattern;

import javax.persistence.*;
import java.util.List;

@Entity(name = "wf$Proc")
@Table(name = "WF_PROC")
@NamePattern("%s|name")
public class Proc extends StandardEntity {

    private static final long serialVersionUID = 7588775221603325166L;

    @Column(name = "NAME", length = 255)
    private String name;

    @Column(name = "JBPM_PROCESS_KEY", length = 255)
    private String jbpmProcessKey;

    @Column(name = "MESSAGES_PACK", length = 200)
    private String messagesPack;

    @OneToMany(mappedBy = "proc")
    @Aggregation
    @OrderBy("code")
    private List<ProcRole> roles;

    @Column(name = "CARD_TYPES")
    private String cardTypes;

    @Column(name = "PERMISSIONS_ENABLED")
    private Boolean permissionsEnabled = false;

    @Column(name = "STATES", length = 500)
    private String states;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJbpmProcessKey() {
        return jbpmProcessKey;
    }

    public void setJbpmProcessKey(String jbpmProcessKey) {
        this.jbpmProcessKey = jbpmProcessKey;
    }

    public List<ProcRole> getRoles() {
        return roles;
    }

    public void setRoles(List<ProcRole> roles) {
        this.roles = roles;
    }

    public String getMessagesPack() {
        return messagesPack;
    }

    public void setMessagesPack(String messagesPack) {
        this.messagesPack = messagesPack;
    }

    public String getCardTypes() {
        return cardTypes;
    }

    public void setCardTypes(String cardTypes) {
        this.cardTypes = cardTypes;
    }

    public Boolean getPermissionsEnabled() {
        return permissionsEnabled;
    }

    public void setPermissionsEnabled(Boolean permissionsEnabled) {
        this.permissionsEnabled = permissionsEnabled;
    }

    public String getStates() {
        return states;
    }

    public void setStates(String states) {
        this.states = states;
    }
}
