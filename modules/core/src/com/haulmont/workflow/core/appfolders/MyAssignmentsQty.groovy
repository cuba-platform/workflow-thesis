/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 14.12.2009 17:27:35
 *
 * $Id$
 */
import com.haulmont.cuba.core.PersistenceProvider
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Query
import com.haulmont.cuba.core.SecurityProvider

EntityManager em = PersistenceProvider.getEntityManager()
Query q = em.createQuery("select count(a.id) from wf\$Assignment a where a.user.id = ?1 and a.finished is null")
q.setParameter(1, SecurityProvider.currentOrSubstitutedUserId())

return q.getSingleResult()

