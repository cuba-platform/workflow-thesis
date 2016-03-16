/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.activity

import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.Scripting
import com.haulmont.workflow.core.app.design.DesignDeployer
import com.haulmont.workflow.core.entity.Card
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.jbpm.api.activity.ActivityExecution

class Decision extends ProcessVariableActivity {

    final String YES_TRANSITION = "yes";
    final String NO_TRANSITION = "no";

    String scriptName
    String scriptPath

    private Log log = LogFactory.getLog(Decision.class);

    void execute(ActivityExecution execution) {
        super.execute(execution);
        Card card = ActivityHelper.findCard(execution)

        String fileName
        if (scriptName != null) {
            String processKey = card.getProc().getProcessPath()
            fileName = processKey + "/" + DesignDeployer.SCRIPTS_DIR + "/" + scriptName
        } else {
            fileName = scriptPath
        }
        log.debug("Running script " + fileName)
        Scripting scripting = AppBeans.get(Scripting.class);
        Object result = scripting.runGroovyScript(fileName, prepareParams(card, execution))
        if (result instanceof Boolean) {
            if (result)
                execution.take(YES_TRANSITION)
            else
                execution.take(NO_TRANSITION)
        } else {
            throw new RuntimeException(String.format("The script %s must return a boolean value", scriptPath));
        }
    }

    protected Map<String, Object> prepareParams(Card card, ActivityExecution execution) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("card", card);
        params.put("activity", execution.getActivityName())
        return params;
    }
}
