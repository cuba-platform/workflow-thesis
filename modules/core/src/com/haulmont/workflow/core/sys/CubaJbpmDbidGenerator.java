/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.sys;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DbDialect;
import com.haulmont.cuba.core.global.SequenceSupport;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.pvm.internal.id.DbidGenerator;

import java.util.List;

/**
 * @author krivopustov
 * @version $Id$
 */
public class CubaJbpmDbidGenerator extends DbidGenerator {

    public static final String SEQ_NAME = "seq_jbpm_id_gen";

    private Log log = LogFactory.getLog(getClass());

    long blocksize = 1000;

    // runtime state
    private long lastId = -2;
    private long nextId = -1;

    private volatile boolean sequenceExists;

    protected Persistence persistence = AppBeans.get(Persistence.class);

    @Override
    public synchronized long getNextId() {
        // if no more ids available
        if (lastId < nextId) {
            // acquire a next block of ids

            log.debug("Last ID " + lastId + " was consumed. Acquiring new block");

            // reset the id block
            lastId = -2;
            nextId = -1;

            try {
                acquireDbidBlock();
            } catch (Exception e) {
                throw new RuntimeException("Couldn't acquire block of ids", e);
            }
        }

        return nextId++;
    }

    protected void acquireDbidBlock() {
        SequenceSupport sequenceSupport = getSequenceSqlProvider();
        checkSequenceExists(sequenceSupport);
        String nextValueSql = sequenceSupport.getNextValueSql(SEQ_NAME);

        Transaction tx = persistence.getTransaction();
        try {
            Object value = executeScript(nextValueSql);
            tx.commit();
            if (value instanceof Long)
                nextId = (Long) value;
            else
                throw new IllegalStateException("Unsupported value type: " + value.getClass());
        } finally {
            tx.end();
        }

        lastId = nextId + blocksize - 1;

        log.debug("Acquired new ID block [" + nextId + "-" + lastId + "]");
    }

    private void checkSequenceExists(SequenceSupport sequenceSupport) {
        if (sequenceExists)
            return;
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Query query = em.createNativeQuery(sequenceSupport.sequenceExistsSql(SEQ_NAME));
            List list = query.getResultList();
            if (list.isEmpty()) {
                query = em.createNativeQuery(sequenceSupport.createSequenceSql(SEQ_NAME, 1, blocksize));
                query.executeUpdate();
            }
            sequenceExists = true;

            tx.commit();
        } finally {
            tx.end();
        }
    }

    private SequenceSupport getSequenceSqlProvider() {
        DbDialect dialect = persistence.getDbDialect();
        if (dialect instanceof SequenceSupport)
            return (SequenceSupport) dialect;
        else
            throw new UnsupportedOperationException("DB sequences not supported");
    }

    private Object executeScript(String sqlScript) {
        EntityManager em = persistence.getEntityManager();
        StrTokenizer tokenizer = new StrTokenizer(sqlScript, SequenceSupport.SQL_DELIMITER);
        Object value = null;
        while (tokenizer.hasNext()) {
            String sql = tokenizer.nextToken();
            Query query = em.createNativeQuery(sql);
            if (isSelectSql(sql))
                value = query.getSingleResult();
            else
                query.executeUpdate();
        }
        return value;
    }

    private boolean isSelectSql(String sql) {
        return sql.trim().toLowerCase().startsWith("select");
    }
}
