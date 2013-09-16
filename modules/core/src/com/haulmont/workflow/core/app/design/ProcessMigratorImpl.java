/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app.design;

import com.google.common.base.Preconditions;
import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.*;
import com.haulmont.workflow.core.entity.CardProc;
import com.haulmont.workflow.core.entity.DesignFile;
import com.haulmont.workflow.core.entity.Proc;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ProcessMigratorImpl implements ProcessMigrator {

    private Log log = LogFactory.getLog(ProcessMigratorImpl.class);

    public Result checkMigrationPossibility(UUID designId, UUID procId) {
        Preconditions.checkArgument(designId != null && procId != null, "designId or procId is null");

        log.info("Checking migration possibility for design " + designId + " and process " + procId);

        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Query q = em.createQuery("select cp from wf$CardProc cp where cp.proc.id = ?1 and cp.active = true");
            q.setParameter(1, procId);
            List<CardProc> list = q.getResultList();

            if (list.isEmpty()) {
                tx.commit();
                log.info("Migration is not required");
                return new Result(true, "");
            }
            String jbpmProcessKey = list.get(0).getProc().getJbpmProcessKey();

            tx.commitRetaining();

            // find states of active cards
            Set<String> states = new HashSet<String>();
            for (CardProc cardProc : list) {
                if (cardProc.getState() != null) {
                    String[] strings = cardProc.getState().split(",");
                    for (String string : strings) {
                        if (!StringUtils.isBlank(string))
                            states.add(string);
                    }
                }
            }
            if (states.isEmpty()) {
                log.info("Migration is not required");
                return new Result(true, "");
            }

            // ensure all active states are present in the new proocess
            Set<String> newStates = new HashSet<String>();
            em = PersistenceProvider.getEntityManager();
            q = em.createQuery("select df from wf$DesignFile df where df.design.id = ?1 and df.type = ?2");
            q.setParameter(1, designId);
            q.setParameter(2, "jpdl");
            List<DesignFile> dfList = q.getResultList();

            tx.commit();

            if (dfList.isEmpty()) {
                return new Result(false, "No compiled JPDL found");
            }
            String jpdl = dfList.get(0).getContent();
            Document document = Dom4j.readDocument(jpdl);
            Element root = document.getRootElement();
            for (Element stateElem : Dom4j.elements(root)) {
                if ("custom".equals(stateElem.getName())) {
                    newStates.add(stateElem.attributeValue("name"));
                }
            }
            for (String state : states) {
                if (!newStates.contains(state)) {
                    log.info("Migration is impossible: new process must contain state " + state);
                    return new Result(false, "New process must contain state " + state);
                }
            }

            log.info("Migration is required and possible");
            return new Result(true, "", jbpmProcessKey);
        } finally {
            tx.end();
        }
    }

    public void migrate(UUID designId, UUID procId, String oldJbpmProcessKey) {
        Preconditions.checkArgument(designId != null && procId != null, "designId or procId is null");

        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Proc proc = em.find(Proc.class, procId);

            log.info("Starting migration from " + oldJbpmProcessKey + " to " + proc.getJbpmProcessKey());

            Query q = em.createNativeQuery("update jbpm4_execution set procdefid_ = ?1 where procdefid_ like ?2");
            q.setParameter(1, proc.getJbpmProcessKey() + "-1");
            q.setParameter(2, oldJbpmProcessKey + "-%");
            q.executeUpdate();

            tx.commit();

            log.info("Migration from " + oldJbpmProcessKey + " to " + proc.getJbpmProcessKey() + " has been completed succesfully");
        } finally {
            tx.end();
        }
    }
}
