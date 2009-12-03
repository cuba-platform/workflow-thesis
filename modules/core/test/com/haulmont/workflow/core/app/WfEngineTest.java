/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 10.11.2009 13:45:21
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.Locator;
import com.haulmont.workflow.core.WfTestCase;
import org.jbpm.api.*;
import org.jbpm.api.task.Task;

import java.util.List;

public class WfEngineTest extends WfTestCase {

    public void testGetProcessEngine() {
        WfEngineMBean mBean = Locator.lookupMBean(WfEngineMBean.class, WfEngineMBean.OBJECT_NAME);
        WfEngineAPI wfe = mBean.getAPI();
        ProcessEngine processEngine = wfe.getProcessEngine();
        assertNotNull(processEngine);
    }

    public void testProcess() {
        WfEngineMBean mBean = Locator.lookupMBean(WfEngineMBean.class, WfEngineMBean.OBJECT_NAME);
        String curDir = System.getProperty("user.dir");
        String res = mBean.deployJpdlXml(curDir + "/modules/core/test/process/process1.jpdl.xml");
        assertTrue(res.startsWith("Deployed:"));

        ProcessEngine pe = mBean.getAPI().getProcessEngine();
        ExecutionService es = pe.getExecutionService();
        ProcessInstance pi = es.startProcessInstanceByKey("New_Process_1");
        assertNotNull(pi);
    }


    public void testTask() {
        WfEngineMBean mBean = Locator.lookupMBean(WfEngineMBean.class, WfEngineMBean.OBJECT_NAME);
        String curDir = System.getProperty("user.dir");
        String res = mBean.deployJpdlXml(curDir + "/modules/core/test/process/process2.jpdl.xml");
        assertTrue(res.startsWith("Deployed:"));

        ProcessEngine pe = mBean.getAPI().getProcessEngine();

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
