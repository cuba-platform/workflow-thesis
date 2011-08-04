/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 28.12.10 15:21
 *
 * $Id$
 */
package com.haulmont.workflow.core.app.design;

import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignScript;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public abstract class Module {

    public static class Context {
        private Design design;
        private JSONObject json;
        private FormCompiler formCompiler;

        public Context(Design design, JSONObject json, FormCompiler formCompiler) {
            this.design = design;
            this.json = json;
            this.formCompiler = formCompiler;
        }

        public Design getDesign() {
            return design;
        }

        public JSONObject getJson() {
            return json;
        }

        public FormCompiler getFormCompiler() {
            return formCompiler;
        }
    }

    protected static class Transition {
        public String srcTerminal;
        public String dstName;
        public String dstTerminal;
    }

    protected Context context;

    protected JSONObject jsValue;

    protected String name;

    protected String caption;

    protected String activityClassName;

    protected List<Transition> transitions = new ArrayList<Transition>();

    protected Map<String, String> scriptNamesMap = new HashMap<String, String>();

    public void init(Context context) throws DesignCompilationException {
        try {
            this.context = context;
            this.jsValue = context.getJson().getJSONObject("value");

            initScriptNamesMap();
            String name = jsValue.optString("name");
            if (!StringUtils.isBlank(name)) {
                this.name = WfUtils.encodeKey(name);
                this.caption = name;
            } else {
                throw new DesignCompilationException(
                        MessageProvider.getMessage(getClass(), "exception.emptyName"));
            }
        } catch (JSONException e) {
            throw new DesignCompilationException(e);
        }
    }

    public String getName() {
        return name;
    }

    public String getCaption() {
        return caption;
    }

    protected void initScriptNamesMap() {
        if (context.getDesign().getScripts() != null) {
            for (DesignScript designScript : context.getDesign().getScripts()) {
                scriptNamesMap.put(designScript.getName(), designScript.getFileName());
            }
        }
    }

    public void addTransition(String srcTerminal, Module dstModule, String dstTerminal) {
        Transition t = new Transition();
        t.srcTerminal = srcTerminal;
        t.dstName = dstModule.getName();
        t.dstTerminal = dstTerminal;
        transitions.add(t);
    }

    public Element writeJpdlXml(Element parentEl) throws DesignCompilationException {
        Element el = writeJpdlMainEl(parentEl);
        writeJpdlTransitions(el);
        return el;
    }

    protected Element writeJpdlMainEl(Element parentEl) {
        Element el = parentEl.addElement("custom");
        if (activityClassName == null)
            throw new IllegalStateException("activityClassName is null");
        el.addAttribute("class", activityClassName);

        el.addAttribute("name", name);
        return el;
    }

    protected void writeJpdlTransitions(Element parentEl) {
        for (Transition transition : transitions) {
            Element trEl = parentEl.addElement("transition");
            trEl.addAttribute("name", WfUtils.encodeKey(transition.srcTerminal));
            trEl.addAttribute("to", transition.dstName);
        }
    }

    protected Element writeJpdlStringPropertyEl(Element parentEl, String name, String value) {
        Element propEl = parentEl.addElement("property");
        propEl.addAttribute("name", name);
        Element valEl = propEl.addElement("string");
        valEl.addAttribute("value", value);
        return valEl;
    }

    protected Element writeJpdlBooleanPropertyEl(Element parentEl, String name, boolean value) {
        Element propEl = parentEl.addElement("property");
        propEl.addAttribute("name", name);
        Element valEl = propEl.addElement(value ? "true" : "false");
        return valEl;
    }

    protected Element writeJpdlObjectPropertyEl(Element parentEl, String name, String className) {
        Element propEl = parentEl.addElement("property");
        propEl.addAttribute("name", name);
        Element valEl = propEl.addElement("object");
        valEl.addAttribute("class", className);
        return valEl;
    }

    public void writeMessages(Properties properties, String lang) {
        properties.setProperty(name, caption);
    }

    public void writeFormsXml(Element rootEl) throws DesignCompilationException {
    }
}
