/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app.design.forms;

import com.haulmont.workflow.core.app.design.FormBuilder;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.json.JSONObject;

/**
 * @author subbotin
 * @version $Id$
 */
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
