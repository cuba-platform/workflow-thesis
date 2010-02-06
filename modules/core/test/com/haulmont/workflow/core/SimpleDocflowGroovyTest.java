/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 13.11.2009 12:59:30
 *
 * $Id$
 */
package com.haulmont.workflow.core;

import com.haulmont.cuba.core.*;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.entity.Group;
import com.haulmont.workflow.core.app.WfEngineAPI;
import com.haulmont.workflow.core.app.WfEngineMBean;
import com.haulmont.workflow.core.entity.*;
import junit.framework.Assert;
import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ProcessInstance;

import java.util.List;
import java.util.UUID;

public class SimpleDocflowGroovyTest extends WfTestCase {

    private User adminUser;
    private User agreementUser1;
    private User agreementUser2;
    private User approvalUser;
    private ProcRole initiatorRole;
    private ProcRole agreementRole;
    private ProcRole approvalRole;
    private Card card;
    private CardRole initiator;
    private CardRole agreementMember1;
    private CardRole agreementMember2;
    private CardRole approver;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Transaction tx = Locator.createTransaction();
        try {
            WfEngineAPI mBean = Locator.lookup(WfEngineAPI.NAME);
            String curDir = System.getProperty("user.dir");
            String res = mBean.deployJpdlXml(curDir + "/modules/core/test/process/simple-docflow-groovy.jpdl.xml");
            assertTrue(res, res.startsWith("Deployed:"));

            tx.commitRetaining();

            EntityManager em = PersistenceProvider.getEntityManager();

            // Create users

            adminUser = em.find(User.class, UUID.fromString("60885987-1b61-4247-94c7-dff348347f93"));

            Group group = em.find(Group.class, UUID.fromString("0fa2b1a5-1d68-4d69-9fbd-dff348347f93"));

            Query q;
            List<User> users;
            q = em.createQuery("select u from sec$User u where u.loginLowerCase = ?1");

            q.setParameter(1, "agreementuser1");
            users = q.getResultList();
            if (!users.isEmpty())
                agreementUser1 = users.get(0);
            else {
                agreementUser1 = new User();
                agreementUser1.setGroup(group);
                agreementUser1.setLogin("agreementUser1");
                agreementUser1.setName("Agreement User 1");
                em.persist(agreementUser1);
            }

            q.setParameter(1, "agreementuser2");
            users = q.getResultList();
            if (!users.isEmpty())
                agreementUser2 = users.get(0);
            else {
                agreementUser2 = new User();
                agreementUser2.setGroup(group);
                agreementUser2.setLogin("agreementUser2");
                agreementUser2.setName("Agreement User 2");
                em.persist(agreementUser2);
            }

            q.setParameter(1, "approvaluser");
            users = q.getResultList();
            if (!users.isEmpty())
                approvalUser = users.get(0);
            else {
                approvalUser = new User();
                approvalUser.setGroup(group);
                approvalUser.setLogin("approvalUser");
                approvalUser.setName("Approval User");
                em.persist(approvalUser);
            }

            tx.commitRetaining();

            // Create ProcRoles

            em = PersistenceProvider.getEntityManager();

            q = em.createQuery("select p from wf$Proc p where p.jbpmProcessKey = ?1");
            q.setParameter(1, "SimpleDocflowGroovy");
            List<Proc> processes = q.getResultList();
            if (processes.isEmpty())
                throw new RuntimeException();
            Proc proc = processes.get(0);

            q = em.createQuery("select r from wf$ProcRole r where r.code = ?1");

            List<ProcRole> roles;

            q.setParameter(1, "Initiator");
            roles = q.getResultList();
            if (!roles.isEmpty())
                initiatorRole = roles.get(0);
            else {
                initiatorRole = new ProcRole();
                initiatorRole.setProc(proc);
                initiatorRole.setCode("Initiator");
                initiatorRole.setName("Initiator");
                em.persist(initiatorRole);
            }

            q.setParameter(1, "AgreementMember");
            roles = q.getResultList();
            if (!roles.isEmpty())
                agreementRole = roles.get(0);
            else {
                agreementRole = new ProcRole();
                agreementRole.setProc(proc);
                agreementRole.setCode("AgreementMember");
                agreementRole.setName("Agreement Member");
                em.persist(agreementRole);
            }

            q.setParameter(1, "Approval");
            roles = q.getResultList();
            if (!roles.isEmpty())
                approvalRole = roles.get(0);
            else {
                approvalRole = new ProcRole();
                approvalRole.setProc(proc);
                approvalRole.setCode("Approval");
                approvalRole.setName("Approval");
                em.persist(approvalRole);
            }

            tx.commitRetaining();

            // Create Card

            em = PersistenceProvider.getEntityManager();

            card = new Card();
            card.setProc(proc);
            em.persist(card);

            tx.commitRetaining();

            // Create CardRoles

            em = PersistenceProvider.getEntityManager();

            initiator = new CardRole();
            initiator.setProcRole(initiatorRole);
            initiator.setUser(adminUser);
            initiator.setCard(card);
            em.persist(initiator);

            agreementMember1 = new CardRole();
            agreementMember1.setProcRole(agreementRole);
            agreementMember1.setUser(agreementUser1);
            agreementMember1.setCard(card);
            em.persist(agreementMember1);

            agreementMember2 = new CardRole();
            agreementMember2.setProcRole(agreementRole);
            agreementMember2.setUser(agreementUser2);
            agreementMember2.setCard(card);
            em.persist(agreementMember2);

            approver = new CardRole();
            approver.setProcRole(approvalRole);
            approver.setUser(approvalUser);
            approver.setCard(card);
            em.persist(approver);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    public void test() {
        WfEngineAPI wf = Locator.lookup(WfEngineAPI.NAME);
        ProcessEngine pe = wf.getProcessEngine();
        ExecutionService es = pe.getExecutionService();

        ProcessInstance pi;

        Transaction tx = Locator.createTransaction();
        try {
            pi = es.startProcessInstanceByKey("SimpleDocflowGroovy", card.getId().toString());
            Assert.assertNotNull(pi);

            EntityManager em = PersistenceProvider.getEntityManager();
            card = em.merge(card);
            card.setJbpmProcessId(pi.getId());

            tx.commitRetaining();

            List<Assignment> assignments;
            Assignment assignment;

            // New

            Execution inNew = pi.findActiveExecutionIn("New");
            Assert.assertNotNull(inNew);

            assignments = wf.getUserAssignments("admin");
            assertEquals(1, assignments.size());
            assignment = assignments.get(0);
            assertEquals(card, assignment.getCard());
            wf.finishAssignment(assignment.getId(), "ToAgreement", null);

            tx.commitRetaining();

            // Agreement

            pi = es.findProcessInstanceById(pi.getId());

            Execution inAgreement = pi.findActiveExecutionIn("Agreement");
            Assert.assertNotNull(inAgreement);

            assignments = wf.getUserAssignments("agreementUser1");
            Assert.assertEquals(1, assignments.size());
            assignment = assignments.get(0);
            wf.finishAssignment(assignment.getId(), "Ok", null);

            tx.commitRetaining();

            pi = es.findProcessInstanceById(pi.getId());

            inAgreement = pi.findActiveExecutionIn("Agreement");
            Assert.assertNotNull(inAgreement);

            assignments = wf.getUserAssignments("agreementUser2");
            Assert.assertEquals(1, assignments.size());
            assignment = assignments.get(0);
            wf.finishAssignment(assignment.getId(), "Ok", null);

            tx.commitRetaining();

            // Approval

            pi = es.findProcessInstanceById(pi.getId());

            Execution inApproval = pi.findActiveExecutionIn("Approval");
            Assert.assertNotNull(inApproval);

            assignments = wf.getUserAssignments("approvalUser");
            assertEquals(1, assignments.size());
            assignment = assignments.get(0);
            wf.finishAssignment(assignment.getId(), "Ok", null);

            tx.commitRetaining();

            // End

            pi = es.findProcessInstanceById(pi.getId());
            assertNull(pi);

            tx.commit();
        } finally {
            tx.end();
        }
    }
}