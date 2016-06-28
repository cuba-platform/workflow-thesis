/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.timer;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.app.WfMailWorker;
import com.haulmont.workflow.core.entity.CardInfo;
import com.haulmont.workflow.core.entity.Proc;
import groovy.lang.Binding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.api.Execution;

import java.util.HashMap;
import java.util.Map;

public class MainAssignmentTimerAction extends AssignmentTimerAction {

    private Log log = LogFactory.getLog(getClass());

    private boolean makesSense(TimerActionContext context) {
        Execution execution = WfHelper.getExecutionService().findExecutionById(context.getJbpmExecutionId());

        if (execution == null) {
            log.debug("Execution not found, do nothing");
            return false;
        }

        if (!execution.isActive(context.getActivity())) {
            log.debug("Execution is not in " + context.getActivity() + ", do nothing");
            return false;
        }
        return true;
    }

    protected CardInfo createCardInfo(TimerActionContext context, User user, int type, String subject) {
        Metadata metadata = AppBeans.get(Metadata.NAME);

        CardInfo ci = metadata.create(CardInfo.class);
        ci.setCard(context.getCard());
        ci.setType(type);
        ci.setUser(user);
        ci.setJbpmExecutionId(context.getJbpmExecutionId());
        ci.setActivity(context.getActivity());
        ci.setDescription(subject);

        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        em.persist(ci);

        return ci;
    }

    protected void sendEmail(User user, String subject, String body) {
        WfMailWorker wfMailWorker = AppBeans.get(WfMailWorker.NAME);
        try {
            wfMailWorker.sendEmail(user, subject, body);
        } catch (Exception e) {
            log.error("Unable to send email to " + user.getEmail(), e);
        }
    }

    @Override
    protected void execute(TimerActionContext context, User user) {
        if (!makesSense(context)) return;
        String script = context.getCard().getProc().getProcessPath() + "/OverdueAssignmentNotification.groovy";

        String subject;
        String body;
        try {
            Map<String, Object> bindingParams = new HashMap<>();
            bindingParams.put("card", context.getCard());
            bindingParams.put("dueDate", context.getDueDate());
            bindingParams.put("activity", context.getActivity());
            bindingParams.put("user", user);

            Binding binding = new Binding(bindingParams);
            AppBeans.get(Scripting.class).runGroovyScript(script, binding);
            subject = binding.getVariable("subject").toString();
            body = binding.getVariable("body").toString();
        } catch (Exception e) {
            log.error("Unable to evaluate groovy script " + script, e);
            Proc proc = context.getCard().getProc();
            String procStr = proc == null ? null : " of process " + proc.getName();
            subject = "Stage " + context.getActivity() + procStr + " is overdue";
            body = "Stage " + context.getActivity() + procStr + " is overdue";
        }

        createCardInfo(context, user, CardInfo.TYPE_OVERDUE, subject);
        sendEmail(user, subject, body);
    }
}