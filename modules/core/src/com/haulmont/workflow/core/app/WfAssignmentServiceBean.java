/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app;

import com.google.common.base.Preconditions;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.PersistenceProvider;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.cuba.core.global.UserSessionProvider;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.core.global.WfConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

/**
 * @author subbotin
 * @version $Id$
 */
@Service(WfAssignmentService.NAME)
public class WfAssignmentServiceBean implements WfAssignmentService {

    @Inject
    private Persistence persistence;
    @Inject
    private NotificationMatrixAPI notificationMatrixAPI;
    @Inject
    private UserSessionSource userSessionSource;
    @Inject
    private TimeSource timeSource;
    private List<AssignmentListener> listeners = new ArrayList<>();

    public static interface AssignmentListener {

        void createAssignment(Assignment assignment, CardRole cardRole);

        void closeAssignment(Assignment assignment, CardRole cr);

    }

    private static final String FIND_ASSIGNMENTS_BY_STATE_QUERY = "select a from wf$Assignment a where a.card.id = :card and a.name = :state";
    private static final String FIND_MASTER_ASSIGNMENT_QUERY = "select a from wf$Assignment a where a.card.id = :card and a.name = :state " +
            "and a.finished is null and a.masterAssignment is null";
    private static final String FIND_FAMILY_ASSIGNMENT_QUERY = "select a from wf$Assignment a where a.subProcCard.id = :card";
    private static final String DELETE_CARD_INFO_QUERY = "update wf$CardInfo ci set ci.deleteTs = :deleteTs, ci.deletedBy = :deletedBy " +
            "where ci.card.id = :card and ci.user.id = :user";

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
    public void reassign(Card card, String state, List<CardRole> roles, String comment) {
        Preconditions.checkNotNull(card, "Card is null");
        Preconditions.checkState(roles != null && roles.size() > 0, "Roles list is empty");
        Set<User> usersSet = new LinkedHashSet<>();
        EntityManager em = persistence.getEntityManager();
        card = em.find(card.getClass(), card.getId());
        for (CardRole cr : roles)
            usersSet.add(cr.getUser());
        Set<User> assignedUser = new LinkedHashSet<>();
        for (Assignment assignment : getAssignmentsByState(card, state)) {
            if (!usersSet.contains(assignment.getUser()) && assignment.getFinished() == null)
                closeAssignment(assignment, createDummyCardRole(assignment, roles.get(0).getCode()), comment);
            assignedUser.add(assignment.getUser());
        }
        for (CardRole cr : roles) {
            if (!assignedUser.contains(cr.getUser()))
                createAssignment(card, em.find(cr.getClass(), cr.getId()), state);
        }
    }

    protected void closeAssignment(Assignment assignment, CardRole cr, String comment) {
        assignment.setFinished(timeSource.currentTimestamp());
        assignment.setFinishedByUser(userSessionSource.getUserSession().getUser());
        assignment.setComment(comment);
        //TODO: set reassign outcome result on assignment
        fireCloseEvent(assignment, cr);
    }

    protected void createAssignment(Card card, CardRole cr, String state) {
        Assignment assignment = new Assignment();
        assignment.setName(state);
        assignment.setDescription("msg://" + state);
        assignment.setJbpmProcessId(card.getJbpmProcessId());
        assignment.setCard(card);
        assignment.setProc(card.getProc());
        assignment.setUser(cr.getUser());
        assignment.setIteration(1);
        assignment.setMasterAssignment(getMasterAssignment(card, state));
        assignment.setFamilyAssignment(getFamilyAssignment(card));
        persistence.getEntityManager().persist(assignment);
        fireCreateEvent(assignment, cr);
    }

    private void fireCloseEvent(Assignment assignment, CardRole cr) {
        if (listeners != null) {
            for (AssignmentListener listener : listeners)
                listener.closeAssignment(assignment, cr);
        }
    }

    private void fireCreateEvent(Assignment assignment, CardRole cr) {
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
     * @return
     */
    private CardRole createDummyCardRole(Assignment assignment, String code) {
        CardRole cr = new CardRole();
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
        EntityManager em = persistence.getEntityManager();
        Query q = em.createQuery(FIND_ASSIGNMENTS_BY_STATE_QUERY)
                .setParameter("card", card)
                .setParameter("state", state);
        List<Assignment> r = q.getResultList();
        if (r.size() > 1) {
            List<Assignment> filter = new LinkedList<>();
            for (Assignment assignment : r) {
                if (assignment.getMasterAssignment() != null)
                    filter.add(assignment);
            }
            return filter;
        }
        return r;

    }

    protected Assignment getMasterAssignment(Card card, String state) {
        EntityManager em = persistence.getEntityManager();
        Query q = em.createQuery(FIND_MASTER_ASSIGNMENT_QUERY)
                .setParameter("card", card)
                .setParameter("state", state);
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
        Query query = em.createQuery(DELETE_CARD_INFO_QUERY)
                .setParameter("deleteTs", assignment.getFinished())
                .setParameter("deletedBy", userSessionSource.getUserSession().getCurrentOrSubstitutedUser().getLogin())
                .setParameter("card", assignment.getCard())
                .setParameter("user", assignment.getUser());
        query.executeUpdate();
    }
}
