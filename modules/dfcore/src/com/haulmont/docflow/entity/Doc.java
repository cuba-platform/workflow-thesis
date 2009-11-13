/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 12.11.2009 18:03:20
 *
 * $Id$
 */
package com.haulmont.docflow.entity;

import com.haulmont.workflow.core.entity.Card;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity(name = "df$Doc")
@Table(name = "DF_DOC")
@Inheritance(strategy = InheritanceType.JOINED)
public class Doc extends Card {

    private static final long serialVersionUID = 8152910192913195923L;

    @Column(name = "DOC_NUM", length = 50)
    private String docNum;

    @Column(name = "DOC_DATE")
    private Date docDate;

    @OneToMany(mappedBy = "doc", fetch = FetchType.LAZY)
    private Set<DocRole> docRoles;

    public String getDocNum() {
        return docNum;
    }

    public void setDocNum(String docNum) {
        this.docNum = docNum;
    }

    public Date getDocDate() {
        return docDate;
    }

    public void setDocDate(Date docDate) {
        this.docDate = docDate;
    }

    public Set<DocRole> getDocRoles() {
        return docRoles;
    }

    public void setDocRoles(Set<DocRole> docRoles) {
        this.docRoles = docRoles;
    }
}
