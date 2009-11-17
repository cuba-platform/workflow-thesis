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
import com.haulmont.workflow.core.app.WfEngineMBean;
import junit.framework.Assert;
import org.jbpm.api.*;
import org.jbpm.api.task.Task;

import java.util.List;

public class SimpleDocflowTest extends WfTestCase {

    public void test() {
        WfEngineMBean mBean = Locator.lookupMBean(WfEngineMBean.class, WfEngineMBean.OBJECT_NAME);
        String curDir = System.getProperty("user.dir");
        String res = mBean.deployJpdlXml(curDir + "/modules/core/test/process/simple-docflow.jpdl.xml");
        Assert.assertTrue(res.startsWith("Deployed:"));

        ProcessEngine pe = mBean.getAPI().getProcessEngine();
        ExecutionService es = pe.getExecutionService();
        TaskService ts = pe.getTaskService();

        ProcessInstance pi;

        pi = es.startProcessInstanceByKey("SimpleDocflow");
        Assert.assertNotNull(pi);

        // New

        Execution inNew = pi.findActiveExecutionIn("New");
        Assert.assertNotNull(inNew);

        es.signalExecutionById(pi.getId(), "ToAgreement");

        // Agreement

        pi = es.findProcessInstanceById(pi.getId());

        Execution inAgreement = pi.findActiveExecutionIn("Agreement");
        Assert.assertNotNull(inAgreement);

        List<Task> testuserTaskList = ts.findPersonalTasks("testuser");
        Assert.assertEquals(1, testuserTaskList.size());
        Task testuserTask = testuserTaskList.get(0);
        ts.completeTask(testuserTask.getId());

        List<Task> agreementuserTaskList = ts.findPersonalTasks("agreementuser");
        Assert.assertEquals(1, testuserTaskList.size());
        Task agreementuserTask = agreementuserTaskList.get(0);
        ts.completeTask(agreementuserTask.getId(), "Ok");

        // Approval

        pi = es.findProcessInstanceById(pi.getId());

        Execution inApproval = pi.findActiveExecutionIn("Approval");
        Assert.assertNotNull(inApproval);

        List<Task> approvaluserTaskList = ts.findPersonalTasks("approvaluser");
        Assert.assertEquals(1, approvaluserTaskList.size());

        Task task = approvaluserTaskList.get(0);
        ts.completeTask(task.getId(), "Ok");

        // End

        pi = es.findProcessInstanceById(pi.getId());
        Assert.assertNull(pi);
    }
}
