/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 03.12.2009 15:04:40
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.chile.core.annotations.NamePattern;

import javax.persistence.*;

@Entity(name = "wf$DefaultProcActor")
@Table(name = "WF_DEFAULT_PROC_ACTOR")
public class DefaultProcActor extends StandardEntity {
    private static final long serialVersionUID = 1792932805043314692L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_ROLE_ID")
    private ProcRole procRole;

    @Column(name = "NOTIFY_BY_EMAIL")
    private Boolean notifyByEmail = true;

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
}
