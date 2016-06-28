/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.AssignmentInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface WfWorkerAPI {

    String NAME = "workflow_WfWorker";

    /**
     * Returns the assignment information for the provided card and current user,
     * including list of actions to be executed.
     *
     * @param card card
     * @return assignment information or null if no process is associated with the card or there are no assignments
     * for the current user
     */
    @Nullable
    AssignmentInfo getAssignmentInfo(Card card);

    AssignmentInfo getAssignmentInfo(Assignment assignment, String processId);

    Set<AssignmentInfo> getAssignmentInfos(Card card);

    Map<String, Object> getProcessVariables(Card card);

    void setProcessVariables(Card card, Map<String, Object> variables);

    void setHasAttachmentsInCard(Card card, Boolean hasAttachments);

    List<User> getProcessActors(Card card, String procCode, String cardRoleCode);
}
