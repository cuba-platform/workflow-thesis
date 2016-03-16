/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;

/**
 *
 */
@Entity(name = "wf$UserNotifiedBySms")
@Table(name = "WF_USER_NOTIFIED_BY_SMS")
public class UserNotifiedBySms extends BaseUuidEntity {

    private static final long serialVersionUID = -4233245010821767444L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    protected User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
