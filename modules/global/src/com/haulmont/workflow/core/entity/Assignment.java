/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 16.11.2009 12:12:45
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.Aggregation;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.MessageUtils;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.chile.core.annotations.MetaProperty;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity(name = "wf$Assignment")
@Table(name = "WF_ASSIGNMENT")
public class Assignment extends StandardEntity {

    private static final long serialVersionUID = 2889343799342063691L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID")
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MASTER_ASSIGNMENT_ID")
    private Assignment masterAssignment;

    @Column(name = "NAME", length = 255)
    private String name;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "JBPM_PROCESS_ID", length = 255)
    private String jbpmProcessId;

    @Column(name = "FINISHED")
    private Date finished;

    @Column(name = "OUTCOME", length = 255)
    private String outcome;

    @Column(name = "COMMENT", length = 2000)
    private String comment;

    @OneToMany(mappedBy = "assignment")
    @OrderBy("name")
    @Aggregation
    private List<AssignmentAttachment> attachments;

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

    public Assignment getMasterAssignment() {
        return masterAssignment;
    }

    public void setMasterAssignment(Assignment masterAssignment) {
        this.masterAssignment = masterAssignment;
    }

    public String getJbpmProcessId() {
        return jbpmProcessId;
    }

    public void setJbpmProcessId(String jbpmProcessId) {
        this.jbpmProcessId = jbpmProcessId;
    }

    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
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

    public List<AssignmentAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AssignmentAttachment> attachments) {
        this.attachments = attachments;
    }

    @MetaProperty
    public Boolean getHasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    @MetaProperty
    public String getLocName() {
        return getLocalizedAttribute(name);
    }

    @MetaProperty
    public String getLocDescription() {
        return getLocalizedAttribute(description);
    }

    @MetaProperty
    public String getLocOutcome() {
        return outcome == null ? null : getLocalizedAttribute(name + "." + outcome);
    }

    @MetaProperty
    public String getLocOutcomeResult() {
        return outcome == null ? null : getLocalizedAttribute(name + "." + outcome + "Result");
    }

    private String getLocalizedAttribute(String value) {
        if (value == null)
            return "";

        if (getCard() != null && getCard().getProc() != null) {
            String messagesPack = getCard().getProc().getMessagesPack();
            if (!value.startsWith(MessageUtils.MARK))
                value = MessageUtils.MARK + value;
            return MessageUtils.loadString(messagesPack, value);
        }
        return value;
    }
}
