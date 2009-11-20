/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 20.11.2009 11:50:10
 *
 * $Id$
 */
package com.haulmont.docflow.core.app;

import com.haulmont.cuba.core.PersistenceProvider;

public class DocflowDeployer implements DocflowDeployerMBean {

    static {
        System.setProperty(PersistenceProvider.PERSISTENCE_UNIT, "docflow");
        System.setProperty(PersistenceProvider.PERSISTENCE_XML, "META-INF/docflow-persistence.xml");
    }

}
