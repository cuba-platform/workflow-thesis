/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.listeners;

import com.haulmont.bali.db.QueryRunner;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.listener.AfterInsertEntityListener;
import com.haulmont.cuba.core.listener.BeforeDeleteEntityListener;
import com.haulmont.cuba.core.sys.persistence.DbTypeConverter;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardAttachment;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;

public class CardAttachmentEntityListener implements AfterInsertEntityListener<CardAttachment>, BeforeDeleteEntityListener<CardAttachment> {

    protected Log log = LogFactory.getLog(CardAttachmentEntityListener.class);

    protected Persistence persistence = AppBeans.get(Persistence.NAME);

    protected DbTypeConverter converter = persistence.getDbTypeConverter();

    protected static final String CARD_UPDATE_QUERY = "update WF_CARD set HAS_ATTACHMENTS = ? where ID = ?";

    @Override
    public void onAfterInsert(CardAttachment entity) {
        executeUpdate(entity.getCard(), Boolean.TRUE);
    }

    @Override
    public void onBeforeDelete(CardAttachment entity) {
        Card card = entity.getCard();
        card.getAttachments().remove(entity);
        if (card.getAttachments().isEmpty()) {
            executeUpdate(card, Boolean.FALSE);
        }
    }

    /**
     * Use QueryRunner here to avoid OptimisticLocking exception
     */
    protected void executeUpdate(Card card, Boolean hasAttachments) {
        try {
            Object hasAttachmentsParam = converter.getSqlObject(hasAttachments);
            Object idParam = converter.getSqlObject(card.getId());

            QueryRunner runner = new QueryRunner();
            runner.update(persistence.getEntityManager().getConnection(),
                    CARD_UPDATE_QUERY, new Object[]{hasAttachmentsParam, idParam});
        } catch (SQLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }
}