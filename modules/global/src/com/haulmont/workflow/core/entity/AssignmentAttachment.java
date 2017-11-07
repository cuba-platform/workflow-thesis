/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
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