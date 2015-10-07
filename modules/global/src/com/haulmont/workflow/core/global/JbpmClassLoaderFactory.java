/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.global;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Scripting;

/**
 * @author degtyarjov
 * @version $Id$
 */
public final class JbpmClassLoaderFactory {
    private JbpmClassLoaderFactory() {
    }

    public static ClassLoader getClassLoader(){
        return AppBeans.get(Scripting.NAME, Scripting.class).getClassLoader();
    }
}
