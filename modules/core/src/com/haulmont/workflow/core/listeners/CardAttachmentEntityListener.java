/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.listeners;

import com.haulmont.bali.db.QueryRunner;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.listener.AfterDeleteEntityListener;
import com.haulmont.cuba.core.listener.AfterInsertEntityListener;
import com.haulmont.cuba.core.sys.persistence.DbTypeConverter;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardAttachment;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.sql.SQLException;

/**
 * @author chernov
 * @version $Id$
 */
@ManagedBean("workflow_CardAttachmentEntityListener")
public class CardAttachmentEntityListener implements
        AfterInsertEntityListener<CardAttachment>, AfterDeleteEntityListener<CardAttachment> {

    protected Log log = LogFactory.getLog(CardAttachmentEntityListener.class);

    @Inject
    protected Persistence persistence;

    protected static final String CARD_UPDATE_QUERY = "update WF_CARD set HAS_ATTACHMENTS = ? where ID = ?";

    @Override
    public void onAfterInsert(CardAttachment entity) {
        executeUpdate(entity.getCard(), Boolean.TRUE);
    }

    @Override
    public void onAfterDelete(CardAttachment entity) {
        Card card = entity.getCard();

        if (card.getAttachments().isEmpty()) {
            executeUpdate(card, Boolean.FALSE);
        }
    }

    /**
     * Use QueryRunner here to avoid OptimisticLocking exception
     */
    protected void executeUpdate(Card card, Boolean hasAttachments) {
        try {
            DbTypeConverter converter = persistence.getDbTypeConverter();
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