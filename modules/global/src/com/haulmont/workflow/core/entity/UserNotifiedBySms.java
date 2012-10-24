/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;

/**
 * <p>$Id$</p>
 *
 * @author novikov
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
