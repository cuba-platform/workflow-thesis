/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app.design.forms;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.workflow.core.app.design.FormBuilder;
import com.haulmont.workflow.core.entity.DesignScript;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Element;
import org.json.JSONObject;

public class InvokeFormBuilder extends FormBuilder {

    @Override
    public Element writeFormEl(Element parentEl, JSONObject jsProperties) throws DesignCompilationException {
        Element el = parentEl.addElement("invoke");
        String scriptName = jsProperties.optString("script");
        Element scriptEl = el.addElement("script");
        String content =  getContentOfScript(scriptName);
        scriptEl.addCDATA(content);
        return el;
    }

    protected String getContentOfScript(String script) throws DesignCompilationException {
        for (DesignScript designScript : design.getScripts()) {
            if (designScript.getName().equals(script)) {
                return  designScript.getContent();
            }
        }
        throw new DesignCompilationException(
                AppBeans.get(Messages.class).formatMessage(getClass(),
                        "exception.invokeScriptNotFound", StringEscapeUtils.escapeHtml(script)));
    }
}
