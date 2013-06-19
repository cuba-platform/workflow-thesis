/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
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

    void reassign(Card card, String state, List<CardRole> user, String comment);

}