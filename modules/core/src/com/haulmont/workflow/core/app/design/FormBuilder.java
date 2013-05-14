/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 17.01.11 15:32
 *
 * $Id$
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
