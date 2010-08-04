/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 30.07.2010 12:25:43
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;
import java.util.Set;

@Table(name = "WF_USER_GROUP")
@Entity(name = "wf$UserGroup")
@NamePattern("%s|name")
public class UserGroup extends StandardEntity {

    @Column(name = "NAME")
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "WF_USER_GROUP_USER",
        joinColumns = @JoinColumn(name = "USER_GROUP_ID", referencedColumnName = "ID"),
        inverseJoinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID"))
    private Set<User> users;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
