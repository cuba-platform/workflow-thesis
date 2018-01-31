package com.haulmont.workflow.core.timer;

import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.workflow.core.app.WorkCalendarAPI;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.CardRole;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;

@Component(OverdueAssignmentDueDateHelperBean.NAME)
public class OverdueAssignmentDueDateHelperBean {
    public static final String NAME = "workflow_OverdueAssignmentDueDateHelperBean";

    @Inject
    protected WorkCalendarAPI workCalendar;

    @Inject
    protected TimeSource timeSource;

    public Date getDueDate(Assignment assignment) {
        CardRole cardRole = assignment.getCardRole();
        return workCalendar.addInterval(timeSource.currentTimestamp(), cardRole.getDuration(), cardRole.getTimeUnit());
    }
}