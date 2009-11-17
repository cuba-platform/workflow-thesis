/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 16.11.2009 15:11:16
 *
 * $Id$
 */
package com.haulmont.docflow;

import com.haulmont.workflow.core.WfTestCase;
import com.haulmont.cuba.core.PersistenceProvider;

public class DfTestCase extends WfTestCase {

    static {
        System.setProperty(PersistenceProvider.PERSISTENCE_XML, "META-INF/docflow-persistence.xml");
        System.setProperty(PersistenceProvider.PERSISTENCE_UNIT, "docflow");
    }
}
