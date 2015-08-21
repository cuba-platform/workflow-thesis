/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package org.springframework.orm.hibernate3;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.AppBeans;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.context.CurrentSessionContext;
import org.hibernate.engine.SessionFactoryImplementor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.sql.Connection;

/**
 * The same as {@link SpringSessionContext} but creates a Hibernate session with connection
 * obtained from {@link EntityManager}
 */
public class CubaSpringSessionContext implements CurrentSessionContext {

    private Log log = LogFactory.getLog(CubaSpringSessionContext.class);

    private final SessionFactoryImplementor sessionFactory;

    public CubaSpringSessionContext(SessionFactoryImplementor sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Session currentSession() throws HibernateException {
        try {
            return (org.hibernate.classic.Session) doGetSession(this.sessionFactory);
        } catch (IllegalStateException ex) {
            throw new HibernateException(ex.getMessage());
        }
    }

    private org.hibernate.Session doGetSession(SessionFactory sessionFactory) throws HibernateException, IllegalStateException
    {
        Assert.notNull(sessionFactory, "No SessionFactory specified");

        SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
        if (sessionHolder != null && !sessionHolder.isEmpty()) {
            // pre-bound Hibernate Session
            org.hibernate.Session session = null;
            if (TransactionSynchronizationManager.isSynchronizationActive() &&
                    sessionHolder.doesNotHoldNonDefaultSession()) {
                // Spring transaction management is active ->
                // register pre-bound Session with it for transactional flushing.
                session = sessionHolder.getValidatedSession();
                if (session != null && !sessionHolder.isSynchronizedWithTransaction()) {
                    log.debug("Registering Spring transaction synchronization for existing Hibernate Session");
                    TransactionSynchronizationManager.registerSynchronization(
                            new CubaSpringSessionSynchronization(sessionHolder, sessionFactory, null, false));
                    sessionHolder.setSynchronizedWithTransaction(true);
                    // Switch to FlushMode.AUTO, as we have to assume a thread-bound Session
                    // with FlushMode.MANUAL, which needs to allow flushing within the transaction.
                    FlushMode flushMode = session.getFlushMode();
                    if (flushMode.lessThan(FlushMode.COMMIT) &&
                            !TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                        session.setFlushMode(FlushMode.AUTO);
                        sessionHolder.setPreviousFlushMode(flushMode);
                    }
                }
            }
            else {
                // No Spring transaction management active -> try JTA transaction synchronization.
//                session = getJtaSynchronizedSession(sessionHolder, sessionFactory, jdbcExceptionTranslator);
            }
            if (session != null) {
                return session;
            }
        }

        log.debug("Opening Hibernate Session");
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        Connection connection = em.getConnection();
        org.hibernate.Session session = sessionFactory.openSession(connection);

        // Use same Session for further Hibernate actions within the transaction.
        // Thread object will get removed by synchronization at transaction completion.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // We're within a Spring-managed transaction, possibly from JtaTransactionManager.
            log.debug("Registering Spring transaction synchronization for new Hibernate Session");
            SessionHolder holderToUse = sessionHolder;
            if (holderToUse == null) {
                holderToUse = new SessionHolder(session);
            }
            else {
                holderToUse.addSession(session);
            }
            if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                session.setFlushMode(FlushMode.MANUAL);
            }
            TransactionSynchronizationManager.registerSynchronization(
                    new CubaSpringSessionSynchronization(holderToUse, sessionFactory, null, true));
            holderToUse.setSynchronizedWithTransaction(true);
            if (holderToUse != sessionHolder) {
                TransactionSynchronizationManager.bindResource(sessionFactory, holderToUse);
            }
        }
        else {
            // No Spring transaction management active -> try JTA transaction synchronization.
//            registerJtaSynchronization(session, sessionFactory, jdbcExceptionTranslator, sessionHolder);
        }

        // Check whether we are allowed to return the Session.
        if (!SessionFactoryUtils.isSessionTransactional(session, sessionFactory)) {
            SessionFactoryUtils.closeSession(session);
            throw new IllegalStateException("No Hibernate Session bound to thread, " +
                "and configuration does not allow creation of non-transactional one here");
        }

        return session;
    }


}
