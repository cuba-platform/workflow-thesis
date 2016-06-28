/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;

@Entity(name = "wf$DefaultProcActor")
@Table(name = "WF_DEFAULT_PROC_ACTOR")
@SystemLevel
public class DefaultProcActor extends StandardEntity {
    private static final long serialVersionUID = 1792932805043314692L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    protected User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_ROLE_ID")
    protected ProcRole procRole;

    @Column(name = "NOTIFY_BY_EMAIL")
    protected Boolean notifyByEmail = true;

    @Column(name = "SORT_ORDER")
    protected Integer sortOrder;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ProcRole getProcRole() {
        return procRole;
    }

    public void setProcRole(ProcRole procRole) {
        this.procRole = procRole;
    }

    public Boolean getNotifyByEmail() {
        return notifyByEmail;
    }

    public void setNotifyByEmail(Boolean notifyByEmail) {
        this.notifyByEmail = notifyByEmail;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}