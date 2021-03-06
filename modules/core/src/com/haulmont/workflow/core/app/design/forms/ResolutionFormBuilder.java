/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app.design.forms;

import com.haulmont.workflow.core.app.design.FormBuilder;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.dom4j.Element;
import org.json.JSONObject;

public class ResolutionFormBuilder extends FormBuilder {

    @Override
    public Element writeFormEl(Element parentEl, JSONObject jsProperties) throws DesignCompilationException {
        Element el = parentEl.addElement("screen");
        el.addAttribute("id", "resolution.form");
        el.addAttribute("before", "true");

        addFormParam(el, "attachmentsVisible", Boolean.toString(jsProperties.optBoolean("attachmentsVisible")));
        addFormParam(el, "commentRequired", Boolean.toString(jsProperties.optBoolean("commentRequired")));

        return el;
    }
}
