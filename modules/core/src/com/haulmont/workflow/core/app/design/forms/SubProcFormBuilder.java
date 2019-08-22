/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app.design.forms;

import com.haulmont.workflow.core.app.design.FormBuilder;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.json.JSONObject;

public class SubProcFormBuilder extends FormBuilder {
    @Override
    public Element writeFormEl(Element parentEl, JSONObject jsProperties) throws DesignCompilationException {
        Element el = parentEl.addElement("screen");
        el.addAttribute("id", "subproc.form");
        el.addAttribute("before", "true");

        addFormParam(el, "cardRolesVisible", Boolean.toString(jsProperties.optBoolean("cardRolesVisible")));
        addFormParam(el, "commentVisible", Boolean.toString(jsProperties.optBoolean("commentVisible")));
        addFormParam(el, "subProcCode", StringUtils.defaultString(jsProperties.optString("subProcCode")));
        addFormParam(el, "requiredRoles", jsProperties.optString("requiredRoles"));
        addFormParam(el, "commentRequired", Boolean.toString(jsProperties.optBoolean("commentRequired")));
        addFormParam(el, "dueDateVisible", Boolean.toString(jsProperties.optBoolean("dueDateVisible")));

        return el;
    }
}
