/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 16.11.2009 12:12:45
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "wf$Assignment")
@Table(name = "WF_ASSIGNMENT")
public class Assignment extends StandardEntity {

    private static final long serialVersionUID = 2889343799342063691L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MASTER_ASSIGNMENT_ID")
    private Assignment masterAssignment;

    @Column(name = "NAME", length = 500)
    private String name;

    @Column(name = "JBPM_PROCESS_ID", length = 255)
    private String jbpmProcessId;

    @Column(name = "FINISHED")
    private Date finished;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Assignment getMasterAssignment() {
        return masterAssignment;
    }

    public void setMasterAssignment(Assignment masterAssignment) {
        this.masterAssignment = masterAssignment;
    }

    public String getJbpmProcessId() {
        return jbpmProcessId;
    }

    public void setJbpmProcessId(String jbpmProcessId) {
        this.jbpmProcessId = jbpmProcessId;
    }

    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }
}
