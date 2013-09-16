/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core;

import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.workflow.core.app.TimerManagerAPI;
import com.haulmont.workflow.core.app.WfEngineAPI;
import com.haulmont.cuba.core.Locator;
import com.haulmont.workflow.core.app.WorkCalendarAPI;
import com.haulmont.workflow.core.global.TimeUnit;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.RepositoryService;

import java.util.HashMap;
import java.util.Map;

public class WfHelper {

    public static Map<String, TimeUnit> timeUnits = new HashMap<String, TimeUnit>();

    static {
        timeUnits.put("minute", TimeUnit.MINUTE);
        timeUnits.put("hour", TimeUnit.HOUR);
        timeUnits.put("day", TimeUnit.DAY);
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
        TimeUnit tu = timeUnits.get(unit);
        if (tu == null && unit.endsWith("s")) {
            tu = timeUnits.get(unit.substring(0, unit.length()-1));
        }
        if (tu == null)
            throw new UnsupportedOperationException("Unsupported time unit: " + expression);

        if (parts.length > 2 && parts[1].equalsIgnoreCase("business")) {
            WorkCalendarAPI wcal = Locator.lookup(WorkCalendarAPI.NAME);
            return wcal.getAbsoluteMillis(TimeProvider.currentTimestamp(), num, tu);
        } else {
            return num * tu.getMillis();
        }
    }
}
