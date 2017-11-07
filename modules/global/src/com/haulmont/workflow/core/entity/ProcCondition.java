/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;

import java.util.Collection;

/**
 *
 */
@MetaClass(name = "wf$ProcCondition")
@SystemLevel
public class ProcCondition extends AbstractNotPersistentEntity {
    private static final long serialVersionUID = 2804489532106080184L;

    @MetaProperty
    private Proc proc;

    @MetaProperty
    private Boolean inExpr = true;

    @MetaProperty
    private Collection<ProcState> states;

    public Proc getProc() {
        return proc;
    }

    public void setProc(Proc proc) {
        this.proc = proc;
    }

    public Boolean getInExpr() {
        return inExpr;
    }

    public void setInExpr(Boolean inExpr) {
        this.inExpr = inExpr;
    }

    public Collection<ProcState> getStates() {
        return states;
    }

    public void setStates(Collection<ProcState> states) {
        this.states = states;
    }
}
