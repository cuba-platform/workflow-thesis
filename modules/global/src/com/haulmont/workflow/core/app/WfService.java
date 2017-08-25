/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardInfo;
import com.haulmont.workflow.core.entity.TimerEntity;
import com.haulmont.workflow.core.global.AssignmentInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface WfService {

    String NAME = "workflow_WfService";

    /**
     * Returns the assignment information for the provided card and current user,
     * including list of actions to be executed.
     *
     * @param card card
     * @return assignment information or null if no process is associated with the card or there are no assignments
     * for the current user
     */
    @Nullable
    AssignmentInfo getAssignmentInfo(Card card);


    /**
     * Creates assignment info for the provided assignment and process id
     *
     * @param assignment assignment
     * @param processId process id
     * @return assignment information created from provided assignment
     */
    AssignmentInfo getAssignmentInfo(Assignment assignment, String processId);

    /**
     * Returns the assignments information for the provided card and current user,
     * including list of actions to be executed.
     *
     * @param card card
     * @return assignments information created from provided card
     */
    Set<AssignmentInfo> getAssignmentInfos(Card card);

    /**
     * Returns the assignments information for the provided card and user,
     * including list of actions to be executed.
     *
     * @param card   card
     * @param userId user id
     * @return assignments information created from provided card and user
     */
    Set<AssignmentInfo> getAssignmentInfos(Card card, UUID userId);

    /**
     * Starts a workflow process for a given card.
     *
     * @param card card
     * @return card
     */
    Card startProcess(Card card);


    /**
     * Do the same as <code>WfService.startProcess(Card card)</code>,
     * but specifies a sub process card that will be used
     *
     * @param card card
     * @param subProcCard sub process card
     *
     * */
    Card startProcess(Card card, Card subProcCard);

    /**
     * Aborts a workflow process for a given card.
     *
     * @param card card
     */
    void cancelProcess(Card card);

    /**
     * Finishes an assignment with a specified id with a given result and comment.
     *
     * @param assignmentId assignment id
     * @param outcome      assignment outcome result
     * @param comment      finish assignment comment
     */
    void finishAssignment(UUID assignmentId, String outcome, String comment);

    /**
     * Do the same as <code>WfService.finishAssignment(UUID assignmentId, String outcome, String comment)</code>,
     * but specifies a sub process card that will be linked to the assignment
     *
     * @param assignmentId
     * @param outcome
     * @param comment
     * @param subProcCard
     */
    void finishAssignment(UUID assignmentId, String outcome, String comment, Card subProcCard);

    /**
     * Gets jbpm process variables, associated with a given card.
     *
     * @param card card
     * @return process variables
     */
    Map<String, Object> getProcessVariables(Card card);

    /**
     * Sets specified variables to a jbpm process, associated with a card.
     *
     * @param card      card
     * @param variables process variables
     */
    void setProcessVariables(Card card, Map<String, Object> variables);

    /**
     * Checks if specified user is a process actor, defined by
     * process role code in a card process.
     *
     * @param card         card in a workflow process
     * @param user         user
     * @param procRoleCode process role code
     * @return true is user is a process actor
     */
    boolean isUserInProcRole(Card card, User user, String procRoleCode);

    /**
     * Do the same as <code>WfService.isUserInProcRole(Card card, User user, String procRoleCode)</code>
     * but takes a user from a current user session.
     *
     * @param card         card in a workflow process
     * @param procRoleCode process role code
     * @return true is user is a process actor
     */
    boolean isCurrentUserInProcRole(Card card, String procRoleCode);

    /**
     * Deletes all process notifications about the card for a user
     *
     * @param card card in a workflow process
     * @param user user
     */
    int deleteNotifications(Card card, User user);

    /**
     * Deletes process notification defined by a given cardInfo
     * for a specified user.
     *
     * @param cardInfo card info
     * @param user     user
     */
    void deleteNotification(CardInfo cardInfo, User user);

    /**
     * DEPRECATED
     * Do not use Service to calculate this information.
     * It can be calculated in the same app with information, stored in user session.
     * <p/>
     * Checks if current user has a specified security role
     *
     * @param role security role
     * @return true if user has a role
     */
    @Deprecated
    boolean isCurrentUserContainsRole(Role role);

    /**
     * Removes process notifications with a specified type
     * for a given user and card.
     *
     * @param card card in a workflow process
     * @param user user
     * @param type notification type
     */
    void deleteNotifications(Card card, User user, int type);

    /**
     * Fires given timer
     *
     * @param timer timer in workflow
     */
    void processTimer(TimerEntity timer);

    /**
     * Sets a HasAttachment attribute in a card.
     *
     * @param card           card
     * @param hasAttachments attribute value
     */
    void setHasAttachmentsInCard(Card card, Boolean hasAttachments);

    /**
     * Creates a process family with a specified card as a parent and
     * a child card in a family with a specified process.
     *
     * @param parentCard  parent card
     * @param subProcCode process code for child card
     * @return child card
     */
    Card createSubProcCard(Card parentCard, String subProcCode);

    /**
     * Removes a card and its {@link com.haulmont.workflow.core.entity.CardProc} links in a cascade.
     *
     * @param card card to be removed.
     */
    void removeSubProcCard(Card card);

    /**
     * Checks if card is in a workflow process.
     *
     * @param card card
     * @return is card in a process
     */
    boolean processStarted(Card card);

    /**
     * Finds users who participate in process on card with given procRole
     *
     * @param card card
     * @param procCode process code
     * @param cardRoleCode card role code
     * @return List of users
     */
    List<User> getProcessActors(Card card, String procCode, String cardRoleCode);
}
