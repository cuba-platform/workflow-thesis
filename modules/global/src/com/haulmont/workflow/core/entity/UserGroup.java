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
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;
import java.util.Set;

@Table(name = "WF_USER_GROUP")
@Entity(name = "wf$UserGroup")
@NamePattern("%s|name")
@SystemLevel
public class UserGroup extends StandardEntity {

    @Column(name = "NAME")
    protected String name;

    @Column(name = "GLOBAL")
    protected Boolean global = false;

    @ManyToMany
    @JoinTable(name = "WF_USER_GROUP_USER",
        joinColumns = @JoinColumn(name = "USER_GROUP_ID"),
        inverseJoinColumns = @JoinColumn(name = "USER_ID"))
    protected Set<User> users;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUBSTITUTED_CREATOR_ID")
    protected User substitutedCreator;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getGlobal() {
        return global;
    }

    public void setGlobal(Boolean global) {
        this.global = global;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public User getSubstitutedCreator() {
        return substitutedCreator;
    }

    public void setSubstitutedCreator(User substitutedCreator) {
        this.substitutedCreator = substitutedCreator;
    }
}