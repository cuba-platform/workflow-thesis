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
    private Boolean multiUser;

    @OneToMany(mappedBy = "procRole")
    private List<DefaultProcActor> defaultProcActors;

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
}