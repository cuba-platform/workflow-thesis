/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.workflow.core.app.WfEngineAPI;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.Proc;
import junit.framework.Assert;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ProcessInstance;

import java.util.List;

public class NestedTxTest extends WfTestCase {

    private Card card;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Transaction tx = persistence.createTransaction();
        try {
            WfEngineAPI mBean = AppBeans.get(WfEngineAPI.NAME);
            String curDir = System.getProperty("user.dir");
            Proc res = mBean.deployJpdlXml("/process/nested-tx-test.jpdl.xml");
            assertTrue(res != null);

            tx.commitRetaining();

            EntityManager em = persistence.getEntityManager();

            // Create ProcRoles

            em = persistence.getEntityManager();

            Query q = em.createQuery("select p from wf$Proc p where p.jbpmProcessKey = ?1");
            q.setParameter(1, "NestedTxTest");
            List<Proc> processes = q.getResultList();
            if (processes.isEmpty())
                throw new RuntimeException();
            Proc proc = processes.get(0);

            tx.commitRetaining();

            // Create Card

            em = persistence.getEntityManager();

            card = new Card();
            card.setProc(proc);
            em.persist(card);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    public void test() {
        WfEngineAPI wf = AppBeans.get(WfEngineAPI.NAME);
        ProcessEngine pe = wf.getProcessEngine();
        ExecutionService es = pe.getExecutionService();

        ProcessInstance pi;

        Transaction tx = persistence.createTransaction();
        try {
            pi = es.startProcessInstanceByKey("NestedTxTest", card.getId().toString());
            Assert.assertNotNull(pi);

            EntityManager em = persistence.getEntityManager();
            card = em.merge(card);
            card.setJbpmProcessId(pi.getId());

            tx.commit();
        } finally {
            tx.end();
        }
    }
}