/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

/**
 *
 * <p>$Id$</p>
 *
 * @author Zaharchenko
 */
package com.haulmont.workflow.core.app.design;

import com.haulmont.workflow.core.error.DesignCompilationError;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class BaseDesignPostProcessor {
    public void processForms(Element rootElement, List<Module> modules, List<DesignCompilationError> errors) {
    }

    public void processMessages(Properties properties, Locale locale) {
    }

    public void processJpdl(Element rootElement, List<DesignCompilationError> compileErrors) {
    }

    public void processStates(List<DesignCompiler.DesignProcState> states, Document document, Properties properties) {
    }
}
