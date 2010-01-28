/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 19.11.2009 12:14:01
 *
 * $Id$
 */
package com.haulmont.workflow.core;

import com.haulmont.workflow.core.app.TimerManagerAPI;
import com.haulmont.workflow.core.app.WfEngineAPI;
import com.haulmont.cuba.core.Locator;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.RepositoryService;

import java.util.HashMap;
import java.util.Map;

public class WfHelper {

    private static Map<String, Long> timeUnits = new HashMap<String, Long>();

    static {
        timeUnits.put("minute", 60000L);
        timeUnits.put("hour", 3600000L);
        timeUnits.put("day", 86400000L);
    }

    public static WfEngineAPI getEngine() {
        return Locator.lookup(WfEngineAPI.NAME);
    }

    public static ProcessEngine getProcessEngine() {
        return getEngine().getProcessEngine();
    }

    public static ExecutionService getExecutionService() {
        return getEngine().getProcessEngine().getExecutionService();
    }

    public static RepositoryService getRepositoryService() {
        return getEngine().getProcessEngine().getRepositoryService();
    }

    public static TimerManagerAPI getTimerManager() {
        return Locator.lookup(TimerManagerAPI.NAME);
    }

    public static Long getTimeMillis(String expression) {
        String[] parts = expression.split("\\s+");
        Integer num = Integer.valueOf(parts[0]);
        String unit = parts.length == 2 ? parts[1] : parts[2];
        Long millis = timeUnits.get(unit);
        if (millis == null && unit.endsWith("s"))
            millis = timeUnits.get(unit.substring(0, unit.length()-1));
        if (millis == null)
            throw new UnsupportedOperationException("Unsupported time unit: " + expression);

        return num * millis;
    }
}
