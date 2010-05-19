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

import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.AssignmentInfo;

import java.util.Map;
import java.util.UUID;

public interface WfService {

    String NAME = "workflow_WfService";

    AssignmentInfo getAssignmentInfo(Card card);

    Card startProcess(Card card);

    void cancelProcess(Card card);

    void finishAssignment(UUID assignmentId, String outcome, String comment);

    Map<String, Object> getProcessVariables(Card card);

    void setProcessVariables(Card card, Map<String, Object> variables);

    boolean isCurrentUserInProcRole(Card card, String procRoleCode);

    boolean isUserInProcRole(Card card, User user, String procRoleCode);

    void deleteNotifications(Card card, User user);
}
