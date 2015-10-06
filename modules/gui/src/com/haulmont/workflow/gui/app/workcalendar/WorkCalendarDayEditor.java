/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.app.workcalendar;

import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.ValidationErrors;
import com.haulmont.workflow.core.entity.WorkCalendarEntity;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * @author gaslov
 * @version $Id$
 */
public class WorkCalendarDayEditor extends AbstractEditor<WorkCalendarEntity> {

    @Inject
    protected DataManager dataManager;

    protected boolean isWorkDayEditor;

    @Override
    protected void postInit() {
        isWorkDayEditor = getCaption().equals(getMessage("workDayEditCaption"));
    }

    @Override
    protected void postValidate(ValidationErrors errors) {
        WorkCalendarEntity item = getItem();
        Date startNew = item.getStart();
        Date endNew = item.getEnd();
        if (startNew != null && endNew != null && startNew.compareTo(endNew) >= 0) {
            errors.add(getMessage("startAfterEnd"));
            return;
        } else if (startNew == null && endNew != null ||
                endNew == null && startNew != null) {
            errors.add(getMessage("onlyOneTimeSpecified"));
            return;
        }

        for (WorkCalendarEntity wce : getCalendarList()) {
            if (wce.getId().equals(item.getId())) {
                continue;
            }
            Date start = wce.getStart();
            Date end = wce.getEnd();
            if (start != null && end != null) {
                if (startNew != null) {
                    if (startNew.compareTo(start) == 0
                            || startNew.before(start) && endNew.after(start)
                            || startNew.after(start) && startNew.compareTo(end) < 0) {
                        errors.add(getSuitableMessage("timeIntervalIntersection"));
                        break;
                    }
                } else {
                    errors.add(getSuitableMessage("existsEntryWithInterval"));
                    break;
                }
            } else if (startNew != null) {
                errors.add(getSuitableMessage("existsDayOffEntry"));
            } else {
                errors.add(getSuitableMessage("alreadyExistsDayOffEntry"));
            }
        }
    }

    protected List<WorkCalendarEntity> getCalendarList() {
        WorkCalendarEntity item = getItem();
        LoadContext loadContext = new LoadContext(item.getClass());
        loadContext.setView(View.LOCAL);
        if (isWorkDayEditor) {
            loadContext.setQueryString("select c from wf$Calendar c where c.dayOfWeek = :workDay")
                    .setParameter("workDay", item.getDayOfWeek().getId());
        } else {
            loadContext.setQueryString("select c from wf$Calendar c where c.day = :day")
                    .setParameter("day", item.getDay());
        }
        return dataManager.loadList(loadContext);
    }

    protected String getSuitableMessage(String key) {
        String suffix = isWorkDayEditor ? "WD" : "ED";
        return getMessage(key + suffix);
    }
}