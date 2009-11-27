/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 17.11.2009 13:45:38
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.Aggregation;
import com.haulmont.chile.core.annotations.NamePattern;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import java.util.List;

@Entity(name = "wf$Proc")
@Table(name = "WF_PROC")
@NamePattern("%s|name")
public class Proc extends StandardEntity {

    private static final long serialVersionUID = 7588775221603325166L;

    @Column(name = "NAME", length = 255)
    private String name;

    @Column(name = "JBPM_PROCESS_KEY", length = 255)
    private String jbpmProcessKey;

    @Column(name = "MESSAGES_PACK", length = 200)
    private String messagesPack;

    @OneToMany(mappedBy = "proc")
    @Aggregation
    private List<ProcRole> roles;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJbpmProcessKey() {
        return jbpmProcessKey;
    }

    public void setJbpmProcessKey(String jbpmProcessKey) {
        this.jbpmProcessKey = jbpmProcessKey;
    }

    public List<ProcRole> getRoles() {
        return roles;
    }

    public void setRoles(List<ProcRole> roles) {
        this.roles = roles;
    }

    public String getMessagesPack() {
        return messagesPack;
    }

    public void setMessagesPack(String messagesPack) {
        this.messagesPack = messagesPack;
    }
}
