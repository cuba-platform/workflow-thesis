/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.activity

import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Transaction
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.Messages
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.core.global.View
import com.haulmont.workflow.core.app.CardPropertyHandlerLoader
import com.haulmont.workflow.core.app.WfEntityDescriptorTools
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.global.CardPropertyUtils
import com.haulmont.workflow.core.app.valuehandler.CardPropertyHandler
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.jbpm.api.activity.ActivityExecution

/**
 *
 * @author zaharchenko
 * @version $Id$
 */
abstract class CardPropertyActivity extends ProcessVariableActivity {

    Messages messages

    String propertyPath
    String cardClass
    String value
    Boolean useExpression

    CardPropertyHandler objectLoader

    Class propertyClass

    WfEntityDescriptorTools dynamicEntityProviderAPI

    Card card

    private static Log log = LogFactory.getLog(CardPropertyActivity.class)

    public void execute(ActivityExecution execution) throws Exception {
        super.execute(execution)
        messages = AppBeans.get(Messages.NAME, Messages.class);
        dynamicEntityProviderAPI = AppBeans.get(WfEntityDescriptorTools.NAME);
        CardPropertyHandlerLoader designManagerAPI = AppBeans.get(CardPropertyHandlerLoader.NAME);
        MetaClass metaClass = AppBeans.get(Metadata.NAME, Metadata.class).getSession().getClass(Class.forName(cardClass));
        propertyClass = CardPropertyUtils.getClassByMetaProperty(metaClass, propertyPath);
        if (propertyClass == null) {
            throw new RuntimeException("Path '" + propertyPath + "' not found in class '" + metaClass.getName() + "'");
        }
        card = ActivityHelper.findCard(execution);
        Persistence persistence = AppBeans.get(Persistence.NAME);
        Transaction tx = persistence.getTransaction();
        try {
            EntityManager em = persistence.getEntityManager()
            card = em.find(Card.class, card.getId(), getRequiredView());
            tx.commit();
        } finally {
            tx.end();
        }
        objectLoader = designManagerAPI.loadHandler(propertyClass, card, useExpression);

    }

    View getRequiredView() {
        View view = new View(Card.class);
        CardPropertyUtils.generateViewByPropertyPath(view, propertyPath);
        return view;
    }
}
