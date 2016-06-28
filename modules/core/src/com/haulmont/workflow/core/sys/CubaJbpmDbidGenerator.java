/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.sys;

import com.haulmont.bali.db.DbUtils;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.sys.persistence.DbmsSpecificFactory;
import com.haulmont.cuba.core.sys.persistence.SequenceSupport;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.pvm.internal.id.DbidGenerator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
            else if (value instanceof BigDecimal)
                nextId = ((BigDecimal) value).longValue();
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
        return DbmsSpecificFactory.getSequenceSupport();
    }

    private Object executeScript(String sqlScript) {
        EntityManager em = persistence.getEntityManager();
        StrTokenizer tokenizer = new StrTokenizer(sqlScript, SequenceSupport.SQL_DELIMITER);
        Object value = null;
        Connection connection = em.getConnection();
        while (tokenizer.hasNext()) {
            String sql = tokenizer.nextToken();
            try {
                PreparedStatement statement = connection.prepareStatement(sql);
                try {
                    if (statement.execute()) {
                        ResultSet rs = statement.getResultSet();
                        if (rs.next())
                            value = rs.getLong(1);
                    }
                } finally {
                    DbUtils.closeQuietly(statement);
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Error executing SQL for getting next number", e);
            }
        }

        return value;
    }
}
