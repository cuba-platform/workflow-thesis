/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app.design;

import com.haulmont.workflow.core.error.DesignCompilationError;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * <p>$Id$</p>
 *
 * @author devyatkin
 */
public class DesignPostProcessor {

    private List<BaseDesignPostProcessor> processors;

    public List<BaseDesignPostProcessor> getProcessors() {
        return processors;
    }

    public void setProcessors(List<BaseDesignPostProcessor> processors) {
        this.processors = processors;
    }

    public void processForms(Element rootElement, List<Module> modules, List<DesignCompilationError> errors) {
        if (processors == null) {
            return;
        }
        for (BaseDesignPostProcessor processor : processors) {
            processor.processForms(rootElement, modules, errors);
        }
    }

    public void processMessages(Properties properties, Locale locale) {
        if (processors == null) {
            return;
        }
        for (BaseDesignPostProcessor processor : processors) {
            processor.processMessages(properties, locale);
        }
    }

    public void processJpdl(Element rootElement, List<DesignCompilationError> compileErrors) {
        if (processors == null) {
            return;
        }
        for (BaseDesignPostProcessor processor : processors) {
            processor.processJpdl(rootElement, compileErrors);
        }
    }

    public void processStates(Map<String, String> states, Document document, Properties properties) {
        if (processors == null) {
            return;
        }
        for (BaseDesignPostProcessor processor : processors) {
            processor.processStates(states, document, properties);
        }
    }

}
