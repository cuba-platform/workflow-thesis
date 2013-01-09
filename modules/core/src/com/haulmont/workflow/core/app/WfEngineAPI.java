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
import com.haulmont.workflow.core.entity.Proc;
import org.jbpm.api.ProcessEngine;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface WfEngineAPI {

    String NAME = "workflow_WfEngine";

    public interface Listener {
        void onProcessCancel(Card card);
    }

    Proc deployJpdlXml(String resourcePath, Proc proc);

    Proc deployJpdlXml(String resourcePath);

    ProcessEngine getProcessEngine();

    List<Assignment> getUserAssignments(UUID userId);

    List<Assignment> getUserAssignments(String userLogin);

    List<Assignment> getUserAssignments(UUID userId, @Nullable Card card);

    void finishAssignment(UUID assignmentId);

    void finishAssignment(UUID assignmentId, String outcome, String comment);

    void finishAssignment(UUID assignmentId, String outcome, String comment, Card subProcCard);

    Card startProcess(Card card);

    void cancelProcess(Card card);

    void addListener(Listener listener);
}
