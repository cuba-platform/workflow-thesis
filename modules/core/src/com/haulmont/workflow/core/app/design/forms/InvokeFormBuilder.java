package com.haulmont.workflow.core.app.design.forms;

import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.workflow.core.app.design.FormBuilder;
import com.haulmont.workflow.core.entity.DesignScript;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.dom4j.Element;
import org.json.JSONObject;

/**
 * @author tsarevskiy
 * @version $Id$
 */
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
                MessageProvider.formatMessage(getClass(), "exception.invokeScriptNotFound", script));
    }
}
