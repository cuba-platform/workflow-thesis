/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 26.11.2009 17:04:16
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.AssignmentInfo;

import java.util.UUID;

public interface WfService {

    String NAME = "workflow_WfService";

    AssignmentInfo getAssignmentInfo(Card card);

    Card startProcess(Card card);

    void finishAssignment(UUID assignmentId, String outcome, String comment);
}
