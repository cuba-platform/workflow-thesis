/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.app.design.modules;

import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class ParallelAssignmentModule extends AssignmentModule {

    public ParallelAssignmentModule() {
        activityClassName = "com.haulmont.workflow.core.activity.UniversalAssigner";
    }

    @Override
    public Element writeJpdlXml(Element parentEl) throws DesignCompilationException {
        Element element = super.writeJpdlXml(parentEl);
        if (jsOptions != null) {
            String successTransition = jsOptions.optString("successTransition");
            if (!StringUtils.isBlank(successTransition)) {
                writeJpdlStringPropertyEl(element, "successTransition", encodeSuccessTransition(successTransition));
            }
            String statusesForFinish = jsOptions.optString("statusesForFinish");
            if (!StringUtils.isBlank(statusesForFinish)) {
                writeJpdlStringPropertyEl(element, "statusesForFinish", encodeSuccessTransition(statusesForFinish));
            }

            boolean refusedOnly = jsOptions.optBoolean("refusedOnly");
            if (refusedOnly) {
                writeJpdlBooleanPropertyEl(element, "refusedOnly", true);
            }

            boolean finishBySingleUser = jsOptions.optBoolean("finishBySingleUser");
            if (finishBySingleUser) {
                writeJpdlBooleanPropertyEl(element, "finishBySingleUser", true);
            }

        }
        return element;
    }

    private String encodeSuccessTransition(String successTransition) {
        StringBuilder sb = new StringBuilder();
        String[] transitions = successTransition.split(",");
        for (int i = 0; i < transitions.length; i++) {
            sb.append(WfUtils.encodeKey(transitions[i]));
            if (i < transitions.length - 1) sb.append(",");
        }
        return sb.toString();
    }
}
