/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.global.TimeUnit;

import javax.persistence.*;

@Entity(name = "wf$CardRole")
@Table(name = "WF_CARD_ROLE")
@NamePattern("%s|code")
@SystemLevel
public class CardRole extends StandardEntity {

    private static final long serialVersionUID = -2251386967542354599L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID")
    protected Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_ROLE_ID")
    protected ProcRole procRole;

    @Column(name = "CODE")
    protected String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    protected User user;

    @Column(name = "NOTIFY_BY_EMAIL")
    protected Boolean notifyByEmail = true;

    @Column(name = "NOTIFY_BY_CARD_INFO")
    protected Boolean notifyByCardInfo = true;

    @Column(name = "SORT_ORDER")
    protected Integer sortOrder;

    @Column(name = "DURATION")
    protected Integer duration;

    @Column(name = "TIME_UNIT")
    protected String timeUnit = "H";

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public ProcRole getProcRole() {
        return procRole;
    }

    public void setProcRole(ProcRole procRole) {
        this.procRole = procRole;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getNotifyByEmail() {
        return notifyByEmail;
    }

    public void setNotifyByEmail(Boolean notifyByEmail) {
        this.notifyByEmail = notifyByEmail;
    }

    public Boolean getNotifyByCardInfo() {
        return notifyByCardInfo;
    }

    public void setNotifyByCardInfo(Boolean notifyByCardInfo) {
        this.notifyByCardInfo = notifyByCardInfo;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public TimeUnit getTimeUnit() {
        return TimeUnit.fromId(timeUnit);
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit == null ? null : timeUnit.getId();
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}