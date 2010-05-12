/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 13.11.2009 11:04:50
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.annotation.LocalizedValue;
import com.haulmont.cuba.core.global.MessageUtils;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.SoftDelete;
import com.haulmont.cuba.core.entity.Updatable;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.chile.core.annotations.Aggregation;
import com.haulmont.chile.core.annotations.MetaProperty;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity(name = "wf$Card")
@Table(name = "WF_CARD")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("0")
public class Card extends BaseUuidEntity implements Updatable, SoftDelete {

    private static final long serialVersionUID = -6180254942462308853L;

    @Column(name = "UPDATE_TS")
    protected Date updateTs;

    @Column(name = "UPDATED_BY", length = LOGIN_FIELD_LEN)
    protected String updatedBy;

    @Column(name = "DELETE_TS")
    protected Date deleteTs;

    @Column(name = "DELETED_BY", length = LOGIN_FIELD_LEN)
    protected String deletedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_ID")
    protected Proc proc;

    @Column(name = "JBPM_PROCESS_ID", length = 255)
    protected String jbpmProcessId;

    @Column(name = "STATE", length = 255)
    @LocalizedValue(messagePackExpr = "proc.messagesPack")
    protected String state;

    @Column(name = "DESCRIPTION", length = 1000)
    protected String description;
    
    @OneToMany(mappedBy = "card", fetch = FetchType.LAZY)
    @OrderBy("sortOrder")
    @Aggregation
    protected List<CardProc> procs;

    @OneToMany(mappedBy = "card", fetch = FetchType.LAZY)
    @OrderBy("code")
    @Aggregation
    protected List<CardRole> roles;

    @OneToMany(mappedBy = "card", fetch = FetchType.LAZY)
    @OrderBy("createTs")
    @Aggregation
    protected List<CardAttachment> attachments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATOR_ID")
    protected User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_CARD_ID")
    protected Card parentCard;

    @OneToMany(mappedBy = "parentCard")
    protected Set<Card> subCards;

    public Date getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(Date updateTs) {
        this.updateTs = updateTs;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Boolean isDeleted() {
        return deleteTs != null;
    }

    public Date getDeleteTs() {
        return deleteTs;
    }

    public void setDeleteTs(Date deleteTs) {
        this.deleteTs = deleteTs;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Proc getProc() {
        return proc;
    }

    public void setProc(Proc proc) {
        this.proc = proc;
    }

    public String getJbpmProcessId() {
        return jbpmProcessId;
    }

    public void setJbpmProcessId(String jbpmProcessId) {
        this.jbpmProcessId = jbpmProcessId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CardProc> getProcs() {
        return procs;
    }

    public void setProcs(List<CardProc> procs) {
        this.procs = procs;
    }

    public List<CardRole> getRoles() {
        return roles;
    }

    public void setRoles(List<CardRole> roles) {
        this.roles = roles;
    }

    public List<CardAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<CardAttachment> attachments) {
        this.attachments = attachments;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Card getParentCard() {
        return parentCard;
    }

    public void setParentCard(Card parentCard) {
        this.parentCard = parentCard;
    }

    public Set<Card> getSubCards() {
        return subCards;
    }

    public void setSubCards(Set<Card> subCards) {
        this.subCards = subCards;
    }

    @MetaProperty
    public String getLocState() {
        if (getState() == null)
            return "";
        if (getProc() != null) {
            String messagesPack = getProc().getMessagesPack();
            return MessageUtils.loadString(messagesPack, "msg://" + getState());
        }
        return getState();
    }
}
