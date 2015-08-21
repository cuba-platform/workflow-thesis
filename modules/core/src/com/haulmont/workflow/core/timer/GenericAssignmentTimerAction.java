/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.timer;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.app.design.DesignDeployer;
import com.haulmont.workflow.core.entity.Assignment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.api.activity.ActivityExecution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericAssignmentTimerAction extends AssignmentTimerAction {

    private Log log = LogFactory.getLog(GenericAssignmentTimerAction.class);

    public static final String SCRIPT_PATH_PREFIX = "path:";

    private void debug(String msg, User user) {
        log.debug(msg + " [" + user + "]");
    }

    @Override
    protected void execute(TimerActionContext context, User user) {
        if (!makesSense(context, user))
            return;

        String transition = context.getParams().get("transition");
        if (transition != null) {
            takeTransition(context, user, transition);
        } else {
            String script = context.getParams().get("script");
            if (script != null) {
                runScript(context, user, script);
            }
        }

    }

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

    protected void takeTransition(TimerActionContext context, User user, String transition) {
        Assignment assignment = null;

        EntityManager em = AppBeans.get(Persistence.NAME,Persistence.class).getEntityManager();
        Query query = em.createQuery("select a from wf$Assignment a where a.card.id = ?1 and a.finished is null");
        query.setParameter(1, context.getCard());
        List<Assignment> assignments = query.getResultList();
        if (!assignments.isEmpty())
            assignment = assignments.get(0);

        if (assignment != null) {
            debug("Taking transition " + transition, user);
            WfHelper.getEngine().finishAssignment(assignment.getId(), transition, assignment.getComment());
        }
    }

    private void runScript(TimerActionContext context, User user, String script) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("card", context.getCard());
        params.put("dueDate", context.getDueDate());
        params.put("activity", context.getActivity());
        params.put("user", user);
        String fileName;
        if (script.startsWith(SCRIPT_PATH_PREFIX)) {
            fileName = script.substring(SCRIPT_PATH_PREFIX.length());
        } else {
            String processKey = context.getCard().getProc().getJbpmProcessKey();
            fileName = "process/" + processKey + "/" + DesignDeployer.SCRIPTS_DIR + "/" + script;
        }
        debug("Running script " + fileName, user);
        AppBeans.get(Scripting.NAME,Scripting.class).runGroovyScript(fileName, params);
    }
}
