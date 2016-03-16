/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.NamePattern;

import javax.persistence.*;

/**
 *
 */
@Entity(name = "wf$DesignProcessVariable")
@Table(name = "WF_DESIGN_PROCESS_VARIABLE")
@NamePattern("%s|name")
public class DesignProcessVariable extends AbstractProcessVariable {
    private static final long serialVersionUID = -7284883853509559184L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESIGN_ID")
    private Design design;

    @Column(name = "SHOULD_BE_OVERRIDDEN")
    private Boolean shouldBeOverridden = Boolean.FALSE;

    public Design getDesign() {
        return design;
    }

    public void setDesign(Design design) {
        this.design = design;
    }

    public Boolean getShouldBeOverridden() {
        return shouldBeOverridden;
    }

    public void setShouldBeOverridden(Boolean shouldBeOverridden) {
        this.shouldBeOverridden = shouldBeOverridden;
    }
}
