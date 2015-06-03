/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app.valuehandler;

import com.haulmont.chile.core.model.Instance;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.utils.InstanceUtils;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.app.PersistenceManagerAPI;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.workflow.core.entity.Card;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zaharchenko
 * @version $Id$
 */
public class EntityCardPropertyHandler extends BaseCardPropertyHandler {

    private PersistenceManagerAPI persistenceManagerAPI;

    public EntityCardPropertyHandler(Class clazz, Card card, Boolean useExpression) {
        super(clazz, card, useExpression);
        persistenceManagerAPI = AppBeans.get(PersistenceManagerAPI.NAME);
    }


    @Override
    public Map<String, Object> loadObjects() {
        Persistence persistence = AppBeans.get(Persistence.NAME);
        Transaction tx = persistence.getTransaction();
        try {
            Metadata metadata = AppBeans.get(Metadata.NAME);
            com.haulmont.chile.core.model.MetaClass metaClass = metadata.getSession().getClass(clazz);

            EntityManager em = persistence.getEntityManager();
            Query query = em.createQuery("select e from " + metaClass.getName() + " e");
            query.setView(getView(metaClass));
            List<BaseUuidEntity> entities = query.getResultList();
            query.setMaxResults(persistenceManagerAPI.getFetchUI(metaClass.getName()));

            Map<String, Object> map = new HashMap<>();
            for (BaseUuidEntity entity : entities) {
                String instanceName = InstanceUtils.getInstanceName((Instance) entity);
                if (StringUtils.isBlank(instanceName)) {
                    instanceName = entity.toString();
                }
                map.put(entity.getId().toString(), instanceName);
            }
            tx.commit();
            return map;
        } finally {
            tx.end();
        }
    }

    protected View getView(MetaClass metaClass) {
        Metadata metadata = AppBeans.get(Metadata.NAME);
        return metadata.getViewRepository().getView(metaClass, View.MINIMAL);
    }
}
