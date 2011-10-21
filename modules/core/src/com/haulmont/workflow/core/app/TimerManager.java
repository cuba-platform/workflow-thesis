/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 26.01.2010 10:34:07
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.app.ClusterManagerAPI;
import com.haulmont.cuba.core.app.ManagementBean;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.core.global.ScriptingProvider;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.TimerEntity;
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

@ManagedBean(TimerManagerAPI.NAME)
public class TimerManager extends ManagementBean implements TimerManagerAPI, TimerManagerMBean {

    private Log log = LogFactory.getLog(TimerManager.class);

    @Inject
    private ClusterManagerAPI clusterManager;

    @Inject
    private WorkCalendarAPI workCalendarAPI;

    public void addTimer(Card card, @Nullable ActivityExecution execution, Date dueDate,
                         Class<? extends TimerAction> taskClass, Map<String, String> taskParams) {
        checkArgument(card != null, "card is null");
        checkArgument(dueDate != null, "dueDate is null");

        EntityManager em = PersistenceProvider.getEntityManager();

        TimerEntity timer = new TimerEntity();
        timer.setCard(card);
        if (execution != null) {
            timer.setJbpmExecutionId(execution.getId());
            timer.setActivity(execution.getActivityName());
        }
        timer.setDueDate(dueDate);

        timer.setActionClass(taskClass.getName());

        Document doc = DocumentHelper.createDocument();
        Dom4j.storeMap(doc.addElement("params"), taskParams);
        timer.setActionParams(Dom4j.writeDocument(doc, true));

        em.persist(timer);
    }

    public void removeTimers(ActivityExecution execution) {
        checkArgument(execution != null, "execution is null");

        EntityManager em = PersistenceProvider.getEntityManager();
        Query q = em.createQuery("delete from wf$Timer t where t.jbpmExecutionId = ?1 and t.activity = ?2");
        q.setParameter(1, execution.getId());
        q.setParameter(2, execution.getActivityName());
        q.executeUpdate();
    }

    public void removeTimers(ActivityExecution execution, Assignment assignment) {
        checkArgument(execution != null, "execution is null");
        checkArgument(assignment != null, "assignment is null");

        EntityManager em = PersistenceProvider.getEntityManager();
        Query q = em.createQuery("select t from wf$Timer t where t.jbpmExecutionId = ?1 and t.activity = ?2");
        q.setParameter(1, execution.getId());
        q.setParameter(2, execution.getActivityName());
        List<TimerEntity> timers =  q.getResultList();

        for (TimerEntity timerEntity : timers) {
            Map<String, String> params = getTimerActionParams(timerEntity.getActionParams());

            EntityLoadInfo entityLoadInfo = EntityLoadInfo.parse(params.get("user"));
            if (entityLoadInfo == null)
                throw new IllegalStateException("No user load info in the parameters map");
            User user = (User) em.find(entityLoadInfo.getMetaClass().getJavaClass(), entityLoadInfo.getId());
            if (user.getId().equals(assignment.getUser().getId())) {
                Query query = em.createQuery("delete from wf$Timer t where t.id = ?1");
                query.setParameter(1, timerEntity.getId());
                query.executeUpdate();
            }
        }
    }

    public void processTimers() {
        if (!AppContext.isStarted() || !clusterManager.isMaster())
            return;

        log.debug("Processing timers");
        try {
            login();

            List<TimerEntity> timers;

            Date currentTime = TimeProvider.currentTimestamp();
            if (!workCalendarAPI.isDateWorkDay(currentTime))
                return;

            Transaction tx = Locator.createTransaction();
            try {
                EntityManager em = PersistenceProvider.getEntityManager();
                Query q = em.createQuery("select t from wf$Timer t where t.dueDate <= ?1 order by t.dueDate desc");
                q.setParameter(1, currentTime);
                timers = q.getResultList();
                tx.commit();
            } finally {
                tx.end();
            }

            for (TimerEntity timer : timers) {
                try {
                    Class<? extends TimerAction> taskClass = ScriptingProvider.loadClass(timer.getActionClass());
                    TimerAction action = taskClass.newInstance();

                    tx = Locator.createTransaction();
                    try {
                        EntityManager em = PersistenceProvider.getEntityManager();
                        TimerEntity t = em.find(TimerEntity.class, timer.getId());

                        TimerActionContext context = new TimerActionContext(t.getCard(), t.getJbpmExecutionId(),
                                t.getActivity(), t.getDueDate(), getTimerActionParams(t.getActionParams()));
                        action.execute(context);

                        em.remove(t);

                        tx.commit();
                    } finally {
                        tx.end();
                    }
                } catch (Throwable e) {
                    log.error("Error firing timer " + timer, e);
                }
            }
        } catch (LoginException e) {
            throw new RuntimeException(e);
        } finally {
            logout();
        }
    }

    private Map<String, String> getTimerActionParams(String actionParams) {
        Map<String, String> map = new HashMap<String, String>();

        Document doc = Dom4j.readDocument(actionParams);
        Dom4j.loadMap(doc.getRootElement().element("map"), map);

        return map;
    }
}
