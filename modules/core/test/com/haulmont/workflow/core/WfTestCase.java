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
import com.haulmont.cuba.core.Locator;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.testsupport.TestContext;
import com.haulmont.cuba.testsupport.TestDataSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class WfTestCase extends CubaTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println(Locator.isInTransaction());
    }

    @Override
    protected void initAppContext() {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(new String[]
                {"cuba-spring.xml", "workflow-spring.xml", "test-spring.xml"});
        AppContext.setApplicationContext(appContext);
    }
}
