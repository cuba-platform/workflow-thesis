/*
* Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
* Haulmont Technology proprietary and confidential.
* Use is subject to license terms.

* Author: Gennady Pavlov
* Created: 29.04.2010 12:38:32
*
* $Id$
*/
package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.SoftDelete;
import com.haulmont.cuba.core.entity.Updatable;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "wf$CardRelation")
@Table(name = "WF_CARD_RELATION")
public class CardRelation extends BaseUuidEntity implements SoftDelete, Updatable {

    @Column(name = "UPDATE_TS")
    protected Date updateTs;

    @Column(name = "UPDATED_BY", length = LOGIN_FIELD_LEN)
    protected String updatedBy;

    @Column(name = "DELETE_TS")
    protected Date deleteTs;

    @Column(name = "DELETED_BY", length = LOGIN_FIELD_LEN)
    protected String deletedBy;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "CARD_ID")
    private Card card;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "RELATED_CARD_ID")
    private Card relatedCard;

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

    public Boolean isDeleted() {
        return deleteTs != null;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Card getRelatedCard() {
        return relatedCard;
    }

    public void setRelatedCard(Card relatedCard) {
        this.relatedCard = relatedCard;
    }
}
