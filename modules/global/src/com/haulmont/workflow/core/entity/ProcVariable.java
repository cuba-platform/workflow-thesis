/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.NamePattern;

import javax.persistence.*;

/**
 *
 *
 */
@Entity(name = "wf$ProcVariable")
@Table(name = "WF_PROC_VARIABLE")
@NamePattern("%s|name")
public class ProcVariable extends AbstractProcessVariable {

    private static final long serialVersionUID = 7148226953025397242L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_ID")
    private Proc proc;

    public Proc getProc() {
        return proc;
    }

    public void setProc(Proc proc) {
        this.proc = proc;
    }
}
