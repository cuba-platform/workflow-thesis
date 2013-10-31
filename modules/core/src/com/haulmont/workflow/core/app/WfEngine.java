/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.exception.WorkflowException;
import com.haulmont.workflow.core.global.WfConstants;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.jbpm.api.*;
import org.jbpm.pvm.internal.processengine.SpringHelper;

import javax.annotation.ManagedBean;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

@ManagedBean(WfEngineAPI.NAME)
public class WfEngine implements WfEngineAPI {

    private Log log = LogFactory.getLog(getClass());

    private SpringHelper jbpmSpringHelper;

    private volatile ProcessEngine processEngine;

    private Set<Listener> listeners = new LinkedHashSet<Listener>();

    private final String PARALLEL_ASSIGMENT_CLASS = "com.haulmont.workflow.core.activity.ParallelAssigner";
    private final String SEQUENTIAL_ASSIGNER_CLASS = "com.haulmont.workflow.core.activity.SequentialAssigner";
    private final String UNIVERSAL_ASSIGNER_CLASS = "com.haulmont.workflow.core.activity.UniversalAssigner";

    @Inject
    private UserSessionSource userSessionSource;

    @Inject
    private Persistence persistence;

    @Inject
    protected Scripting scripting;

    @Inject
    protected Resources resources;

    @Inject
    protected TimeSource timeSource;

    @Inject
    protected Messages messages;

    @Inject
    private NotificationMatrixAPI notificationBean;

    @Inject
    private Metadata metadata;

    static {
        System.setProperty("cuba.jbpm.classLoaderFactory", "com.haulmont.cuba.core.global.ScriptingProvider#getClassLoader");
    }

    @Resource(name = "jbpmSpringHelper")
    public void setJbpmSpringHelper(SpringHelper jbpmSpringHelper) {
        this.jbpmSpringHelper = jbpmSpringHelper;
    }

    public ProcessEngine getProcessEngine() {
        if (processEngine == null) {
            synchronized (this) {
                if (processEngine == null) {
                    Transaction tx = persistence.createTransaction();
                    try {
                        if (processEngine == null) {
                            processEngine = jbpmSpringHelper.createProcessEngine();
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

    public Proc deployJpdlXml(String resourcePath, Proc proc) {
        RepositoryService rs = getProcessEngine().getRepositoryService();

        NewDeployment deployment = rs.createDeployment();

        String resource = resources.getResourceAsString(resourcePath);
        if (resource == null)
            throw new IllegalArgumentException("Resource not found: " + resourcePath);

        deployment.addResourceFromString(resourcePath, resource);
        deployment.setName(resourcePath.substring(resourcePath.lastIndexOf('/')));
        deployment.setTimestamp(timeSource.currentTimeMillis());
        deployment.deploy();

        ProcessDefinitionQuery pdq = rs.createProcessDefinitionQuery().deploymentId(deployment.getId());
        ProcessDefinition pd = pdq.uniqueResult();

        EntityManager em = persistence.getEntityManager();

        if (proc == null) {
            Query q = em.createQuery("select p from wf$Proc p where p.jbpmProcessKey = ?1");
            q.setParameter(1, pd.getKey());
            List<Proc> processes = q.getResultList();
            if (processes.isEmpty()) {
                proc = new Proc();
                proc.setName(pd.getName());
                proc.setJbpmProcessKey(pd.getKey());
                proc.setCode(pd.getKey());
                proc.setRoles(new ArrayList());
                em.persist(proc);
            } else {
                proc = processes.get(0);
                if (StringUtils.isEmpty(pd.getName()))
                    proc.setName(pd.getName());
            }
        } else {
            proc.setJbpmProcessKey(pd.getKey());
        }

        String mp = StringUtils.substring(resourcePath, 0, resourcePath.lastIndexOf('/')).replace('/', '.');
        if (mp.startsWith("."))
            mp = mp.substring(1);
        proc.setMessagesPack(mp);

        deployProcessStuff(pd.getDeploymentId(), proc);

        log.info("Deployed: key=" + pd.getKey() + ", name=" + proc.getName() + ", id=" + proc.getId());
        return proc;
    }

    public Proc deployJpdlXml(String resourcePath) {
        return deployJpdlXml(resourcePath, null);
    }

    /**
     * Deploys roles, states
     *
     * @param deploymentId
     * @param proc
     */
    private void deployProcessStuff(String deploymentId, Proc proc) {
        Set<String> roles = new HashSet<String>();
        Set<String> states = new HashSet<String>();

        EntityManager em = persistence.getEntityManager();

        RepositoryService rs = getProcessEngine().getRepositoryService();
        Set<String> resourceNames = rs.getResourceNames(deploymentId);
        List<String> multiUserRoles = new ArrayList<String>();
        for (String resourceName : resourceNames) {
            if (resourceName.endsWith(".jpdl.xml")) {
                InputStream is = rs.getResourceAsStream(deploymentId, resourceName);
                Document doc = Dom4j.readDocument(is);
                Element root = doc.getRootElement();
                for (Element stateElem : Dom4j.elements(root)) {
                    String state = stateElem.attributeValue("name");
                    String clazz = stateElem.attributeValue("class");

                    if ("custom".equals(stateElem.getName())) {
                        if (StringUtils.isNotBlank(state))
                            states.add(state);
                    }
                    for (Element element : Dom4j.elements(stateElem)) {
                        String name = element.attributeValue("name");
                        if (name != null && "property".equals(element.getName())) {
                            Element valueElem = element.element("string");
                            if (name.startsWith("role") || "observers".equals(name)) {
                                String role = valueElem.attributeValue("value");
                                if (!StringUtils.isBlank(role)) {
                                    String[] strings = role.split(",");
                                    for (String string : strings) {
                                        roles.add(string.trim());
                                        if (checkMultyUserRole(clazz)) {
                                            multiUserRoles.add(string.trim());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        roles.add(WfConstants.CARD_CREATOR);

        if (!roles.isEmpty()) {
            Set<ProcRole> deletedRoles = findDeletedRoles(proc.getRoles(), roles);
            for (ProcRole deletedRole : deletedRoles) {
                em.remove(deletedRole);
            }
            proc.getRoles().removeAll(deletedRoles);

            int sortOrder = findMaxSortOrder(proc.getRoles());
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
                    procRole.setSortOrder(++sortOrder);
                    if (WfConstants.CARD_CREATOR.equals(role)) {
                        procRole.setInvisible(true);
                        procRole.setAssignToCreator(true);
                    }
                    if (multiUserRoles.contains(role)) {
                        procRole.setMultiUser(true);
                    }
                    em.persist(procRole);
                }
            }
        }

        if (!states.isEmpty()) {
            Query query = em.createQuery("delete from wf$ProcState p where p.proc.id = ?1");
            query.setParameter(1, proc.getId());
            query.executeUpdate();

            for (String state : states) {
                ProcState procState = metadata.create(ProcState.class);
                procState.setName(state);
                procState.setProc(proc);
                em.persist(procState);
            }

            if (!states.contains(WfConstants.CARD_STATE_CANCELED)) {
                ProcState procState = metadata.create(ProcState.class);
                procState.setName(WfConstants.CARD_STATE_CANCELED);
                procState.setProc(proc);
                em.persist(procState);
            }

            String statesStr = states.toString();
            proc.setStates(statesStr.substring(1, statesStr.length() - 1));
        }
    }

    private int findMaxSortOrder(List<ProcRole> roles) {
        int max = 0;
        for (ProcRole role : roles) {
            if (role.getSortOrder() != null && role.getSortOrder() > max)
                max = role.getSortOrder();
        }
        return max;
    }

    private Set<ProcRole> findDeletedRoles(List<ProcRole> oldRoles, Set<String> newRoles) {
        Set<ProcRole> deleted = new HashSet<ProcRole>();
        for (ProcRole oldRole : oldRoles) {
            if (!newRoles.contains(oldRole.getCode()))

                deleted.add(oldRole);
        }
        return deleted;
    }

    private boolean checkMultyUserRole(String className) {
        if (className == null)
            return false;
        Class<?> parallelClass = scripting.loadClass(PARALLEL_ASSIGMENT_CLASS);
        Class<?> sequentialClass = scripting.loadClass(SEQUENTIAL_ASSIGNER_CLASS);
        Class<?> universalClass = scripting.loadClass(UNIVERSAL_ASSIGNER_CLASS);
        Class<?> currentClass = scripting.loadClass(className);

        if (parallelClass != null && parallelClass.isAssignableFrom(currentClass))
            return true;
        else if (sequentialClass != null && sequentialClass.isAssignableFrom(currentClass))
            return true;
        else if (universalClass != null && universalClass.isAssignableFrom(currentClass))
            return true;

        return false;
    }

    public List<Assignment> getUserAssignments(UUID userId) {
        return getUserAssignments(userId, null);
    }

    public List<Assignment> getUserAssignments(String userLogin) {
        checkArgument(!StringUtils.isBlank(userLogin), "userLogin is blank");

        User user;
        Transaction tx = persistence.getTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
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

        Transaction tx = persistence.getTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            String s = "select a from wf$Assignment a where a.user.id = ?1 and a.finished is null";
            if (card != null)
                s = s + " and (a.card.id = ?2 or a.card.procFamily.card.id = ?2)";
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
        finishAssignment(assignmentId, outcome, comment, null);
    }

    public void finishAssignment(UUID assignmentId, String outcome, String comment, Card subProcCard) {
        checkArgument(assignmentId != null, "assignmentId is null");

        Transaction tx = persistence.getTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            Assignment assignment = em.find(Assignment.class, assignmentId);
            if (assignment == null)
                throw new RuntimeException("Assignment not found: " + assignmentId);

            assignment.setFinished(timeSource.currentTimestamp());
            assignment.setFinishedByUser(userSessionSource.getUserSession().getUser());
            assignment.setOutcome(outcome);
            assignment.setComment(comment);
            if (subProcCard != null) {
                assignment.setSubProcCard(subProcCard);
                em.flush();
            }
            ExecutionService es = getProcessEngine().getExecutionService();
            ProcessInstance pi = es.findProcessInstanceById(assignment.getJbpmProcessId());
            //if process is over
            if (pi == null)
                throw new WorkflowException(WorkflowException.Type.NO_ACTIVE_EXECUTION, "No active execution in " + assignment.getName());
            Execution execution = pi.findActiveExecutionIn(assignment.getName());
            if (execution == null)
                throw new WorkflowException(WorkflowException.Type.NO_ACTIVE_EXECUTION, "No active execution in " + assignment.getName());

            Map<String, Object> params = new HashMap<>();
            params.put("assignment", assignment);
            if (subProcCard != null) {
                es.setVariable(execution.getId(), "subProcCard", subProcCard.getId().toString());
                if (comment != null)
                    es.setVariable(execution.getId(), "startSubProcessComment", comment);
                if (subProcCard.getInitialProcessVariables() != null && subProcCard.getInitialProcessVariables().containsKey("dueDate"))
                    es.setVariable(execution.getId(), "subProc_dueDate", subProcCard.getInitialProcessVariables().get("dueDate"));
            }
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

    public Card startProcess(Card card) {
        Map<String, Object> initialProcessVariables = card.getInitialProcessVariables();

        EntityManager em = persistence.getEntityManager();
        card = em.find(Card.class, card.getId());
        if (card.getProc() == null)
            throw new IllegalStateException("Card.proc required");

        ProcessVariableAPI processVariableAPI = Locator.lookup(ProcessVariableAPI.NAME);
        processVariableAPI.createVariablesForCard(card);

        if (card.getJbpmProcessId() != null)
            throw new IllegalStateException("Another process already started");

        ExecutionService es = WfHelper.getExecutionService();
        card.setState(null);
        ProcessInstance pi = es.startProcessInstanceByKey(
                card.getProc().getJbpmProcessKey(),
                initialProcessVariables,
                card.getId().toString()
        );
        card.setJbpmProcessId(pi.getId());

        for (CardProc cardProc : card.getProcs())
            if (card.getProc().equals(cardProc.getProc())) {
                cardProc.setJbpmProcessId(pi.getId());
                cardProc.setActive(true);
                cardProc.setStartCount((Integer) ObjectUtils.defaultIfNull(cardProc.getStartCount(), 0) + 1);
            }

        if (Execution.STATE_ENDED.equals(pi.getState())) {
            card.setJbpmProcessId(null);
        }

        return card;
    }

    public void cancelProcess(Card card) {
        EntityManager em = persistence.getEntityManager();
        Card c = em.merge(card);
        if (c.isSubProcCard())
            //cancel sub process. sub state should be only "ended".
            //This is restriction of jBPM
            cancelProcessInternal(c, Execution.STATE_ENDED);
        else {
            for (Card subProcCard : findFamilyCards(c))
                cancelProcessInternal(subProcCard, Execution.STATE_ENDED);
            cancelProcessInternal(c, WfConstants.CARD_STATE_CANCELED);
        }
        deleteNotifications(card, CardInfo.TYPE_NOTIFICATION);
    }

    @Override
    public void deleteNotifications(Card card, int type) {
        EntityManager em = persistence.getEntityManager();
        em.createQuery("delete from wf$CardInfo ci where ci.card.id = :cardId and ci.type = :type")
                .setParameter("cardId", card)
                .setParameter("type", type)
                .executeUpdate();
    }

    public void cancelProcessInternal(Card card, String state) {
        EntityManager em = persistence.getEntityManager();
        Query query = em.createQuery("select a from wf$Assignment a where a.card.id = ?1 and a.finished is null");
        query.setParameter(1, card);
        List<Assignment> assignments = query.getResultList();
        for (Assignment assignment : assignments) {
            if (!WfConstants.CARD_STATE_CANCELED.equals(assignment.getName())) {
                assignment.setComment(messages.getMessage(card.getProc().getMessagesPack(), "canceledCard.msg"));
            }
            assignment.setFinished(timeSource.currentTimestamp());
            if (assignment.getFinishedByUser() == null) {
                assignment.setFinishedByUser(userSessionSource.getUserSession().getCurrentOrSubstitutedUser());
            }
        }

        if (card.getJbpmProcessId() != null) {
            ProcessInstance processInstance = WfHelper.getExecutionService().findProcessInstanceById(card.getJbpmProcessId());
            //Top process is not canceled when all sub process are canceled
            if (processInstance != null)
                WfHelper.getExecutionService().endProcessInstance(card.getJbpmProcessId(), state);
        }

        Proc proc = card.getProc();
        for (CardProc cp : card.getProcs()) {
            if (cp.getProc().equals(proc)) {
                cp.setActive(false);
                cp.setState("," + WfConstants.CARD_STATE_CANCELED + ",");
                break;
            }
        }
        card.setJbpmProcessId(null);
        card.setState("," + WfConstants.CARD_STATE_CANCELED + ",");
        for (Listener listener : listeners) {
            listener.onProcessCancel(card);
        }

        notificationBean.notifyByCard(card, WfConstants.CARD_STATE_CANCELED);
    }

    protected List<Card> findFamilyCards(Card topFamily) {
        EntityManager em = persistence.getEntityManager();
        Query query = em.createQuery("select c from wf$Card c where c.procFamily.card.id = :card and c.procFamily.jbpmProcessId = :procId")
                .setParameter("card", topFamily)
                .setParameter("procId", topFamily.getJbpmProcessId());
        return query.getResultList();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }
}
