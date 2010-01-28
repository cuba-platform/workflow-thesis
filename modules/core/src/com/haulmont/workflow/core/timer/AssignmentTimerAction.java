/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 26.01.2010 14:33:44
 *
 * $Id$
 */
package com.haulmont.workflow.core.timer;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.PersistenceProvider;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.security.entity.User;

public abstract class AssignmentTimerAction implements TimerAction {

    public void execute(TimerActionContext context) {
        EntityLoadInfo entityLoadInfo = EntityLoadInfo.parse(context.getParams().get("user"));
        if (entityLoadInfo == null)
          throw new IllegalStateException("No user load info in the parameters map");

        EntityManager em = PersistenceProvider.getEntityManager();
        User user = (User) em.find(entityLoadInfo.getMetaClass().getJavaClass(), entityLoadInfo.getId());

        execute(context, user);
    }

    protected abstract void execute(TimerActionContext context, User user);
}
