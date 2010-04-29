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

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.app.ManagementBean;
import com.haulmont.cuba.core.app.ServerConfig;
import com.haulmont.cuba.core.global.ConfigProvider;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.exception.WorkflowException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.jbpm.api.*;

import javax.annotation.ManagedBean;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

@ManagedBean(WfEngineAPI.NAME)
public class WfEngine extends ManagementBean implements WfEngineMBean, WfEngineAPI {

    private Configuration jbpmConfiguration;

    private volatile ProcessEngine processEngine;

    @Resource(name = "jbpmConfiguration")
    public void setJbpmConfiguration(Configuration jbpmConfiguration) {
        this.jbpmConfiguration = jbpmConfiguration;
    }

    public ProcessEngine getProcessEngine() {
        if (processEngine == null) {
            synchronized (this) {
                if (processEngine == null) {
                    Transaction tx = Locator.createTransaction();
                    try {
                        if (processEngine == null) {
                            processEngine = jbpmConfiguration.buildProcessEngine();
                        }
                        tx.commit();
                    } finally {
                        tx.end();
                    }
                }
            }
        }
        return processEngine;
    }

    @Inject
    public void setConfigProvider(ConfigProvider configProvider) {
        if (configProvider.doGetConfig(GlobalConfig.class).isGroovyClassLoaderEnabled())
            System.setProperty("cuba.jbpm.classLoaderFactory", "com.haulmont.cuba.core.global.ScriptingProvider#getGroovyClassLoader");
    }

    public String getJbpmConfigName() {
        String name = AppContext.getProperty(JBPM_CFG_NAME_PROP);
        return name == null ? DEF_JBPM_CFG_NAME : name;
    }

    public String deployJpdlXml(String fileName) {
        Transaction tx = Locator.createTransaction();
        try {
            login();

            RepositoryService rs = getProcessEngine().getRepositoryService();

            NewDeployment deployment = rs.createDeployment();
            File file = new File(fileName);
            if (!file.exists())
                return "File doesn't exist: " + fileName;
            deployment.addResourceFromFile(file);
            deployment.setName(file.getName());
            deployment.setTimestamp(file.lastModified());
            deployment.deploy();

            int dot = file.getName().indexOf(".jpdl.xml");
            String pName = StringUtils.substring(file.getName(), 0, dot);

            ProcessDefinitionQuery pdq = rs.createProcessDefinitionQuery().deploymentId(deployment.getId());
            ProcessDefinition pd = pdq.uniqueResult();

            Proc proc;

            EntityManager em = PersistenceProvider.getEntityManager();
            Query q = em.createQuery("select p from wf$Proc p where p.jbpmProcessKey = ?1");
            q.setParameter(1, pd.getKey());
            List<Proc> processes = q.getResultList();
            if (processes.isEmpty()) {
                proc = new Proc();
                proc.setName(pd.getName());
                proc.setJbpmProcessKey(pd.getKey());
                proc.setMessagesPack("process." + pName);
                proc.setRoles(new ArrayList());
                em.persist(proc);
            } else {
                proc = processes.get(0);
                proc.setName(pd.getName());
                proc.setMessagesPack("process." + pName);
            }

            deployRoles(pd.getDeploymentId(), proc);

            tx.commit();
            return "Deployed: " + deployment;
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        } finally {
            tx.end();
            logout();
        }
    }

    public String deployProcess(String name) {
        String confDir = ConfigProvider.getConfig(GlobalConfig.class).getConfDir();
        String fileName = confDir + "/process/" + name + "/" + name + ".jpdl.xml";
        return deployJpdlXml(fileName);
    }

    private void deployRoles(String deploymentId, Proc proc) {
        Set<String> roles = new HashSet<String>();

        RepositoryService rs = getProcessEngine().getRepositoryService();
        Set<String> resourceNames = rs.getResourceNames(deploymentId);
        for (String resourceName : resourceNames) {
            if (resourceName.endsWith(".jpdl.xml")) {
                InputStream is = rs.getResourceAsStream(deploymentId, resourceName);
                Document doc = Dom4j.readDocument(is);
                Element root = doc.getRootElement();
                for (Element stateElem : Dom4j.elements(root)) {
                    for (Element element : Dom4j.elements(stateElem)) {
                        String name = element.attributeValue("name");
                        if (name != null && "property".equals(element.getName()) &&
                                ("role".equals(name) || "observers".equals(name)))
                        {
                            Element valueElem = element.element("string");
                            String role = valueElem.attributeValue("value");
                            if (!StringUtils.isBlank(role)) {
                                String[] strings = role.split(",");
                                for (String string : strings) {
                                    roles.add(string.trim());
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!roles.isEmpty()) {
            EntityManager em = PersistenceProvider.getEntityManager();
            for (String role : roles) {
                boolean exists = false;
                for (ProcRole procRole : proc.getRoles()) {
                    if (role.equals(procRole.getCode())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    ProcRole procRole = new ProcRole();
                    procRole.setProc(proc);
                    procRole.setCode(role);
                    procRole.setName(role);
                    em.persist(procRole);
                }
            }
        }
    }

    public String printDeployments() {
        Transaction tx = Locator.createTransaction();
        try {
            String result;
            RepositoryService rs = getProcessEngine().getRepositoryService();
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

    public String printDeploymentResource(String id) {
        Transaction tx = Locator.createTransaction();
        try {
            String result;
            RepositoryService rs = getProcessEngine().getRepositoryService();
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

    public String printProcessDefinitions() {
        Transaction tx = Locator.createTransaction();
        try {
            String result;
            RepositoryService rs = getProcessEngine().getRepositoryService();
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

    public String deployTestProcesses() {
        String dir = ConfigProvider.getConfig(ServerConfig.class).getServerConfDir();
        return deployJpdlXml(dir + "/process/test1/test1.jpdl.xml");
    }

    public String startProcessByKey(String key) {
        Transaction tx = Locator.createTransaction();
        try {
            ProcessEngine pe = getProcessEngine();
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

    public List<Assignment> getUserAssignments(UUID userId) {
        return getUserAssignments(userId, null);
    }

    public List<Assignment> getUserAssignments(String userLogin) {
        checkArgument(!StringUtils.isBlank(userLogin), "userLogin is blank");

        User user;
        Transaction tx = Locator.getTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Query q = em.createQuery("select u from sec$User u where u.loginLowerCase = ?1");
            q.setParameter(1, userLogin.toLowerCase());
            List<User> list = q.getResultList();
            if (list.isEmpty())
                throw new RuntimeException("User not found: " + userLogin);
            user = list.get(0);

            tx.commit();
        } finally {
            tx.end();
        }
        return getUserAssignments(user.getId());
    }

    public List<Assignment> getUserAssignments(UUID userId, @Nullable Card card) {
        checkArgument(userId != null, "userId is null");

        Transaction tx = Locator.getTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            String s = "select a from wf$Assignment a where a.user.id = ?1 and a.finished is null";
            if (card != null)
                s = s + " and a.card.id = ?2";
            Query q = em.createQuery(s);
            q.setParameter(1, userId);
            if (card != null)
                q.setParameter(2, card.getId());

            List<Assignment> list = q.getResultList();

            tx.commit();
            return list;
        } finally {
            tx.end();
        }
    }

    public void finishAssignment(UUID assignmentId) {
        finishAssignment(assignmentId, null, null);
    }

    public void finishAssignment(UUID assignmentId, String outcome, String comment) {
        checkArgument(assignmentId != null, "assignmentId is null");

        Transaction tx = Locator.getTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Assignment assignment = em.find(Assignment.class, assignmentId);
            if (assignment == null)
                throw new RuntimeException("Assignment not found: " + assignmentId);

            assignment.setFinished(TimeProvider.currentTimestamp());
            assignment.setFinishedByUser(SecurityProvider.currentUserSession().getUser());
            assignment.setOutcome(outcome);
            assignment.setComment(comment);

            ExecutionService es = getProcessEngine().getExecutionService();
            ProcessInstance pi = es.findProcessInstanceById(assignment.getJbpmProcessId());
            //if process is over
            if (pi == null)
                throw new WorkflowException(WorkflowException.Type.NO_ACTIVE_EXECUTION, "No active execution in " + assignment.getName());
            Execution execution = pi.findActiveExecutionIn(assignment.getName());
            if (execution == null)
                throw new WorkflowException(WorkflowException.Type.NO_ACTIVE_EXECUTION, "No active execution in " + assignment.getName());

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("assignment", assignment);

            pi = es.signalExecutionById(execution.getId(), outcome, params);

            if (Execution.STATE_ENDED.equals(pi.getState())) {
                Proc proc = assignment.getCard().getProc();
                for (CardProc cp : assignment.getCard().getProcs()) {
                    if (cp.getProc().equals(proc)) {
                        cp.setActive(false);
                    }
                }
                assignment.getCard().setJbpmProcessId(null);
            }

            tx.commit();
        } finally {
            tx.end();
        }
    }
}
