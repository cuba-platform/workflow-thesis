/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.TypedQuery;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardInfo;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.core.global.WfConstants;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Service(WfAssignmentService.NAME)
public class WfAssignmentServiceBean implements WfAssignmentService {

    @Inject
    protected Persistence persistence;
    @Inject
    protected NotificationMatrixAPI notificationMatrixAPI;
    @Inject
    protected UserSessionSource userSessionSource;
    @Inject
    protected TimeSource timeSource;

    @Inject
    protected Metadata metadata;

    protected List<AssignmentListener> listeners = new ArrayList<>();

    public interface AssignmentListener {

        void createAssignment(Assignment assignment, CardRole cardRole);

        void closeAssignment(Assignment assignment, CardRole cr);
    }

    protected static final String FIND_ASSIGNMENTS_BY_STATE_QUERY = "select a from wf$Assignment a where a.card.id = :card and a.name = :state";
    protected static final String FIND_MASTER_ASSIGNMENT_QUERY = "select a from wf$Assignment a where a.card.id = :card and a.name = :state " +
            "and a.finished is null and a.masterAssignment is null and a.user is null order by a.createTs desc";
    protected static final String FIND_FAMILY_ASSIGNMENT_QUERY = "select a from wf$Assignment a where a.subProcCard.id = :card";

    protected static Comparator<Assignment> BY_CREATE_TS_COMPARATOR = new Comparator<Assignment>() {
        @Override
        public int compare(Assignment a1, Assignment a2) {
            if (a1.getCreateTs() == null && a1.getCreateTs() == null) {
                return 0;
            }
            if (a1.getCreateTs() == null) {
                return -1;
            }
            if (a2.getCreateTs() == null) {
                return 1;
            }
            return a1.getCreateTs().compareTo(a2.getCreateTs());
        }
    };

    private final AssignmentListener notificationMatrixListener = new AssignmentListener() {
        @Override
        public void createAssignment(Assignment assignment, CardRole cr) {
            notificationMatrixAPI.notifyCardRole(assignment.getCard(), cr, WfConstants.CARD_STATE_REASSIGN, assignment);
        }

        @Override
        public void closeAssignment(Assignment assignment, CardRole cr) {
            notificationMatrixAPI.notifyCardRole(assignment.getCard(), cr, WfConstants.CARD_STATE_REASSIGN, assignment);
            deleteNotification(assignment);
        }
    };

    @PostConstruct
    protected void init() {
        listeners.add(notificationMatrixListener);
    }

    @Override
    @Transactional
    public void reassign(Card card, String state, List<CardRole> newRoles, List<CardRole> oldRoles, String comment) {
        Preconditions.checkNotNull(card, "Card is null");
        Preconditions.checkState(newRoles != null && newRoles.size() > 0, "Roles list is empty");
        EntityManager em = persistence.getEntityManager();
        card = em.find(card.getClass(), card.getId());
        Multimap<User, Assignment> assignmentsMap = getAssignmentsMap(card, state);
        for (CardRole cr : newRoles)
            createAssignmentForNewCardRole(card, cr, state, assignmentsMap, oldRoles);

        closeCurrentAssignments(newRoles, assignmentsMap, comment);
    }

    protected Multimap<User, Assignment> getAssignmentsMap(Card card, String state) {
        Multimap<User, Assignment> assignmentsMap = ArrayListMultimap.create();
        for (Assignment assignment : getAssignmentsByState(card, state))
            assignmentsMap.put(assignment.getUser(), assignment);

        return assignmentsMap;
    }

    protected void createAssignmentForNewCardRole(Card card, CardRole cr, String state, Multimap<User, Assignment> assignmentsMap,
                                                  List<CardRole> oldRoles) {
        EntityManager em = persistence.getEntityManager();
        if (assignmentsMap.containsKey(cr.getUser())) {
            if (needCreateAssignmentForCardRole(cr, assignmentsMap, oldRoles))
                createAssignment(card, em.find(cr.getClass(), cr.getId()), state);
        } else if (cr.getUser() != null) {
            createAssignment(card, em.find(cr.getClass(), cr.getId()), state);
        }
    }

    protected boolean needCreateAssignmentForCardRole(CardRole cr, Multimap<User, Assignment> assignmentsMap, List<CardRole> oldRoles) {
        final Assignment lastAssignment = Collections.max(assignmentsMap.get(cr.getUser()), BY_CREATE_TS_COMPARATOR);
        Predicate<CardRole> predicate = new Predicate<CardRole>() {
            @Override
            public boolean apply(@Nullable CardRole input) {
                return input != null && ObjectUtils.equals(lastAssignment.getUser(), input.getUser());
            }
        };

        return lastAssignment.getFinished() != null && !Iterables.any(oldRoles, predicate);
    }

    protected void closeCurrentAssignments(List<CardRole> newRoles, Multimap<User, Assignment> assignmentsMap, String comment) {
        Set<User> usersSet = new LinkedHashSet<>();
        for (CardRole cr : newRoles)
            usersSet.add(cr.getUser());
        for (Assignment assignment : assignmentsMap.values())
            if (!usersSet.contains(assignment.getUser()) && assignment.getFinished() == null)
                closeAssignment(assignment, createDummyCardRole(assignment, newRoles.get(0).getCode()), comment);
    }

    protected void closeAssignment(Assignment assignment, CardRole cr, String comment) {
        assignment.setFinished(timeSource.currentTimestamp());
        assignment.setFinishedByUser(userSessionSource.getUserSession().getUser());
        assignment.setOutcome(WfConstants.ACTION_REASSIGN);
        assignment.setComment(comment);
        fireCloseEvent(assignment, cr);
    }

    protected Assignment createAssignment(Card card, CardRole cr, String state) {
        List<Assignment> assignments = getAssignmentsByState(card, state);
        Date dueDate = null;
        for (Assignment assignment : assignments) {
            if (assignment.getDueDate() != null) {
                dueDate = assignment.getDueDate();
                break;
            }
        }
        Assignment assignment = metadata.create(Assignment.class);
        assignment.setName(state);
        assignment.setDescription("msg://" + state);
        assignment.setJbpmProcessId(card.getJbpmProcessId());
        assignment.setCard(card);
        assignment.setProc(card.getProc());
        assignment.setUser(cr.getUser());
        Assignment master = getMasterAssignment(card, state);
        assignment.setIteration(1);
        assignment.setMasterAssignment(master);
        assignment.setFamilyAssignment(getFamilyAssignment(card));
        if (dueDate != null) {
            assignment.setDueDate(dueDate);
        }
        persistence.getEntityManager().persist(assignment);
        fireCreateEvent(assignment, cr);
        return assignment;
    }

    protected void fireCloseEvent(Assignment assignment, CardRole cr) {
        if (listeners != null) {
            for (AssignmentListener listener : listeners)
                listener.closeAssignment(assignment, cr);
        }
    }

    protected void fireCreateEvent(Assignment assignment, CardRole cr) {
        if (listeners != null) {
            for (AssignmentListener listener : listeners)
                listener.createAssignment(assignment, cr);
        }
    }

    /**
     * Create card role for notify user by email about close assignment
     * Notification by card role don't executed
     *
     * @param code - process role code
     */
    protected CardRole createDummyCardRole(Assignment assignment, String code) {
        CardRole cr = metadata.create(CardRole.class);
        cr.setDeletedBy(userSessionSource.getUserSession().getUser().getLogin());
        cr.setDeleteTs(timeSource.currentTimestamp());
        cr.setNotifyByCardInfo(false);
        cr.setCode(code);
        cr.setUser(assignment.getUser());
        cr.setCard(assignment.getCard());
        return cr;
    }

    @SuppressWarnings("unchecked")
    protected List<Assignment> getAssignmentsByState(Card card, String state) {
        List<Assignment> r = loadAssignmentsByState(card, state);
        Set<Assignment> masterAssignments = new HashSet<>();
        for (Assignment assignment : r) {
            if (assignment.getMasterAssignment() != null) {
                masterAssignments.add(assignment.getMasterAssignment());
            }
        }
        //single assignment
        if (masterAssignments.isEmpty()) {
            for (Assignment assignment : r) {
                if (assignment.getFinished() == null) {
                    return Collections.singletonList(assignment);
                }
            }
        }
        //universal assignment
        Assignment lastMasterAssignment = Collections.max(masterAssignments, BY_CREATE_TS_COMPARATOR);
        List<Assignment> filter = new LinkedList<>();
        for (Assignment assignment : r) {
            if (lastMasterAssignment.equals(assignment.getMasterAssignment()))
                filter.add(assignment);
        }
        return filter;
    }

    @SuppressWarnings("unchecked")
    protected List<Assignment> loadAssignmentsByState(Card card, String state) {
        EntityManager em = persistence.getEntityManager();
        Query q = em.createQuery(FIND_ASSIGNMENTS_BY_STATE_QUERY, metadata.getExtendedEntities().getEffectiveClass(Assignment.class))
                .setParameter("card", card)
                .setParameter("state", state);
        return (List<Assignment>) q.getResultList();
    }

    protected Assignment getMasterAssignment(Card card, String state) {
        EntityManager em = persistence.getEntityManager();
        Query q = em.createQuery(FIND_MASTER_ASSIGNMENT_QUERY)
                .setParameter("card", card)
                .setParameter("state", state);
        q.setMaxResults(1);
        List r = q.getResultList();
        return r.isEmpty() ? null : (Assignment) r.get(0);
    }

    protected Assignment getFamilyAssignment(Card card) {
        EntityManager em = persistence.getEntityManager();
        Query q = em.createQuery(FIND_FAMILY_ASSIGNMENT_QUERY)
                .setParameter("card", card);
        List r = q.getResultList();
        return r.isEmpty() ? null : (Assignment) r.get(0);
    }

    protected void deleteNotification(Assignment assignment) {
        EntityManager em = persistence.getEntityManager();
        TypedQuery<CardInfo> query = em.createQuery("select ci from wf$CardInfo ci where ci.card.id = :card and ci.user.id = :user",
                CardInfo.class);
        query.setParameter("card", assignment.getCard());
        query.setParameter("user", assignment.getUser());
        List<CardInfo> cardInfoList = query.getResultList();
        for (CardInfo ci : cardInfoList)
            em.remove(ci);
    }
}