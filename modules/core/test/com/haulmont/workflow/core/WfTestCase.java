/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core;

import com.haulmont.cuba.core.CubaTestCase;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public abstract class WfTestCase extends CubaTestCase {

    @Override
    protected List<String> getTestAppProperties() {
        String[] files = {
                "classpath:cuba-app.properties",
                "classpath:test-app.properties",
                "classpath:wf-test-app.properties"
        };
        return Arrays.asList(files);
    }
}
