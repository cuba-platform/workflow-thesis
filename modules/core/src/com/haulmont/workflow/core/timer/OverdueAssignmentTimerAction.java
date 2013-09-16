/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.timer;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Locator;
import com.haulmont.cuba.core.PersistenceProvider;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.app.NotificationMatrix;
import com.haulmont.workflow.core.app.NotificationMatrixAPI;
import com.haulmont.workflow.core.entity.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.api.activity.ActivityExecution;

import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class OverdueAssignmentTimerAction extends AssignmentTimerAction {

    private Log log = LogFactory.getLog(GenericAssignmentTimerAction.class);

    protected boolean makesSense(TimerActionContext context, User user) {
        ActivityExecution execution = (ActivityExecution) WfHelper.getExecutionService().findExecutionById(context.getJbpmExecutionId());
        if (execution == null) {
            debug("Execution not found, do nothing", user);
            return false;
        }
        if (!execution.isActive(context.getActivity())) {
            debug("Execution is not in " + context.getActivity() + ", do nothing", user);
            return false;
        }
        return true;
    }

    @Override
    protected void execute(TimerActionContext context, User user) {
        if (!makesSense(context, user))
            return;

        EntityLoadInfo cardRoleLoadInfo = EntityLoadInfo.parse(context.getParams().get("cardRole"));
        if (cardRoleLoadInfo == null)
            throw new IllegalStateException("Card role not found in context params");

        EntityLoadInfo assignmentLoadInfo = EntityLoadInfo.parse(context.getParams().get("assignment"));
        if (assignmentLoadInfo == null)
            throw new IllegalStateException("Assignment not found in context params");

        EntityManager em = PersistenceProvider.getEntityManager();
        CardRole cardRole = (CardRole) em.find(cardRoleLoadInfo.getMetaClass().getJavaClass(), cardRoleLoadInfo.getId());
        Assignment assignment = (Assignment) em.find(assignmentLoadInfo.getMetaClass().getJavaClass(), assignmentLoadInfo.getId());

        Card card = context.getCard();

        NotificationMatrixAPI notificationMatrix = Locator.lookup(NotificationMatrixAPI.NAME);
        notificationMatrix.notifyCardRole(card, cardRole, NotificationMatrix.OVERDUE_CARD_STATE, assignment);
    }

    private void debug(String msg, User user) {
        log.debug(msg + " [" + user + "]");
    }
}
