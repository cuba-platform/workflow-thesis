/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app.design;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignProcessVariable;
import com.haulmont.workflow.core.entity.DesignScript;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.json.JSONArray;
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

    protected List<DesignProcessVariable> designProcessVariables = new ArrayList<>();

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
                        AppBeans.get(Messages.class).getMessage(Module.class, "exception.emptyName"));
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
        return propEl.addElement(value ? "true" : "false");
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

    protected void writeJpdlTimers(Element parentEl,JSONObject jsTimers) throws DesignCompilationException {
        if (jsTimers == null)
            return;
        try {
            JSONArray jsTimersList = jsTimers.optJSONArray("list");
            if (jsTimersList == null || jsTimersList.length() == 0)
                return;

            Element element = writeJpdlObjectPropertyEl(
                    parentEl, "timersFactory", getTimersFactory());

            String dueDateType;
            StringBuilder dueDates = new StringBuilder();
            StringBuilder transitions = new StringBuilder();
            StringBuilder scripts = new StringBuilder();
            for (int i = 0; i < jsTimersList.length(); i++) {
                JSONObject jsTimer = jsTimersList.getJSONObject(i);

                JSONObject jsProps = jsTimer.getJSONObject("properties");
                dueDateType = jsProps.getString("dueDateType");
                if ("manual".equals(dueDateType)) {
                    JSONArray jsDueDate = jsProps.getJSONArray("dueDate");
                    String dueDate = jsDueDate.getInt(0) + " " + jsDueDate.getString(1) + " " + jsDueDate.getString(2);
                    dueDates.append(dueDate);
                } else {
                    dueDates.append("process");
                }

                String type = jsTimer.getString("type");
                if (type.equals("script")) {
                    String script = jsProps.getString("name");
                    String fileName=null;
                    for (DesignScript designScript : context.getDesign().getScripts()) {
                        if (ObjectUtils.equals(designScript.getName(), script)
                                && StringUtils.isNotEmpty(designScript.getContent())
                                && designScript.getContent().endsWith(".groovy")) {
                            fileName = "path:" + designScript.getContent();
                        }
                    }

                    if (fileName == null) {
                        fileName = scriptNamesMap.get(script);
                    }
                    if (fileName == null)
                        throw new DesignCompilationException("Unable to compile timers for module " + caption
                                + ": script '" + script + "' not found");
                    scripts.append(fileName);
                } else if (type.equals("transision")) {
                    String transition = WfUtils.encodeKey(jsProps.getString("name"));
                    transitions.append(transition);
                }

                if (i < jsTimersList.length() - 1) {
                    dueDates.append("|");
                    transitions.append("|");
                    scripts.append("|");
                }
            }

            writeJpdlStringPropertyEl(element, "dueDates", dueDates.toString());
            writeJpdlStringPropertyEl(element, "transitions", transitions.toString());
            writeJpdlStringPropertyEl(element, "scripts", scripts.toString());

        } catch (JSONException e) {
            throw new DesignCompilationException("Unable to compile timers for module " + caption, e);
        }
    }

    public Boolean isVariableExists(String key) throws DesignCompilationException {
        try {
            if (jsValue.isNull("variables")) return false;
            JSONObject variables = jsValue.getJSONObject("variables");
            Iterator keys = variables.keys();
            while (keys.hasNext()) {
                String variableKey = (String) keys.next();
                if (key.equals(variableKey)) return true;

            }
            return false;
        } catch (JSONException e) {
            throw new DesignCompilationException("Unable to get variables for module " + caption, e);
        }
    }

    public List<DesignProcessVariable> generateDesignProcessVariables() throws DesignCompilationException {
        try {
            if (jsValue.isNull("variables")) return designProcessVariables;
            if (jsValue.isNull("options")) return designProcessVariables;
            JSONObject variables = jsValue.getJSONObject("variables");
            JSONObject options = jsValue.getJSONObject("options");
            Iterator keys = variables.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                String alias = variables.getString(key);
                if (StringUtils.isNotBlank(alias)) {
                    DesignProcessVariable variable = new DesignProcessVariable();
                    variable.setAlias(alias);
                    variable.setName(alias);
                    variable.setPropertyName(key);
                    variable.setModuleName(name);
                    String value = options.optString(key, null);
                    variable.setValue(value);
                    variable.setShouldBeOverridden(StringUtils.isBlank(value));
                    designProcessVariables.add(variable);
                }
            }
            return designProcessVariables;
        } catch (JSONException e) {
            throw new DesignCompilationException("Unable to get variables for module " + caption, e);
        }
    }

    protected DesignProcessVariable getVariableByPropertyName(String propertyName) {
        for (DesignProcessVariable variable : designProcessVariables) {
            if (propertyName.equals(variable.getPropertyName())) {
                return variable;
            }
        }
        return null;
    }

    protected String getTimersFactory() {
        return null;
    }
}
