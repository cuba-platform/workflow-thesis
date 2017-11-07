/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app.design.modules;

import com.haulmont.workflow.core.app.design.Module;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.dom4j.Element;

public class WriteCardPropertyModule extends CardPropertyModule {

    public WriteCardPropertyModule() {
        activityClassName = "com.haulmont.workflow.core.activity.WriteCardPropertyActivity";
        setTransitionNames("out");
    }

    @Override
    public void init(Module.Context context) throws DesignCompilationException {
        super.init(context);
    }

    @Override
    public Element writeJpdlXml(Element parentEl) throws DesignCompilationException {
        Element element = super.writeJpdlXml(parentEl);
        return element;
    }
}
