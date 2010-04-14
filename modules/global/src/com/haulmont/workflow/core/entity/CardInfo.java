/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 09.04.2010 17:14:54
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;

@Entity(name = "wf$CardInfo")
@Table(name = "WF_CARD_INFO")
public class CardInfo extends BaseUuidEntity {

    private static final long serialVersionUID = -49071058042769381L;

    public enum Type implements EnumClass<Integer> {
        OVERDUE(10),
        NOTIFY_OVERDUE(20);

        private final Integer id;

        private Type(Integer id) {
            this.id = id;
        }

        public Integer getId() {
            return id;
        }

        public static Type fromId(Integer id) {
            for (Type type : Type.values()) {
                if (type.getId().equals(id))
                    return type;
            }
            return null;
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID")
    private Card card;

    @Column(name = "TYPE")
    private Integer type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "JBPM_EXECUTION_ID", length = 255)
    private String jbpmExecutionId;

    @Column(name = "ACTIVITY", length = 255)
    private String activity;

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Type getType() {
        return Type.fromId(type);
    }

    public void setType(Type type) {
        this.type = type.getId();
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
}
