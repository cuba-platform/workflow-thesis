/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.Composition;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserFormatTools;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.app.AssignmentLocalizationTools;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity(name = "wf$Assignment")
@Table(name = "WF_ASSIGNMENT")
@SystemLevel
public class Assignment extends StandardEntity {

    private static final long serialVersionUID = 2889343799342063691L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    protected User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID")
    protected Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUBPROC_CARD_ID")
    protected Card subProcCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_ID")
    protected Proc proc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MASTER_ASSIGNMENT_ID")
    protected Assignment masterAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FAMILY_ASSIGNMENT_ID")
    protected Assignment familyAssignment;

    @Column(name = "NAME", length = 255)
    protected String name;

    @Column(name = "DESCRIPTION", length = 1000)
    protected String description;

    @Column(name = "JBPM_PROCESS_ID", length = 255)
    protected String jbpmProcessId;

    @Column(name = "DUE_DATE")
    protected Date dueDate;

    @Column(name = "FINISHED")
    protected Date finished;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FINISHED_BY")
    protected User finishedByUser;

    @Column(name = "OUTCOME", length = 255)
    protected String outcome;

    @Column(name = "ASSIGNMENT_COMMENT", length = 100000)
    protected String comment;

    @OneToMany(mappedBy = "assignment")
    @OrderBy("name")
    @Composition
    protected List<CardAttachment> attachments;

    @Column(name = "ITERATION")
    protected Integer iteration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ROLE_ID")
    protected CardRole cardRole;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Card getSubProcCard() {
        return subProcCard;
    }

    public void setSubProcCard(Card subProcCard) {
        this.subProcCard = subProcCard;
    }

    public Proc getProc() {
        return proc;
    }

    public void setProc(Proc proc) {
        this.proc = proc;
    }

    public Assignment getMasterAssignment() {
        return masterAssignment;
    }

    public void setMasterAssignment(Assignment masterAssignment) {
        this.masterAssignment = masterAssignment;
    }

    public Assignment getFamilyAssignment() {
        return familyAssignment;
    }

    public void setFamilyAssignment(Assignment familyAssignment) {
        this.familyAssignment = familyAssignment;
    }

    public String getJbpmProcessId() {
        return jbpmProcessId;
    }

    public void setJbpmProcessId(String jbpmProcessId) {
        this.jbpmProcessId = jbpmProcessId;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }

    public User getFinishedByUser() {
        return finishedByUser;
    }

    public void setFinishedByUser(User finishedByUser) {
        this.finishedByUser = finishedByUser;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<CardAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<CardAttachment> attachments) {
        this.attachments = attachments;
    }

    public Integer getIteration() {
        return iteration;
    }

    public void setIteration(Integer iteration) {
        this.iteration = iteration;
    }

    @MetaProperty
    public String getDisplayUser() {
        if (user == null) {
            return "";
        }

        UserFormatTools formatTools = AppBeans.get(UserFormatTools.NAME);
        if (finishedByUser == null || ObjectUtils.equals(user, finishedByUser)) {
            return formatTools.formatOfficial(user);
        }
        return formatTools.formatSubstitution(finishedByUser, user);
    }

    private String userNameOrLogin(User user) {
        return StringUtils.isBlank(user.getName()) ? user.getLogin() : user.getName();
    }

    @MetaProperty
    public Boolean getHasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    @MetaProperty
    public String getLocName() {
        return getLocTools().getLocalizedAttribute(this, name);
    }

    @MetaProperty
    public String getLocDescription() {
        return getLocTools().getLocalizedAttribute(this, description);
    }

    @MetaProperty
    public String getLocOutcome() {
        return getLocTools().getLocOutcome(this);
    }

    @MetaProperty
    public String getLocOutcomeResult() {
        return getLocTools().getLocOutcomeResult(this);
    }

    protected AssignmentLocalizationTools getLocTools() {
        return AppBeans.get(AssignmentLocalizationTools.class);
    }

    public CardRole getCardRole() {
        return cardRole;
    }

    public void setCardRole(CardRole cardRole) {
        this.cardRole = cardRole;
    }
}
