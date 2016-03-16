/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.workflow.core.entity.AbstractProcessVariable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 *
 *
 */
@Service(ProcessVariableService.NAME)
public class ProcessVariableServiceBean implements ProcessVariableService {

    @Inject
    private ProcessVariableAPI processVariableAPI;

    @Override
    public String getStringValue(Object value) {
        Transaction tx = AppBeans.get(Persistence.class).getTransaction();
        try {
            String s = processVariableAPI.getStringValue(value);
            tx.commit();
            return s;
        } finally {
            tx.end();
        }
    }

    @Override
    public Object getValue(AbstractProcessVariable designProcessVariable) {
        Transaction tx = AppBeans.get(Persistence.class).getTransaction();
        try {
            Object result = processVariableAPI.getValue(designProcessVariable);
            tx.commit();
            return result;
        } finally {
            tx.end();
        }

    }

    @Override
    public String getLocalizedValue(AbstractProcessVariable designProcessVariable) {
        Transaction tx = AppBeans.get(Persistence.class).getTransaction();
        try {
            String result = processVariableAPI.getLocalizedValue(designProcessVariable);
            tx.commit();
            return result;
        } finally {
            tx.end();
        }
    }
}
