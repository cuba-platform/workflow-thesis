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

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;

@Entity(name = "wf$Proc")
@Table(name = "WF_PROC")
public class Proc extends StandardEntity {

    private static final long serialVersionUID = 7588775221603325166L;

    @Column(name = "NAME", length = 255)
    private String name;

    @Column(name = "JBPM_PROCESS_KEY", length = 255)
    private String jbpmProcessKey;

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
}
