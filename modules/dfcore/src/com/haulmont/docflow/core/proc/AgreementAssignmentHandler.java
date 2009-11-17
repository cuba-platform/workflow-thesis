/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 13.11.2009 12:33:13
 *
 * $Id$
 */
package com.haulmont.docflow.core.proc;

import org.jbpm.api.task.AssignmentHandler;
import org.jbpm.api.task.Assignable;
import org.jbpm.api.task.Task;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.TaskService;
import com.haulmont.cuba.core.Locator;
import com.haulmont.workflow.core.app.WfEngineMBean;

public class AgreementAssignmentHandler implements AssignmentHandler {

    public void assign(Assignable assignable, OpenExecution execution) throws Exception {
        assignable.setAssignee("agreementuser");

        WfEngineMBean mbean = Locator.lookupMBean(WfEngineMBean.class);
        ProcessEngine pe = mbean.getAPI().getProcessEngine();
        TaskService ts = pe.getTaskService();

        Task task = ts.newTask();
        task.setName("Agreement");
        task.setDescription("execution=" + execution.getId());
        task.setAssignee("testuser");
        ts.saveTask(task);
    }
}
