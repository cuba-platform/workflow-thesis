/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 23.04.2010 14:39:47
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.core.global.DeletePolicy;

import javax.persistence.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity(name = "wf$CardProc")
@Table(name = "WF_CARD_PROC")
@SystemLevel
public class CardProc extends StandardEntity {

    private static final long serialVersionUID = -90039251878415667L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID")
    protected Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_ID")
    @OnDeleteInverse(DeletePolicy.DENY)
    protected Proc proc;

    @Column(name = "IS_ACTIVE")
    protected Boolean active;

    @Column(name = "START_COUNT")
    protected Integer startCount = 0;

    @Column(name = "STATE", length = 255)
    protected String state;

    @Column(name = "SORT_ORDER")
    protected Integer sortOrder;

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Proc getProc() {
        return proc;
    }

    public void setProc(Proc proc) {
        this.proc = proc;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getStartCount() {
        return startCount;
    }

    public void setStartCount(Integer startCount) {
        this.startCount = startCount;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    @MetaProperty
    public String getLocState() {
        if (getState() == null)
            return "";
        if (getProc() != null) {
            String messagesPack = getProc().getMessagesPack();
            StringBuilder sb = new StringBuilder();
            Matcher matcher = Pattern.compile("[^ ,]+").matcher(getState());
            while (matcher.find()) {
                sb.append(AppBeans.get(MessageTools.class).loadString(messagesPack, "msg://" + matcher.group()))
                        .append(Card.STATE_SEPARATOR);
            }
            if (sb.length() > 0)
                sb.delete(sb.length() - Card.STATE_SEPARATOR.length(), sb.length());
            return sb.toString();
        }
        return getState();
    }
}
