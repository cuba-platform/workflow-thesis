/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
import com.haulmont.cuba.core.PersistenceProvider
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Query
import com.haulmont.cuba.core.global.UserSessionProvider

EntityManager em = PersistenceProvider.getEntityManager()
Query q = em.createQuery("select count(a.id) from wf\$Assignment a where a.user.id = ?1 and a.finished is null")
q.setParameter(1, UserSessionProvider.currentOrSubstitutedUserId())

return q.getSingleResult()

