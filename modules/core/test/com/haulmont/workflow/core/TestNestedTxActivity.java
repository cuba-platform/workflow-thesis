/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core;

import com.haulmont.cuba.core.*;
import com.haulmont.workflow.core.activity.CardActivity;
import org.jbpm.api.activity.ActivityExecution;

import java.util.List;

public class TestNestedTxActivity extends CardActivity {

    public void execute(ActivityExecution execution) throws Exception {
        super.execute(execution);
        EntityManager em = PersistenceProvider.getEntityManager();
        System.out.println("conn: " + em.getConnection());


        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em1 = PersistenceProvider.getEntityManager();
            Query query = em1.createQuery("select u from sec$User u");
            List list = query.getResultList();
            System.out.println("nested conn: " + em1.getConnection());

            tx.commit();
        } finally {
            tx.end();
        }
    }
}
