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

import java.io.InputStream;
import java.util.List;

public abstract class WfTestCase extends CubaTestCase {

    @Override
    protected List<String> getTestAppContextFiles() {
        List<String> files = super.getTestAppContextFiles();
        files.add("workflow-spring.xml");
        return files;
    }

    @Override
    protected List<String> getPersistenceSourceFiles() {
        List<String> list = super.getPersistenceSourceFiles();
        list.add("workflow-persistence.xml");
        return list;
    }

    @Override
    protected InputStream getTestAppProperties() {
        return WfTestCase.class.getResourceAsStream("/wf-test-app.properties");
    }
}
