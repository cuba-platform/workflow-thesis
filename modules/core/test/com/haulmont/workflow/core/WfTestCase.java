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
