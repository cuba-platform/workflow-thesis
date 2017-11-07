/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app.design.postprocessors;

import com.haulmont.workflow.core.app.design.BaseDesignPostProcessor;
import com.haulmont.workflow.core.error.DesignCompilationError;
import org.dom4j.Element;

import java.util.List;

public class DisableCachePostProcessor extends BaseDesignPostProcessor {

    @Override
    public void processJpdl(Element rootElement, List<DesignCompilationError> compileErrors) {
        List<Element> activities = (List<Element>) rootElement.elements("custom");
        if (!activities.isEmpty()) {
            for (Element element : activities) {
                element.addAttribute("cache", "disabled");
            }
        }
    }
}
