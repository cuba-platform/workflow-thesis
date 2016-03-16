/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.sys;

import com.haulmont.workflow.core.WfTestCase;

/**
 */
public class CubaJbpmDbidGeneratorTest extends WfTestCase {

    CubaJbpmDbidGenerator generator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generator = new CubaJbpmDbidGenerator();
        generator.blocksize = 10;
    }

    public void testGetNextId() throws Exception {
        for (int i = 0; i < 25; i++) {
            long nextId = generator.getNextId();
            System.out.println(nextId);
        }
    }
}
