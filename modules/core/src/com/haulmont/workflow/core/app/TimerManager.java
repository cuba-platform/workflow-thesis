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
import com.haulmont.cuba.core.global.ScriptingProvider;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.core.sys.ServerSecurityUtils;
import com.haulmont.cuba.security.global.LoginException;
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

    private SecurityContext securityContext;

    private Log log = LogFactory.getLog(TimerManager.class);

    private ClusterManagerAPI clusterManager;

    @Inject
    public void setClusterManager(ClusterManagerAPI clusterManager) {
        this.clusterManager = clusterManager;
    }

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

    public void processTimers() {
        if (!AppContext.isStarted())
            return;

        if (!clusterManager.isMaster())
            return;

        log.info("Processing timers");
        try {
            if (securityContext != null)
                ServerSecurityUtils.setSecurityAssociation(securityContext.getUser(), securityContext.getSessionId());
            login();
            securityContext = ServerSecurityUtils.getSecurityAssociation();
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }

        List<TimerEntity> timers;

        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Query q = em.createQuery("select t from wf$Timer t where t.dueDate <= ?1 order by t.dueDate desc");
            q.setParameter(1, TimeProvider.currentTimestamp());
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
    }

    private Map<String, String> getTimerActionParams(String actionParams) {
        Map<String, String> map = new HashMap<String, String>();

        Document doc = Dom4j.readDocument(actionParams);
        Dom4j.loadMap(doc.getRootElement().element("map"), map);

        return map;
    }
}
