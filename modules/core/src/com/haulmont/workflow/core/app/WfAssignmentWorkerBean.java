/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.activity.HasTimersFactory;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.timer.AssignmentTimersFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.svc.AbstractServiceImpl;
import org.jbpm.pvm.internal.wire.usercode.UserCodeActivityBehaviour;
import org.jbpm.pvm.internal.wire.usercode.UserCodeReference;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.lang.reflect.Field;

@ManagedBean(WfAssignmentWorker.NAME)
public class WfAssignmentWorkerBean implements WfAssignmentWorker {
    private static Log log = LogFactory.getLog(WfAssignmentServiceBean.class);

    protected static final Field customActivityReferenceField;

    static {
        try {
            customActivityReferenceField = UserCodeActivityBehaviour.class.getDeclaredField("customActivityReference");
            customActivityReferenceField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Inject
    protected Metadata metadata;
    @Inject
    protected Persistence persistence;

    @Override
    public Assignment createAssignment(String name, CardRole cardRole, String description,
                                       String jbpmProcessId, User user, Card card, Proc proc,
                                       Integer iteration, Assignment familyAssignment, Assignment master) {
        Assignment assignment = metadata.create(Assignment.class);
        assignment.setName(name);
        if (cardRole != null) assignment.setCardRole(cardRole);
        assignment.setDescription(description);
        assignment.setJbpmProcessId(jbpmProcessId);
        assignment.setUser(user);
        assignment.setCard(card);
        assignment.setProc(proc);
        assignment.setIteration(iteration);
        assignment.setFamilyAssignment(familyAssignment);
        assignment.setMasterAssignment(master);
        return assignment;
    }


    @Override
    public void createTimers(final Assignment assignment) {
        ExecutionService es = WfHelper.getExecutionService();
        final ActivityExecution execution = (ActivityExecution) es.findExecutionById(assignment.getJbpmProcessId());

        Boolean timersCreatedWithAssignerFactory = ((AbstractServiceImpl) es).getCommandService().execute(
                new Command<Boolean>() {
                    @Override
                    public Boolean execute(Environment environment) {
                        return tryToCreateTimersWithFactoryFromAssigner(assignment, execution);
                    }
                }
        );
        if (timersCreatedWithAssignerFactory)
            return;

        try {
            createTimersWithLastUsedFactory(assignment, execution);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    @Override
    public void removeTimers(Assignment assignment) {
        ActivityExecution execution = (ActivityExecution) WfHelper.getExecutionService().findExecutionById(assignment.getJbpmProcessId());
        WfHelper.getTimerManager().removeTimers(execution, assignment);
    }

    protected boolean tryToCreateTimersWithFactoryFromAssigner(Assignment assignment, ActivityExecution execution) {
        ActivityImpl activityImpl = tryToCast(execution.getActivity(), ActivityImpl.class);
        if (activityImpl == null)
            return false;

        UserCodeActivityBehaviour userCodeActivityBehaviour = tryToCast(activityImpl.getActivityBehaviour(), UserCodeActivityBehaviour.class);
        if (userCodeActivityBehaviour == null)
            return false;

        try {
            UserCodeReference userCodeReference = tryToCast(customActivityReferenceField.get(userCodeActivityBehaviour), UserCodeReference.class);
            if (userCodeReference == null)
                return false;

            HasTimersFactory hasTimersFactory = tryToCast(userCodeReference.getObject(execution), HasTimersFactory.class);
            if (hasTimersFactory == null)
                return false;

            AssignmentTimersFactory timersFactory = hasTimersFactory.getTimersFactory();
            if (timersFactory == null)
                return false;

            timersFactory.createTimers(execution, assignment);
            return true;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected <T> T tryToCast(Object object, Class<T> activityClass) {
        if (object == null)
            return null;

        if (activityClass.isInstance(object))
            return activityClass.cast(object);
        else {
            log.debug(String.format("%s is not instanceOf %s", object, activityClass));
            return null;
        }
    }

    protected void createTimersWithLastUsedFactory(Assignment assignment, ActivityExecution execution)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {

        TimerEntity anyTimer = getAnyTimer(execution);
        if (anyTimer == null || StringUtils.isBlank(anyTimer.getFactoryClass()))
            return;

        Class<?> factoryClass = Class.forName(anyTimer.getFactoryClass());
        ((AssignmentTimersFactory) factoryClass.newInstance()).createTimers(execution, assignment);
    }

    protected TimerEntity getAnyTimer(ActivityExecution execution) {
        return persistence.getEntityManager().createQuery(
                "select t from wf$Timer t " +
                        "   where t.jbpmExecutionId = ?1 " +
                        "   and t.activity = ?2", TimerEntity.class)
                .setParameter(1, execution.getId())
                .setParameter(2, execution.getActivityName())
                .setMaxResults(1)
                .getFirstResult();
    }

}
