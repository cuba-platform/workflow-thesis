/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.sys;

import com.haulmont.workflow.core.WfTestCase;

/**
 * @author krivopustov
 * @version $Id$
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
