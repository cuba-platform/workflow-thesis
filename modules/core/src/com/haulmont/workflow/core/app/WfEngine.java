/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;

import com.google.common.collect.Sets;
import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.exception.WorkflowException;
import com.haulmont.workflow.core.global.WfConstants;
import org.apache.commons.collections.CollectionUtils;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.haulmont.workflow.core.global.WfConstants.CARD_STATE_CANCELED;

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

        String resource = getJpdlXmlString(resourcePath);
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
                proc = metadata.create(Proc.class);
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

    protected String getJpdlXmlString(String resourcePath) {
        return resources.getResourceAsString(resourcePath);
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
        Map<String, OrderFillingType> multiUserRoles = new HashMap<>();
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

                    if ("foreach".equals(stateElem.getName())) {
                        String inAttributeValue = stateElem.attributeValue("in");
                        if (StringUtils.isNotBlank(inAttributeValue)) {
                            Pattern rolePattern =
                                    Pattern.compile("(#\\{wf:getUsersByProcRole\\(execution, \")(\\w+)(\"\\)\\})");
                            Matcher matcher = rolePattern.matcher(inAttributeValue);
                            if (matcher.matches())
                                roles.add(matcher.group(2));
                        }
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
                                        OrderFillingType orderFillingType = getTypeMultyUserRole(clazz);
                                        if (orderFillingType != null) {
                                            multiUserRoles.put(string.trim(), orderFillingType);
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
                    ProcRole procRole = metadata.create(ProcRole.class);
                    procRole.setProc(proc);
                    procRole.setCode(role);
                    procRole.setName(role);
                    procRole.setSortOrder(++sortOrder);
                    if (WfConstants.CARD_CREATOR.equals(role)) {
                        procRole.setInvisible(true);
                        procRole.setAssignToCreator(true);
                    }
                    if (multiUserRoles.containsKey(role)) {
                        procRole.setMultiUser(true);
                        procRole.setOrderFillingType(multiUserRoles.get(role));
                    }
                    em.persist(procRole);
                }
            }
        }

        if (!states.isEmpty()) {
            if (!states.contains(CARD_STATE_CANCELED))
                states.add(CARD_STATE_CANCELED);

            TypedQuery<ProcState> querySelect = em.createQuery("select p from wf$ProcState p where p.proc.id = ?1",
                    ProcState.class);
            querySelect.setParameter(1, proc.getId());
            List<ProcState> existingProcStates = querySelect.getResultList();

            Set<String> procStateNames = Sets.newHashSet();
            if (CollectionUtils.isNotEmpty(existingProcStates)) {
                for (ProcState procState : existingProcStates)
                    procStateNames.add(procState.getName());
            }

            for (final String state : states) {
                if (CollectionUtils.isEmpty(procStateNames) || !procStateNames.contains(state)) {
                    ProcState procState = metadata.create(ProcState.class);
                    procState.setName(state);
                    procState.setProc(proc);
                    em.persist(procState);
                }
            }

            for (ProcState procState : existingProcStates)
                if (!states.contains(procState.getName()) && !CARD_STATE_CANCELED.equals(procState.getName()))
                    em.remove(procState);

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

    private OrderFillingType getTypeMultyUserRole(String className) {
        if (className == null)
            return null;
        Class<?> parallelClass = scripting.loadClass(PARALLEL_ASSIGMENT_CLASS);
        Class<?> sequentialClass = scripting.loadClass(SEQUENTIAL_ASSIGNER_CLASS);
        Class<?> universalClass = scripting.loadClass(UNIVERSAL_ASSIGNER_CLASS);
        Class<?> currentClass = scripting.loadClass(className);

        if (parallelClass != null && parallelClass.isAssignableFrom(currentClass))
            return OrderFillingType.PARALLEL;
        else if (sequentialClass != null && sequentialClass.isAssignableFrom(currentClass))
            return OrderFillingType.SEQUENTIAL;
        else if (universalClass != null && universalClass.isAssignableFrom(currentClass))
            return OrderFillingType.PARALLEL;

        return null;
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
            Assignment assignment = (Assignment) em.find(metadata.getExtendedEntities().getEffectiveClass(Assignment.class), assignmentId);
            if (assignment == null)
                throw new RuntimeException("Assignment not found: " + assignmentId);

            String cardStateBefore = assignment.getCard().getState();
            Date currentTs = timeSource.currentTimestamp();

            assignment.setFinished(currentTs);
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

                tx.commitRetaining();
                em = persistence.getEntityManager();

                Query query = em.createQuery("select a from wf$Assignment a where a.card.id = ?1 and a.finished is null and a.id <> ?2",
                        metadata.getExtendedEntities().getEffectiveClass(Assignment.class));
                query.setParameter(1, assignment.getCard());
                query.setParameter(2, assignment.getId());
                List<Assignment> assignments = query.getResultList();
                String finishMsg = messages.getMessage(WfEngine.class, "anotherForkBranchFinish.msg");
                for (Assignment assignmentToFinish : assignments) {
                    assignmentToFinish.setComment(finishMsg);
                    assignmentToFinish.setFinished(currentTs);
                }

                String cardStateAfter = ",".equals(assignment.getCard().getState()) ?
                        ",ProcessCompleted," : assignment.getCard().getState();
                assignment.getCard().setState(extractChangedState(cardStateBefore, cardStateAfter));
                em.merge(assignment.getCard());
            }

            tx.commit();
        } finally {
            tx.end();
        }
    }

    public void signalExecution(String jbpmExecutionId, String transition, Card card) {
        signalExecution(jbpmExecutionId, transition, card, null);
    }

    @Override
    public void signalExecution(String jbpmExecutionId, String transition, Card card, Map<String, ?> params) {
        ExecutionService es = getProcessEngine().getExecutionService();
        ProcessInstance pi = es.signalExecutionById(jbpmExecutionId, transition, params);

        if (Execution.STATE_ENDED.equals(pi.getState())) {
            Proc proc = card.getProc();
            for (CardProc cp : card.getProcs()) {
                if (cp.getProc().equals(proc)) {
                    cp.setActive(false);
                }
            }
            card.setJbpmProcessId(null);
        }
    }

    public Card startProcess(Card card) {
        return startProcess(card, null);
    }

    public Card startProcess(Card card, Card subProcCard) {
        Map<String, Object> initialProcessVariables =
                card.getInitialProcessVariables() != null ? card.getInitialProcessVariables() : new HashMap<String, Object>();
        EntityManager em = persistence.getEntityManager();
        card = em.find(Card.class, card.getId());
        if (card.getProc() == null)
            throw new IllegalStateException("Card.proc required");

        if (subProcCard != null) {
            initialProcessVariables.put("subProcCard", subProcCard.getId().toString());
            Map<String, Object> subCardInitialVariables = subProcCard.getInitialProcessVariables();
            if (subCardInitialVariables != null) {
                if (subCardInitialVariables.containsKey("dueDate"))
                    initialProcessVariables.put("subProc_dueDate", subCardInitialVariables.get("dueDate"));
                if (subCardInitialVariables.containsKey("startProcessComment")) {
                    initialProcessVariables.put("startSubProcessComment", subCardInitialVariables.get("startProcessComment"));
                }
            }
        }

        ProcessVariableAPI processVariableAPI = AppBeans.get(ProcessVariableAPI.NAME);
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
            cancelProcessInternal(c, CARD_STATE_CANCELED);
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
        cancelAssignments(card);
        setCanceledState(card);
        if (card.getJbpmProcessId() != null) {
            ProcessInstance processInstance = WfHelper.getExecutionService().findProcessInstanceById(card.getJbpmProcessId());
            //Top process is not canceled when all sub process are canceled
            if (processInstance != null)
                WfHelper.getExecutionService().endProcessInstance(card.getJbpmProcessId(), state);
        }

        card.setJbpmProcessId(null);
        for (Listener listener : listeners) {
            listener.onProcessCancel(card);
        }

        notificationBean.notifyByCard(card, CARD_STATE_CANCELED);
    }

    protected void setCanceledState(Card card) {
        Proc proc = card.getProc();
        for (CardProc cp : card.getProcs()) {
            if (cp.getProc().equals(proc)) {
                cp.setActive(false);
                cp.setState("," + CARD_STATE_CANCELED + ",");
                break;
            }
        }
        card.setState("," + CARD_STATE_CANCELED + ",");
    }

    protected void cancelAssignments(Card card) {
        List<Assignment> assignments = findCardAssignments(card);
        if (CollectionUtils.isNotEmpty(assignments))
            for (Assignment assignment : assignments) {
                if (!CARD_STATE_CANCELED.equals(assignment.getName())) {
                    assignment.setComment(messages.getMessage(card.getProc().getMessagesPack(), "canceledCard.msg"));
                }
                assignment.setFinished(timeSource.currentTimestamp());
            }
    }

    @SuppressWarnings("unchecked")
    protected List<Assignment> findCardAssignments(Card card) {
        EntityManager em = persistence.getEntityManager();
        Query query = em.createQuery("select a from wf$Assignment a where a.card.id = ?1 and a.finished is null",
                metadata.getExtendedEntities().getEffectiveClass(Assignment.class));
        query.setParameter(1, card);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    protected List<Card> findFamilyCards(Card topFamily) {
        EntityManager em = persistence.getEntityManager();
        Query query = em.createQuery("select c from wf$Card c where c.procFamily.card.id = :card and c.procFamily.jbpmProcessId = :procId")
                .setParameter("card", topFamily)
                .setParameter("procId", topFamily.getJbpmProcessId());
        return query.getResultList();
    }

    /**
     * Method is used in cases, when process flow was forked and card got several states.
     * When process is finished in one of the fork branches, we need to know which
     * card state is real and which card states belong to 'dead' fork branches.
     * <p/>
     * If method can't detect actual state, it returns current state
     *
     * @return actual card state
     */
    protected String extractChangedState(String stateBefore, String stateAfter) {
        String[] statesBeforeArray = StringUtils.substring(stateBefore, 1, stateBefore.length() - 1).split(",");
        List<String> statesBefore = Arrays.asList(statesBeforeArray);

        String[] statesAfter = StringUtils.substring(stateAfter, 1, stateAfter.length() - 1).split(",");
        String resultState = null;

        for (String after : statesAfter) {
            if (!statesBefore.contains(after)) {
                resultState = "," + after + ",";
            }
        }

        return resultState != null ? resultState : "";
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }
}
