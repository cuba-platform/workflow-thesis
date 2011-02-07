/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 28.12.10 18:39
 *
 * $Id$
 */
package com.haulmont.workflow.core.app.design.modules;

import com.haulmont.workflow.core.app.design.Module;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StartModule extends Module {

    @Override
    public Element writeJpdlMainEl(Element parentEl) {
        Element el = parentEl.addElement("start");
        el.addAttribute("name", name);
        return el;
    }

    @Override
    public void writeFormsXml(Element rootEl) throws DesignCompilationException {
        try {
            JSONObject jsOptions = jsValue.optJSONObject("options");
            if (jsOptions == null)
                return;

            JSONObject jsForms = jsOptions.optJSONObject("forms");
            if (jsForms == null)
                return;

            JSONArray jsFormsList = jsForms.optJSONArray("list");
            if (jsFormsList == null || jsFormsList.length() == 0)
                return;

            Element el = rootEl.element("start");
            if (el == null)
                el = rootEl.addElement("start");

            for (int i = 0; i < jsFormsList.length(); i++) {
                JSONObject jsForm = jsFormsList.getJSONObject(i);
                String formName = jsForm.getString("name");
                JSONObject jsProperties = jsForm.getJSONObject("properties");
                context.getFormCompiler().writeFormEl(el, formName, jsProperties);
            }
        } catch (JSONException e) {
            throw new DesignCompilationException("Unable to compile forms for module " + name, e);
        }
    }
}
