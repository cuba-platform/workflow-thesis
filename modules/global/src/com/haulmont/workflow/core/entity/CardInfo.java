/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.SoftDelete;
import com.haulmont.cuba.core.entity.Updatable;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "wf$CardInfo")
@Table(name = "WF_CARD_INFO")
@SystemLevel
public class CardInfo extends BaseUuidEntity implements Updatable, SoftDelete {

    private static final long serialVersionUID = -49071058042769381L;

    public static final int TYPE_SIMPLE = 0;
    public static final int TYPE_NOTIFICATION = 5;
    public static final int TYPE_OVERDUE = 10;
    public static final int TYPE_NOTIFY_OVERDUE = 20;

    @MetaProperty
    public Date getUpdateTs() {
        return null;
    }

    public void setUpdateTs(Date updateTs) {
    }

    @MetaProperty
    public String getUpdatedBy() {
        return null;
    }

    public void setUpdatedBy(String updatedBy) {
    }

    @Column(name = "DELETE_TS")
    protected Date deleteTs;

    @Column(name = "DELETED_BY", length = LOGIN_FIELD_LEN)
    protected String deletedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID")
    protected Card card;

    @Column(name = "CARD_INFO_TYPE")
    protected Integer type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    protected User user;

    @Column(name = "JBPM_EXECUTION_ID", length = 255)
    protected String jbpmExecutionId;

    @Column(name = "ACTIVITY", length = 255)
    protected String activity;

    @Lob
    @Column(name = "DESCRIPTION", length = 100000)
    protected String description;

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
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

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getJbpmExecutionId() {
        return jbpmExecutionId;
    }

    public void setJbpmExecutionId(String jbpmExecutionId) {
        this.jbpmExecutionId = jbpmExecutionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
