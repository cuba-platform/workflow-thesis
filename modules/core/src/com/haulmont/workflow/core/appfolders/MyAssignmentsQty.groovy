/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */


import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Query
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.UserSessionSource

EntityManager em = AppBeans.get(Persistence.class).getEntityManager()
Query q = em.createQuery("select count(a.id) from wf\$Assignment a where a.user.id = ?1 and a.finished is null")
q.setParameter(1, AppBeans.get(UserSessionSource.class).currentOrSubstitutedUserId())

return q.getSingleResult()

