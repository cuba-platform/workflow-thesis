/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;

import java.util.List;

/**
 * @author subbotin
 * @version $Id$
 */
public interface WfAssignmentService {

    String NAME = "workflow_WfAssignmentService";

    /**
     * Reassigns card assignments of a cardin a specified process state
     * to new process actors. Assignments for exist card roles, that are
     * not present in cardRoles parameter would be closed. Assignments for
     * exist card roles, that are present in cardRoles parameter won't be
     * affected. As well, assignments in cardRoles parameter, not related to
     * exist assignments, would be created.
     *
     * @param card      card in a workflow process
     * @param state     process state
     * @param cardRoles list of CardRole entities
     * @param comment   reassignment comment
     */
    void reassign(Card card, String state, List<CardRole> cardRoles, String comment);

}