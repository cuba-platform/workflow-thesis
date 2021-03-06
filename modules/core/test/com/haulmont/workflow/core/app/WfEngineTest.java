/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.Locator;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.workflow.core.WfTestCase;
import com.haulmont.workflow.core.entity.Proc;
import org.jbpm.api.*;
import org.jbpm.api.task.Task;

import java.util.List;

public class WfEngineTest extends WfTestCase {

    public void testGetProcessEngine() {
        WfEngineAPI wfe = Locator.lookup(WfEngineAPI.NAME);
        ProcessEngine processEngine = wfe.getProcessEngine();
        assertNotNull(processEngine);
    }

    public void testProcess() {
        WfEngineAPI mBean = Locator.lookup(WfEngineAPI.NAME);
        String curDir = System.getProperty("user.dir");
        Transaction tx = Locator.createTransaction();
        try {
            Proc res = mBean.deployJpdlXml(curDir + "/modules/core/test/process/process1.jpdl.xml");
            assertTrue(res != null);

            tx.commit();
        } finally {
            tx.end();
        }

        ProcessEngine pe = mBean.getProcessEngine();
        ExecutionService es = pe.getExecutionService();
        ProcessInstance pi = es.startProcessInstanceByKey("New_Process_1");
        assertNotNull(pi);
    }


    public void testTask() {
        WfEngineAPI mBean = Locator.lookup(WfEngineAPI.NAME);
        String curDir = System.getProperty("user.dir");
        Transaction tx = Locator.createTransaction();
        try {
            Proc res = mBean.deployJpdlXml(curDir + "/modules/core/test/process/process2.jpdl.xml");
            assertTrue(res != null);

            tx.commit();
        } finally {
            tx.end();
        }

        ProcessEngine pe = mBean.getProcessEngine();
        ExecutionService es = pe.getExecutionService();
        ProcessInstance pi = es.startProcessInstanceByKey("New_Process_2");
        assertNotNull(pi);

        TaskService ts = pe.getTaskService();
        List<Task> taskList = ts.findPersonalTasks("testuser");
        assertEquals(1, taskList.size());

        Task task = taskList.get(0);
        ts.completeTask(task.getId());
    }
}
