/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.Messages;

import javax.persistence.*;

/**
 * <p>$Id$</p>
 *
 * @author pavlov
 */
@Entity(name = "wf$ProcState")
@Table(name = "WF_PROC_STATE")
@NamePattern("%s|locName")
public class ProcState extends StandardEntity {
    private static final long serialVersionUID = 1544054162497523936L;

    @Column(name = "NAME")
    protected String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_ID")
    protected Proc proc;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Proc getProc() {
        return proc;
    }

    public void setProc(Proc proc) {
        this.proc = proc;
    }

    @MetaProperty
    public String getLocName() {
        if (proc != null) {
            return MessageProvider.getMessage(proc.getMessagesPack(), name);
        }

        return name;
    }

    @Override
    public String toString() {
        return getLocName();
    }
}
