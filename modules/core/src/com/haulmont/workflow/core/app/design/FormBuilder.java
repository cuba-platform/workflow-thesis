/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.app.design;

import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.dom4j.Element;
import org.json.JSONObject;

public abstract class FormBuilder {

    protected Design design;

    public void init(Design design) {
        this.design = design;
    }

    public abstract Element writeFormEl(Element parentEl, JSONObject jsProperties) throws DesignCompilationException;

    protected void addFormParam(Element el, String name, String value) {
        if (value == null)
            return;

        Element paramEl = el.addElement("param");
        paramEl.addAttribute("name", name);
        paramEl.addAttribute("value", value);
    }
}
