/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.cuba.core.entity.EmbeddableEntity;
import com.haulmont.cuba.core.entity.annotation.OnDelete;
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.core.global.DeletePolicy;

import javax.persistence.*;

/**
 * @author subbotin
 * @version $Id$
 */
@Embeddable
@MetaClass(name = "wf$ProcFamily")
@SystemLevel
public class ProcFamily extends EmbeddableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FAMILY_CARD_ID")
    @OnDelete(DeletePolicy.CASCADE)
    protected Card card;

    @Column(name = "FAMILY_JBPM_PROCESS_ID", length = 255)
    protected String jbpmProcessId;

    private static final long serialVersionUID = 1069116616849257355L;

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public String getJbpmProcessId() {
        return jbpmProcessId;
    }

    public void setJbpmProcessId(String jbpmProcessId) {
        this.jbpmProcessId = jbpmProcessId;
    }
}
