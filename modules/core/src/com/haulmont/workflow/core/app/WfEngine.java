/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 10.11.2009 12:11:01
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.Locator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jbpm.api.*;

import javax.naming.NamingException;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class WfEngine implements WfEngineMBean, WfEngineAPI {

    public WfEngineAPI getAPI() {
        return this;
    }

    public String deployJpdlXml(String fileName) {
        try {
            RepositoryService rs = getProcessEngine().getRepositoryService();
            NewDeployment deployment = rs.createDeployment();
            File file = new File(fileName);
            if (!file.exists())
                return "File doesn't exist: " + fileName;
            deployment.addResourceFromFile(file);
            deployment.setName(file.getName());
            deployment.setTimestamp(file.lastModified());
            deployment.deploy();
            return "Deployed: " + deployment;
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        }
    }

    public String printDeployments() {
        try {
            RepositoryService rs = getProcessEngine().getRepositoryService();
            List<Deployment> list = rs.createDeploymentQuery().orderAsc(DeploymentQuery.PROPERTY_TIMESTAMP).list();
            if (list.isEmpty())
                return "No deployments found";
            else {
                StringBuilder sb = new StringBuilder();
                for (Deployment deployment : list) {
                    sb.append("Id=").append(deployment.getId()).append("\n");
                    sb.append("Name=").append(deployment.getName()).append("\n");
                    sb.append("State=").append(deployment.getState()).append("\n");
                    sb.append("Timestamp=").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(deployment.getTimestamp()))).append("\n");
                    sb.append("\n");
                }
                return sb.toString();
            }
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        }
    }

    public String printDeploymentResource(String id) {
        try {
            RepositoryService rs = getProcessEngine().getRepositoryService();
            Set<String> resourceNames = rs.getResourceNames(id);
            if (resourceNames.isEmpty())
                return "No resources found";
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
                return sb.toString();
            }
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        }
    }

    public String printProcessDefinitions() {
        try {
            RepositoryService rs = getProcessEngine().getRepositoryService();
            List<ProcessDefinition> list = rs.createProcessDefinitionQuery().orderAsc(ProcessDefinitionQuery.PROPERTY_ID).list();
            if (list.isEmpty())
                return "No deployments found";
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
                return sb.toString();
            }
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        }
    }

    public ProcessEngine getProcessEngine() {
        try {
            ProcessEngine pe = (ProcessEngine) Locator.getJndiContext().lookup("java:/ProcessEngine");
            return pe;
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
}
