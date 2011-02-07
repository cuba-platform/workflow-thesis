/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 17.01.11 15:25
 *
 * $Id$
 */
package com.haulmont.workflow.core.app.design;

import com.haulmont.cuba.core.global.ScriptingProvider;
import org.dom4j.Element;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FormCompiler {

    private Map<String, String> builderClassNames;

    private volatile Map<String, Class<? extends FormBuilder>> builderClasses;

    // set from spring.xml
    public void setBuilderClasses(Map<String, String> builderClassNames) {
        this.builderClassNames = builderClassNames;
    }

    private Map<String, Class<? extends FormBuilder>> getBuilderClasses() {
        if (builderClasses == null) {
            synchronized (this) {
                builderClasses = new HashMap<String, Class<? extends FormBuilder>>();

                for (Map.Entry<String, String> entry : builderClassNames.entrySet()) {
                    builderClasses.put(entry.getKey(), ScriptingProvider.loadClass(entry.getValue()));
                }
            }
        }
        return builderClasses;
    }

    public void writeFormEl(Element parentEl, String formName, JSONObject jsProperties) {
        Class<? extends FormBuilder> cls = getBuilderClasses().get(formName);
        if (cls == null) {
            throw new RuntimeException("Unsupported form name: " + formName);
        }
        FormBuilder builder;
        try {
            builder = cls.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        builder.writeFormEl(parentEl, jsProperties);
    }
}
