/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.workflow.core.activity.CardActivity;
import org.jbpm.api.activity.ActivityExecution;

import java.util.List;

public class TestNestedTxActivity extends CardActivity {

    public void execute(ActivityExecution execution) throws Exception {
        super.execute(execution);
        Persistence persistence = AppBeans.get(Persistence.class);

        EntityManager em = persistence.getEntityManager();
        System.out.println("conn: " + em.getConnection());


        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em1 = persistence.getEntityManager();
            Query query = em1.createQuery("select u from sec$User u");
            List list = query.getResultList();
            System.out.println("nested conn: " + em1.getConnection());

            tx.commit();
        } finally {
            tx.end();
        }
    }
}
