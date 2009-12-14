/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 14.12.2009 17:15:01
 *
 * $Id$
 */
import com.haulmont.cuba.core.PersistenceProvider
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Query

EntityManager em = PersistenceProvider.getEntityManager()
Query q = em.createQuery("select count(c.id) from wf\$Card c where c.state = ?1")
q.setParameter(1, "New")

return q.getSingleResult()
