/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 12.11.2009 18:01:18
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.chile.core.annotations.Aggregation;
import com.haulmont.cuba.security.entity.Role;

import javax.persistence.*;
import java.util.List;

@Entity(name = "wf$ProcRole")
@Table(name = "WF_PROC_ROLE")
@NamePattern("%s|name")
public class ProcRole extends StandardEntity {

    private static final long serialVersionUID = 8160964587888346590L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_ID")
    private Proc proc;

    @Column(name = "CODE", length = 50)
    private String code;

    @Column(name = "NAME", length = 100)
    private String name;

    @Column(name = "IS_MULTI_USER")
    private Boolean multiUser = false;

    @Column(name = "INVISIBLE")
    private Boolean invisible = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROLE_ID")
    private Role role;

    @OneToMany(mappedBy = "procRole")
    @Aggregation
    private List<DefaultProcActor> defaultProcActors;

    @Column(name = "ASSIGN_TO_CREATOR")
    private Boolean assignToCreator = false;

    @OneToMany(mappedBy = "procRoleFrom")
    @Aggregation
    private List<ProcRolePermission> permissions;

    public List<DefaultProcActor> getDefaultProcActors() {
        return defaultProcActors;
    }

    public void setDefaultProcActors(List<DefaultProcActor> defaultProcActors) {
        this.defaultProcActors = defaultProcActors;
    }

    public Proc getProc() {
        return proc;
    }

    public void setProc(Proc proc) {
        this.proc = proc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getMultiUser() {
        return multiUser;
    }

    public void setMultiUser(Boolean multiUser) {
        this.multiUser = multiUser;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getAssignToCreator() {
        return assignToCreator;
    }

    public void setAssignToCreator(Boolean assignToCreator) {
        this.assignToCreator = assignToCreator;
    }

    public List<ProcRolePermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<ProcRolePermission> permissions) {
        this.permissions = permissions;
    }

    public Boolean getInvisible() {
        return invisible;
    }

    public void setInvisible(Boolean invisible) {
        this.invisible = invisible;
    }
}