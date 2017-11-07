/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
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