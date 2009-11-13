/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 13.11.2009 12:59:30
 *
 * $Id$
 */
package com.haulmont.docflow;

import com.haulmont.workflow.core.WfTestCase;
import com.haulmont.workflow.core.app.WfEngineMBean;
import com.haulmont.cuba.core.Locator;
import org.jbpm.api.*;
import org.jbpm.api.task.Task;

import java.util.List;

public class SimpleDocflowTest extends WfTestCase {

    public void test() {
        WfEngineMBean mBean = Locator.lookupMBean(WfEngineMBean.class, WfEngineMBean.OBJECT_NAME);
        String curDir = System.getProperty("user.dir");
        String res = mBean.deployJpdlXml(curDir + "/modules/dfcore/src-conf/docflow/process/simple-docflow.jpdl.xml");
        assertTrue(res.startsWith("Deployed:"));

        ProcessEngine pe = mBean.getAPI().getProcessEngine();
        ExecutionService es = pe.getExecutionService();
        TaskService ts = pe.getTaskService();

        ProcessInstance pi;

        pi = es.startProcessInstanceByKey("SimpleDocflow");
        assertNotNull(pi);

        // New

        Execution inNew = pi.findActiveExecutionIn("New");
        assertNotNull(inNew);

        es.signalExecutionById(pi.getId(), "ToAgreement");

        // Agreement

        pi = es.findProcessInstanceById(pi.getId());

        Execution inAgreement = pi.findActiveExecutionIn("Agreement");
        assertNotNull(inAgreement);

        List<Task> testuserTaskList = ts.findPersonalTasks("testuser");
        assertEquals(1, testuserTaskList.size());
        Task testuserTask = testuserTaskList.get(0);
        ts.completeTask(testuserTask.getId());

        List<Task> agreementuserTaskList = ts.findPersonalTasks("agreementuser");
        assertEquals(1, testuserTaskList.size());
        Task agreementuserTask = agreementuserTaskList.get(0);
        ts.completeTask(agreementuserTask.getId(), "Ok");

        // Approval

        pi = es.findProcessInstanceById(pi.getId());

        Execution inApproval = pi.findActiveExecutionIn("Approval");
        assertNotNull(inApproval);

        List<Task> approvaluserTaskList = ts.findPersonalTasks("approvaluser");
        assertEquals(1, approvaluserTaskList.size());

        Task task = approvaluserTaskList.get(0);
        ts.completeTask(task.getId(), "Ok");

        // End

        pi = es.findProcessInstanceById(pi.getId());
        assertNull(pi);
    }
}
