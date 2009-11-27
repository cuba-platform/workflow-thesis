/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 26.11.2009 17:04:16
 *
 * $Id$
 */
package com.haulmont.workflow.core.global;

import com.haulmont.workflow.core.entity.Card;

import javax.ejb.Local;
import java.util.List;
import java.util.UUID;

@Local
public interface WfService {

    String JNDI_NAME = "workflow/WfService";

    AssignmentInfo getAssignmentInfo(Card card);

    Card startProcess(Card card);

    void finishAssignment(UUID assignmentId, String outcome, String comment);
}
