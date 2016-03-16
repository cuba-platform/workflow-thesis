/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;

import javax.persistence.*;

/**
 */
@Entity(name = "wf$DesignScript")
@Table(name = "WF_DESIGN_SCRIPT")
@NamePattern("%s|name")
@SystemLevel
public class DesignScript extends StandardEntity {

    private static final long serialVersionUID = -1279214573901102955L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESIGN_ID")
    protected Design design;

    @Column(name = "NAME", length = 100, nullable = false)
    protected String name;

    @Column(name = "CONTENT", length = 0)
    protected String content;

    public Design getDesign() {
        return design;
    }

    public void setDesign(Design design) {
        this.design = design;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileName() {
        return "script_" + getId().toString().replace("-", "_") + ".groovy";
    }
}