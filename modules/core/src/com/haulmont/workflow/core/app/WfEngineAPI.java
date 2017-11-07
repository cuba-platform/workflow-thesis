/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.Proc;
import org.jbpm.api.ProcessEngine;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface WfEngineAPI {

    String NAME = "workflow_WfEngine";

    interface Listener {
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

    void signalExecution(String jbpmExecutionId, String transition, Card card);

    void signalExecution(String jbpmExecutionId, String transition, Card card, Map<String, ?> params);

    Card startProcess(Card card);

    Card startProcess(Card card, Card subProcCard);

    void cancelProcess(Card card);

    void deleteNotifications(Card card, int type);

    void addListener(Listener listener);
}