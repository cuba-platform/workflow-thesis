/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;

import java.util.Collection;

/**
 * <p>$Id$</p>
 *
 * @author pavlov
 */
@MetaClass(name = "wf$ProcCondition")
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