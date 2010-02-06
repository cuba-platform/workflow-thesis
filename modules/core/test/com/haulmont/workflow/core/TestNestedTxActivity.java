/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 06.02.2010 13:53:27
 *
 * $Id$
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
