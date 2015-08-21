/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Query
import com.haulmont.cuba.core.global.AppBeans

EntityManager em = AppBeans.get(Persistence.class).getEntityManager()
Query q = em.createQuery("select count(c.id) from wf\$Card c where c.state = ?1")
q.setParameter(1, "New")

return q.getSingleResult()
