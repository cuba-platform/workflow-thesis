/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 20.01.2010 11:37:53
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.annotation.Listeners;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;

import javax.persistence.*;

@Entity(name = "wf$AssignmentAttachment")
@DiscriminatorValue("A")
@Listeners("com.haulmont.workflow.core.listeners.AssignmentAttachmentEntityListener")
@SystemLevel
public class AssignmentAttachment extends Attachment {

    private static final long serialVersionUID = 8490471166699968392L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSIGNMENT_ID")
    protected Assignment assignment;

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }
}