/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.app.ClusterManagerAPI;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.security.app.Authentication;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.TimerEntity;
import com.haulmont.workflow.core.timer.AssignmentTimersFactory;
import com.haulmont.workflow.core.timer.TimerAction;
import com.haulmont.workflow.core.timer.TimerActionContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.jbpm.api.activity.ActivityExecution;

import javax.annotation.ManagedBean;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author krivopustov
 * @version $Id$
 */
@ManagedBean(TimerManagerAPI.NAME)
public class TimerManager implements TimerManagerAPI {

    private Log log = LogFactory.getLog(TimerManager.class);

    @Inject
    private ClusterManagerAPI clusterManager;

    @Inject
    private Persistence persistence;

    @Inject
    private TimeSource timeSource;

    @Inject
    private Metadata metadata;

    @Inject
    private Scripting scripting;

    @Inject
    protected Authentication authentication;

    @Override
    public void addTimer(Card card, @Nullable ActivityExecution execution, Date dueDate,
                         Class<? extends AssignmentTimersFactory> factoryClass, Class<? extends TimerAction> taskClass,
                         Map<String, String> taskParams) {
        checkArgument(card != null, "card is null");
        checkArgument(dueDate != null, "dueDate is null");

        EntityManager em = persistence.getEntityManager();

        TimerEntity timer = metadata.create(TimerEntity.class);
        timer.setCard(card);
        if (execution != null) {
            timer.setJbpmExecutionId(execution.getId());
            timer.setActivity(execution.getActivityName());
        }
        timer.setDueDate(dueDate);

        timer.setActionClass(taskClass.getName());
        timer.setFactoryClass(factoryClass.getName());

        Document doc = DocumentHelper.createDocument();
        Dom4j.storeMap(doc.addElement("params"), taskParams);
        timer.setActionParams(Dom4j.writeDocument(doc, true));

        em.persist(timer);
    }

    @Override
    public void removeTimers(ActivityExecution execution) {
        checkNotNull(execution, "execution is null");

        EntityManager em = persistence.getEntityManager();
        TypedQuery<TimerEntity> q = em.createQuery("select t from wf$Timer t " +
                "where t.jbpmExecutionId = ?1 and t.activity = ?2", TimerEntity.class);
        q.setParameter(1, execution.getId());
        q.setParameter(2, execution.getActivityName());
        List<TimerEntity> timerEntities = q.getResultList();
        for (TimerEntity timerEntity : timerEntities) {
            em.remove(timerEntity);
        }
    }

    @Override
    public void removeTimers(ActivityExecution execution, Assignment assignment) {
        checkNotNull(execution, "execution is null");
        checkNotNull(assignment, "assignment is null");

        EntityManager em = persistence.getEntityManager();
        TypedQuery<TimerEntity> q = em.createQuery(
                "select t from wf$Timer t where t.jbpmExecutionId = ?1 and t.activity = ?2",
                TimerEntity.class
        );
        q.setParameter(1, execution.getId());
        q.setParameter(2, execution.getActivityName());
        List<TimerEntity> timers = q.getResultList();

        for (TimerEntity timerEntity : timers) {
            Map<String, String> params = getTimerActionParams(timerEntity.getActionParams());

            EntityLoadInfo entityLoadInfo = EntityLoadInfo.parse(params.get("user"));
            if (entityLoadInfo == null)
                throw new IllegalStateException("No user load info in the parameters map");
            if (entityLoadInfo.getId().equals(assignment.getUser().getId())) {
                Query query = em.createQuery("select t  from wf$Timer t where t.id = ?1");
                query.setParameter(1, timerEntity.getId());
                TimerEntity timer = (TimerEntity) query.getFirstResult();
                em.remove(timer);
            }
        }
    }

    @Override
    public void removeTimers(String jbpmExecutionId) {
        checkNotNull(jbpmExecutionId, "jbpmExecutionId is null");

        EntityManager em = persistence.getEntityManager();
        TypedQuery<TimerEntity> q = em.createQuery(
                "select t from wf$Timer t where t.jbpmExecutionId = ?1",
                TimerEntity.class
        );
        q.setParameter(1, jbpmExecutionId);
        List<TimerEntity> timers = q.getResultList();

        for (TimerEntity timerEntity : timers) {
            Query query = em.createQuery("select t from wf$Timer t where t.id = ?1");
            query.setParameter(1, timerEntity.getId());
            TimerEntity timer = (TimerEntity) query.getFirstResult();
            em.remove(timer);
        }
    }

    @Override
    public void processTimers() {
        if (!AppContext.isStarted() || !clusterManager.isMaster())
            return;

        log.debug("Processing timers");
        authentication.begin();
        try {
            List<TimerEntity> timers = loadTimers(timeSource.currentTimestamp());

            for (TimerEntity timer : timers) {
                try {
                    processTimer(timer);
                } catch (Throwable e) {
                    log.error("Error firing timer " + timer, e);
                }
            }
        } finally {
            authentication.end();
        }
    }

    @Override
    public void processTimer(TimerEntity timer) {
        Transaction tx = persistence.createTransaction();
        try {
            Class<? extends TimerAction> taskClass = scripting.loadClass(timer.getActionClass());
            if (taskClass == null) {
                log.error(String.format("Can not find class %s for timer %s", timer.getActionClass(), timer));
                return;
            }

            TimerAction action = taskClass.newInstance();
            EntityManager em = persistence.getEntityManager();
            TimerEntity t = em.find(TimerEntity.class, timer.getId());

            //The timer can be concurrently deleted so we need to check if it still exist in a db
            if (t != null) {
                TimerActionContext context = new TimerActionContext(t.getCard(), t.getJbpmExecutionId(),
                        t.getActivity(), t.getDueDate(), getTimerActionParams(t.getActionParams()));
                action.execute(context);
                // Delete timer using native query to avoid optimistic lock
                // if timer was deleted during timer action execution
                em.createNativeQuery("delete from WF_TIMER where ID = ?1")
                        .setParameter(1, t.getId())
                        .executeUpdate();
                tx.commit();
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Timer action's class instantiation exception: ", e);
        } finally {
            tx.end();
        }
    }

    private List<TimerEntity> loadTimers(Date currentTime) {
        List<TimerEntity> timers;

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<TimerEntity> q = em.createQuery(
                    "select t from wf$Timer t where t.dueDate <= ?1 order by t.dueDate desc",
                    TimerEntity.class
            );

            q.setParameter(1, currentTime);
            timers = q.getResultList();
            tx.commit();
        } finally {
            tx.end();
        }
        return timers;
    }

    private Map<String, String> getTimerActionParams(String actionParams) {
        Map<String, String> map = new HashMap<>();

        Document doc = Dom4j.readDocument(actionParams);
        Dom4j.loadMap(doc.getRootElement().element("map"), map);

        return map;
    }
}
