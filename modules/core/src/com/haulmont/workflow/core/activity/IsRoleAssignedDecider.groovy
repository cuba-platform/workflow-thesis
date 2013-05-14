/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 * Author: Konstantin Devyatkin
 * Created: 01.04.2011 12:52:29
 *
 * $Id$
 */



package com.haulmont.workflow.core.activity

import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardRole
import org.jbpm.api.activity.ActivityExecution
import org.jbpm.api.activity.ExternalActivityBehaviour

public class IsRoleAssignedDecider implements ExternalActivityBehaviour {

    private String role;

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
        execution.take(signalName);
    }

    public void execute(ActivityExecution execution) throws Exception {
        Card card = ActivityHelper.findCard(execution);
        card.getRoles().get(0).getCode();
        CardRole cardRole = findRole(card);
        if ((cardRole != null) && (cardRole.getUser() != null)) {
            execution.take("yes");
        } else {
            execution.take("no");
        }
    }

    private CardRole findRole(Card card) {
        for (CardRole cardRole : card.getRoles()) {
            if (cardRole.getCode().equals(role) && cardRole.procRole != null && cardRole.procRole.proc == card.proc) {
                return cardRole;
            }
        }
        return null;
    }

}