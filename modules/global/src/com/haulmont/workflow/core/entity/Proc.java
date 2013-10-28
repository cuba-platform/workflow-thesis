/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.Composition;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.annotation.OnDelete;
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse;
import com.haulmont.cuba.core.global.DeletePolicy;
import com.haulmont.cuba.security.entity.Role;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity(name = "wf$Proc")
@Table(name = "WF_PROC")
@NamePattern("%s|name")
public class Proc extends StandardEntity {

    private static final long serialVersionUID = 7588775221603325166L;

    @Column(name = "NAME", length = 255)
    protected String name;

    @Column(name = "JBPM_PROCESS_KEY", length = 255)
    protected String jbpmProcessKey;

    @Column(name = "CODE", length = 255)
    protected String code;

    @Column(name = "MESSAGES_PACK", length = 200)
    protected String messagesPack;

    @OneToMany(mappedBy = "proc")
    @Composition
    @OrderBy("sortOrder")
    protected List<ProcRole> roles;

    @Column(name = "CARD_TYPES")
    protected String cardTypes;

    @Column(name = "PERMISSIONS_ENABLED")
    protected Boolean permissionsEnabled = false;

    @Column(name = "STATES", length = 500)
    protected String states;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESIGN_ID")
    @OnDeleteInverse(DeletePolicy.UNLINK)
    protected Design design;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AVAILABLE_ROLE_ID")
    protected Role availableRole;

    @OneToMany(mappedBy = "proc", fetch = FetchType.LAZY)
    @OnDelete(DeletePolicy.CASCADE)
    private Set<ProcVariable> processVariables;

    @Column(name = "COMBINED_STAGES_ENABLED")
    protected Boolean combinedStagesEnabled = false;

    @Column(name = "DURATION_ENABLED")
    protected Boolean durationEnabled = false;

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

    public Design getDesign() {
        return design;
    }

    public void setDesign(Design design) {
        this.design = design;
    }

    public Role getAvailableRole() {
        return availableRole;
    }

    public void setAvailableRole(Role availableRole) {
        this.availableRole = availableRole;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getCombinedStagesEnabled() {
        return combinedStagesEnabled;
    }

    public void setCombinedStagesEnabled(Boolean combinedStagesEnabled) {
        this.combinedStagesEnabled = combinedStagesEnabled;
    }

    public Boolean getDurationEnabled() {
        return durationEnabled;
    }

    public void setDurationEnabled(Boolean durationEnabled) {
        this.durationEnabled = durationEnabled;
    }

    public Set<ProcVariable> getProcessVariables() {
        return processVariables;
    }

    public void setProcessVariables(Set<ProcVariable> processVariables) {
        this.processVariables = processVariables;
    }
}
