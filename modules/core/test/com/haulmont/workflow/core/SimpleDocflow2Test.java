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

import com.haulmont.cuba.core.Locator;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.workflow.core.app.WfEngineAPI;
import com.haulmont.workflow.core.app.WfEngineMBean;
import com.haulmont.workflow.core.entity.Assignment;
import junit.framework.Assert;
import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ProcessInstance;

import java.util.List;

public class SimpleDocflow2Test extends WfTestCase {

    public void test() {
        WfEngineMBean mBean = Locator.lookupMBean(WfEngineMBean.class, WfEngineMBean.OBJECT_NAME);
        String curDir = System.getProperty("user.dir");
        String res = mBean.deployJpdlXml(curDir + "/modules/core/test/process/simple-docflow2.jpdl.xml");
        Assert.assertTrue(res.startsWith("Deployed:"));

        WfEngineAPI wf = mBean.getAPI();
        ProcessEngine pe = wf.getProcessEngine();
        ExecutionService es = pe.getExecutionService();

        ProcessInstance pi;

        Transaction tx = Locator.createTransaction();
        try {
            pi = es.startProcessInstanceByKey("SimpleDocflow");
            Assert.assertNotNull(pi);

            tx.commitRetaining();

            // New

            Execution inNew = pi.findActiveExecutionIn("New");
            Assert.assertNotNull(inNew);

            es.signalExecutionById(pi.getId(), "ToAgreement");

            tx.commitRetaining();

            // Agreement

            pi = es.findProcessInstanceById(pi.getId());

            Execution inAgreement = pi.findActiveExecutionIn("Agreement");
            Assert.assertNotNull(inAgreement);

            List<Assignment> assignments = wf.getUserAssignments("admin");
            Assert.assertEquals(1, assignments.size());
            Assignment assignment = assignments.get(0);
            wf.finishAssignment(assignment.getId(), "Ok");

            tx.commitRetaining();

            // Approval

            pi = es.findProcessInstanceById(pi.getId());

            Execution inApproval = pi.findActiveExecutionIn("Approval");
            Assert.assertNotNull(inApproval);

            assignments = wf.getUserAssignments("admin");
            Assert.assertEquals(1, assignments.size());
            assignment = assignments.get(0);
            wf.finishAssignment(assignment.getId(), "Ok");

            tx.commitRetaining();

            // End

            pi = es.findProcessInstanceById(pi.getId());
            Assert.assertNull(pi);

            tx.commit();
        } finally {
            tx.end();
        }
    }
}