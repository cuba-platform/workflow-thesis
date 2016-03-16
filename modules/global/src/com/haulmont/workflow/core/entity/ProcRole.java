/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.Composition;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.NamePattern;
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
    protected Proc proc;

    @Column(name = "CODE", length = 50)
    protected String code;

    @Column(name = "NAME", length = 100)
    protected String name;

    @Column(name = "IS_MULTI_USER")
    protected Boolean multiUser = false;

    @Column(name = "INVISIBLE")
    protected Boolean invisible = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROLE_ID")
    protected Role role;

    @OneToMany(mappedBy = "procRole")
    @Composition
    protected List<DefaultProcActor> defaultProcActors;

    @Column(name = "ASSIGN_TO_CREATOR")
    protected Boolean assignToCreator = false;

    @Column(name = "SORT_ORDER")
    protected Integer sortOrder;

    @Column(name = "ORDER_FILLING_TYPE")
    protected String orderFillingType;

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

    public Boolean getInvisible() {
        return invisible;
    }

    public void setInvisible(Boolean invisible) {
        this.invisible = invisible;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public OrderFillingType getOrderFillingType() {
        return OrderFillingType.fromId(orderFillingType);
    }

    public void setOrderFillingType(OrderFillingType orderFillingType) {
        this.orderFillingType = orderFillingType == null ? null : orderFillingType.getId();
    }
}