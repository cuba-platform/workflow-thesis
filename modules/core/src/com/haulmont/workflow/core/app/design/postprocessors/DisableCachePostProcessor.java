/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app.design.postprocessors;

import com.haulmont.workflow.core.app.design.BaseDesignPostProcessor;
import com.haulmont.workflow.core.error.DesignCompilationError;
import org.dom4j.Element;

import java.util.List;

/**
 * @author zaharchenko
 * @version $Id$
 */
public class DisableCachePostProcessor extends BaseDesignPostProcessor {

    @Override
    public void processJpdl(Element rootElement, List<DesignCompilationError> compileErrors) {
        List<Element> activities = (List<Element>) rootElement.elements();
        if (!activities.isEmpty()) {
            for (Element element : activities) {
                element.addAttribute("cache", "disabled");
            }
        }
    }
}
