/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.workflow.core.activity.ActivityHelper;
import com.haulmont.workflow.core.entity.Card;
import org.jbpm.api.Execution;

import java.io.Serializable;
import java.util.List;

/**
 */
public class WfOperations implements Serializable {

    /**
     * Finds users for current card(bounded to execution) with specified process role
     * Used in EL expression in jpdl
     *
     * @param execution jbpm execution
     * @param roleCode
     * @return list of users ids
     */
    public static List<String> getUsersByProcRole(Execution execution, String roleCode) {
        Card card = ActivityHelper.findCard(execution);
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        Query q = em.createQuery("select cr.user.id from wf$CardRole cr where cr.card.id = ?1 and cr.procRole.code = ?2 and cr.procRole.proc.id = ?3 order by cr.sortOrder, cr.createTs")
                .setParameter(1, card)
                .setParameter(2, roleCode)
                .setParameter(3, card.getProc());
        return q.getResultList();
    }

    /**
     * Finds users count for current card(bounded to execution) with specified process role
     * Used in EL expression in jpdl
     *
     * @param execution jbpm execution
     * @param roleCode
     * @return count of users
     */
    public static int getUserCntByProcRole(Execution execution, String roleCode) {
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        Card card = ActivityHelper.findCard(execution);
        Query q = em.createQuery("select count(cr) from wf$CardRole cr where cr.card.id = ?1 and cr.procRole.code = ?2 and cr.procRole.proc.id = ?3")
                .setParameter(1, card)
                .setParameter(2, roleCode)
                .setParameter(3, card.getProc());
        return Integer.parseInt(q.getSingleResult().toString());
    }
}
