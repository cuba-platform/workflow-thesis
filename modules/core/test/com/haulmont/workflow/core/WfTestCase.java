/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 11.11.2009 18:43:05
 *
 * $Id$
 */
package com.haulmont.workflow.core;

import com.haulmont.cuba.core.CubaTestCase;
import com.haulmont.cuba.core.PersistenceProvider;
import com.haulmont.cuba.core.Locator;

public abstract class WfTestCase extends CubaTestCase {

    static {
        System.setProperty(PersistenceProvider.PERSISTENCE_XML, "META-INF/workflow-persistence.xml");
        System.setProperty(PersistenceProvider.PERSISTENCE_UNIT, "workflow");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println(Locator.isInTransaction());
    }
}
