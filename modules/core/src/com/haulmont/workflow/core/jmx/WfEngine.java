/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.jmx;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.security.app.Authenticated;
import com.haulmont.workflow.core.app.WfEngineAPI;
import com.haulmont.workflow.core.entity.Proc;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jbpm.api.*;

import org.springframework.stereotype.Component;
import javax.inject.Inject;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component("workflow_WfEngineMBean")
public class WfEngine implements WfEngineMBean {

    @Inject
    protected WfEngineAPI engine;

    @Inject
    protected Persistence persistence;

    @Override
    public String getJbpmConfigName() {
        String name = AppContext.getProperty(JBPM_CFG_NAME_PROP);
        return name == null ? DEF_JBPM_CFG_NAME : name;
    }

    @Authenticated
    @Override
    public String deployProcess(String name) {
        Transaction tx = persistence.createTransaction();
        try {
            String resourcePath = "/process/" + name + "/" + name + ".jpdl.xml";
            Proc proc = engine.deployJpdlXml(resourcePath);

            tx.commit();
            return "Deployed process " + proc.getJbpmProcessKey();
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        } finally {
            tx.end();
        }
    }


    @Override
    public String printDeployments() {
        Transaction tx = persistence.createTransaction();
        try {
            String result;
            RepositoryService rs = engine.getProcessEngine().getRepositoryService();
            List<Deployment> list = rs.createDeploymentQuery().orderAsc(DeploymentQuery.PROPERTY_TIMESTAMP).list();
            if (list.isEmpty())
                result = "No deployments found";
            else {
                StringBuilder sb = new StringBuilder();
                for (Deployment deployment : list) {
                    sb.append("Id=").append(deployment.getId()).append("\n");
                    sb.append("Name=").append(deployment.getName()).append("\n");
                    sb.append("State=").append(deployment.getState()).append("\n");
                    sb.append("Timestamp=").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(deployment.getTimestamp()))).append("\n");
                    sb.append("\n");
                }
                result = sb.toString();
            }
            tx.commit();
            return result;
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        } finally {
            tx.end();
        }
    }

    @Override
    public String printDeploymentResource(String id) {
        Transaction tx = persistence.createTransaction();
        try {
            String result;
            RepositoryService rs = engine.getProcessEngine().getRepositoryService();
            Set<String> resourceNames = rs.getResourceNames(id);
            if (resourceNames.isEmpty())
                result = "No resources found";
            else {
                StringBuilder sb = new StringBuilder();
                sb.append("Resources in deployment id=").append(id).append("\n\n");
                for (String resourceName : resourceNames) {
                    sb.append("***** ").append(resourceName).append("\n");
                    if (resourceName.endsWith(".xml")) {
                        InputStream is = rs.getResourceAsStream(id, resourceName);
                        String str = IOUtils.toString(is);
                        sb.append(StringEscapeUtils.escapeXml(str)).append("\n");
                    }
                }
                result = sb.toString();
            }
            tx.commit();
            return result;
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        } finally {
            tx.end();
        }
    }

    @Override
    public String printProcessDefinitions() {
        Transaction tx = persistence.createTransaction();
        try {
            String result;
            RepositoryService rs = engine.getProcessEngine().getRepositoryService();
            List<ProcessDefinition> list = rs.createProcessDefinitionQuery().orderAsc(ProcessDefinitionQuery.PROPERTY_ID).list();
            if (list.isEmpty())
                result = "No deployments found";
            else {
                StringBuilder sb = new StringBuilder();
                for (ProcessDefinition pd : list) {
                    sb.append("Name=").append(pd.getName()).append("\n");
                    sb.append("Key=").append(pd.getKey()).append("\n");
                    sb.append("Id=").append(pd.getId()).append("\n");
                    sb.append("Version=").append(pd.getVersion()).append("\n");
                    sb.append("DeploymentId=").append(pd.getDeploymentId()).append("\n");
                    sb.append("\n");
                }
                result = sb.toString();
            }
            tx.commit();
            return result;
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        } finally {
            tx.end();
        }
    }

    @Override
    public String deployTestProcesses() {
        return deployProcess("test1");
    }

    @Override
    public String startProcessByKey(String key) {
        Transaction tx = persistence.createTransaction();
        try {
            ProcessEngine pe = engine.getProcessEngine();
            ExecutionService es = pe.getExecutionService();
            ProcessInstance pi = es.startProcessInstanceByKey(key);
            tx.commit();
            return "ProcessInstance.id=" + pi.getId();
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        } finally {
            tx.end();
        }
    }
}
