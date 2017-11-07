/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.activity

import com.haulmont.chile.core.model.utils.InstanceUtils
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Transaction
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.workflow.core.entity.Card
import org.jbpm.api.activity.ActivityExecution

public class WriteCardPropertyActivity extends CardPropertyActivity {

    @Override
    void execute(ActivityExecution execution) throws Exception {
        super.execute(execution);
        writeCardProperty(card, execution)
    }

    void writeCardProperty(Card eventCard, ActivityExecution execution) {
        Object targetValue = objectLoader.getValue(value);
        if (value != null && targetValue == null) {
            throw new RuntimeException("Unsupported value '" + value + "' for property '" + propertyPath + "'");
        }
        if (targetValue instanceof Double &&
                BigDecimal.class.isAssignableFrom(eventCard.getMetaClass().getProperty(propertyPath).getJavaType())) {
            targetValue = new BigDecimal((Double) targetValue)
        }
        InstanceUtils.setValueEx(eventCard, propertyPath, targetValue);
        Persistence persistence = AppBeans.get(Persistence.NAME);
        Transaction tx = persistence.getTransaction();
        try {
            EntityManager em = persistence.getEntityManager()
            em.merge(card)
            tx.commit();
        } finally {
            tx.end();
        }
    }
}
