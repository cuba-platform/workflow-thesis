/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.timer;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.security.entity.User;

/**
 * @author krivopustov
 * @version $Id$
 */
public abstract class AssignmentTimerAction implements TimerAction {

    @Override
    public void execute(TimerActionContext context) {
        EntityLoadInfo entityLoadInfo = EntityLoadInfo.parse(context.getParams().get("user"));
        if (entityLoadInfo == null)
          throw new IllegalStateException("No user load info in the parameters map");

        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        User user = (User) em.find(entityLoadInfo.getMetaClass().getJavaClass(), entityLoadInfo.getId());

        execute(context, user);
    }

    protected abstract void execute(TimerActionContext context, User user);
}