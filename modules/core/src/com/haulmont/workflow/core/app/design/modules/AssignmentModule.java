/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 28.12.10 18:44
 *
 * $Id$
 */
package com.haulmont.workflow.core.app.design.modules;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.app.design.Module;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class AssignmentModule extends Module {

    protected String role;

    protected String description;

    protected Map<String, String> outputs = new HashMap<String, String>();

    protected JSONObject jsOptions;

    public AssignmentModule() {
        activityClassName = "com.haulmont.workflow.core.activity.Assigner";
    }

    @Override
    public void init(Context context) throws DesignCompilationException {
        super.init(context);
        try {
            role = jsValue.getString("role");

            jsOptions = jsValue.optJSONObject("options");
            if (jsOptions != null)
                description = jsOptions.optString("description");

            initOutputs();
        } catch (JSONException e) {
            throw new DesignCompilationException(e);
        }
        if (outputs.isEmpty())
            throw new DesignCompilationException(MessageProvider.formatMessage(getClass(), "exception.noOutputs", name));
    }

    protected void initOutputs() throws JSONException {
        JSONArray jsOutputs = jsValue.optJSONArray("outputs");
        if (jsOutputs != null) {
            for (int i = 0; i < jsOutputs.length(); i++) {
                JSONObject jsOut = jsOutputs.getJSONObject(i);
                String outName = jsOut.getString("name");
                outputs.put(WfUtils.encodeKey(outName), outName);
            }
        }
    }

    @Override
    public Element writeJpdlXml(Element parentEl) throws DesignCompilationException {
        Element el = writeJpdlMainEl(parentEl);
        writeJpdlStringPropertyEl(el, "role", role);
        writeJpdlTransitions(el);
        writeJpdlTimers(el);
        return el;
    }

    @Override
    public void writeMessages(Properties properties, String lang) {
        super.writeMessages(properties, lang);
        Locale locale = new Locale(lang);

        if (!StringUtils.isBlank(description))
            properties.setProperty(name + ".description", description);
        else
            properties.setProperty(name + ".description", MessageProvider.getMessage(Assignment.class, "AssignmentModule.description", locale));

        for (Map.Entry<String, String> entry : outputs.entrySet()) {
            properties.setProperty(name + "." + entry.getKey(), entry.getValue());
            properties.setProperty(name + "." + entry.getKey() + ".Result", entry.getValue());
        }
    }

    @Override
    public void writeFormsXml(Element rootEl) throws DesignCompilationException {
        if (jsOptions == null)
            return;

        try {
            JSONObject jsForms = jsOptions.optJSONObject("forms");
            if (jsForms == null)
                return;

            JSONArray jsFormsList = jsForms.optJSONArray("list");
            if (jsFormsList == null || jsFormsList.length() == 0)
                return;

            for (int i = 0; i < jsFormsList.length(); i++) {
                JSONObject jsForm = jsFormsList.getJSONObject(i);
                String formName = jsForm.getString("name");
                String transition = WfUtils.encodeKey(jsForm.getString("transition"));
                JSONObject jsProperties = jsForm.getJSONObject("properties");
                context.getFormCompiler().writeFormEl(getTransitionEl(rootEl, transition), formName, jsProperties);
            }
        } catch (JSONException e) {
            throw new DesignCompilationException("Unable to compile forms for module " + caption, e);
        }
    }

    protected void writeJpdlTimers(Element parentEl) throws DesignCompilationException {
        if (jsOptions == null)
            return;

        try {
            JSONObject jsTimers = jsOptions.optJSONObject("timers");
            if (jsTimers == null)
                return;

            JSONArray jsTimersList = jsTimers.optJSONArray("list");
            if (jsTimersList == null || jsTimersList.length() == 0)
                return;

            Element element = writeJpdlObjectPropertyEl(
                    parentEl, "timersFactory", "com.haulmont.workflow.core.timer.GenericAssignmentTimersFactory");

            StringBuilder dueDates = new StringBuilder();
            StringBuilder transitions = new StringBuilder();
            StringBuilder scripts = new StringBuilder();
            for (int i = 0; i < jsTimersList.length(); i++) {
                JSONObject jsTimer = jsTimersList.getJSONObject(i);

                JSONObject jsProps = jsTimer.getJSONObject("properties");

                JSONArray jsDueDate = jsProps.getJSONArray("dueDate");
                String dueDate = jsDueDate.getInt(0) + " " + jsDueDate.getString(1) + " " + jsDueDate.getString(2);
                dueDates.append(dueDate);

                String type = jsTimer.getString("type");
                if (type.equals("script")) {
                    String script = jsProps.getString("name");
                    String fileName = scriptNamesMap.get(script);
                    if (fileName == null)
                        throw new DesignCompilationException("Unable to compile timers for module " + caption
                                + ": script '" + script + "' not found");
                    scripts.append(fileName);
                } else {
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

    protected Element getActivityEl(Element rootEl) {
        for (Element element : Dom4j.elements(rootEl, "activity")) {
            if (element.attributeValue("name").equals(name))
                return element;
        }
        Element activityEl = rootEl.addElement("activity");
        activityEl.addAttribute("name", name);
        return activityEl;
    }

    protected Element getTransitionEl(Element rootEl, String transitionName) {
        Element activityEl = getActivityEl(rootEl);
        for (Element element : Dom4j.elements(activityEl, "transition")) {
            if (element.attributeValue("name").equals(transitionName))
                return element;
        }
        Element transitionEl = activityEl.addElement("transition");
        transitionEl.addAttribute("name", transitionName);
        return transitionEl;
    }
}
