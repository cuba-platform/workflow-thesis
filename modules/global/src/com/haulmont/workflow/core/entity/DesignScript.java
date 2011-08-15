/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 27.01.11 13:14
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.*;

@Entity(name = "wf$DesignScript")
@Table(name = "WF_DESIGN_SCRIPT")
@NamePattern("%s|name")
public class DesignScript extends StandardEntity {

    private static final long serialVersionUID = -1279214573901102955L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESIGN_ID")
    protected Design design;

    @Column(name = "NAME", length = 100)
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
