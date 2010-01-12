/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 10.11.2009 12:10:36
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.Execution;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface WfEngineAPI {

    String NAME = "workflow_WfEngine";

    String deployJpdlXml(String fileName);

    ProcessEngine getProcessEngine();

    List<Assignment> getUserAssignments(UUID userId);

    List<Assignment> getUserAssignments(String userLogin);

    List<Assignment> getUserAssignments(UUID userId, @Nullable Card card);

    void finishAssignment(UUID assignmentId);

    void finishAssignment(UUID assignmentId, String outcome, String comment);
}
