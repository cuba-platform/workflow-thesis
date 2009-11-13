/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 13.11.2009 16:07:05
 *
 * $Id$
 */
package com.haulmont.docflow.entity;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;

@Entity(name = "df$UserRole")
@Table(name = "DF_USER_ROLE")
public class DfUserRole extends StandardEntity {

    private static final long serialVersionUID = -3702914132857780441L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DF_ROLE_ID")
    private DfRole role;

    public DfRole getRole() {
        return role;
    }

    public void setRole(DfRole role) {
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
