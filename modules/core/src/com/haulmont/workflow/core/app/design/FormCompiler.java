/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app.design;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.dom4j.Element;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author krivopustov
 * @version $Id$
 */
public class FormCompiler {

    public enum TransitionStyle {
        SUCCESS("success", "wf-success"),
        FAILURE("failure", "wf-failure");

        private String id;
        private String styleName;

        TransitionStyle(String id, String styleName) {
            this.styleName = styleName;
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static TransitionStyle fromId(String id) {
            for (TransitionStyle ts : TransitionStyle.values()) {
                if (ts.getId().equals(id)) {
                    return ts;
                }
            }
            return null;
        }

        public String getStyleName() {
            return styleName;
        }
    }

    private Map<String, String> builderClassNames;

    private volatile Map<String, Class<?>> builderClasses;

    // set from spring.xml
    public void setBuilderClasses(Map<String, String> builderClassNames) {
        this.builderClassNames = builderClassNames;
    }

    private Map<String, Class<?>> getBuilderClasses() {
        if (builderClasses == null) {
            synchronized (this) {
                builderClasses = new HashMap<>();

                for (Map.Entry<String, String> entry : builderClassNames.entrySet()) {
                    builderClasses.put(entry.getKey(), AppBeans.get(Scripting.class).loadClass(entry.getValue()));
                }
            }
        }
        return builderClasses;
    }

    public void writeFormEl(Element parentEl, String formName, String transitionStyle, JSONObject jsProperties,
                            Design design) throws DesignCompilationException {
        Class<?> cls = getBuilderClasses().get(formName);
        if (cls == null) {
            throw new RuntimeException("Unsupported form name: " + formName);
        }
        FormBuilder builder;
        try {
            builder = (FormBuilder) cls.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        builder.init(design);
        builder.writeFormEl(parentEl, jsProperties);
        TransitionStyle style = TransitionStyle.fromId(transitionStyle);
        if (style != null) {
            parentEl.addAttribute("style", TransitionStyle.fromId(transitionStyle).getStyleName());
        }
    }
}