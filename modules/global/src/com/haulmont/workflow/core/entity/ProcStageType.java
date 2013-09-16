/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "wf$ProcStageType")
@Table(name = "WF_PROC_STAGE_TYPE")
@NamePattern("%s|name")
@SystemLevel
public class ProcStageType extends StandardEntity {
    private static final long serialVersionUID = -4894528040675569985L;

    @Column(name = "CODE", length = 200)
    protected String code;

    @Column(name = "NAME", length = 200)
    protected String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
