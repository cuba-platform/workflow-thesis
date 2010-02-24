/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 29.01.2010 10:43:14
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.Updatable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity(name = "wf$Calendar")
@Table(name = "WF_CALENDAR")
public class WorkCalendarEntity extends BaseUuidEntity implements Updatable {

    private static final long serialVersionUID = 8935633783119746469L;

    @Column(name = "UPDATE_TS")
    protected Date updateTs;

    @Column(name = "UPDATED_BY", length = LOGIN_FIELD_LEN)
    protected String updatedBy;

    @Column(name = "WORK_DAY")
    private Date day;

    @Column(name = "WORK_DAY_OF_WEEK")
    private Integer dayOfWeek;

    @Column(name = "WORK_START")
    private String start;

    @Column(name = "WORK_END")
    private String end;

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(Date updateTs) {
        this.updateTs = updateTs;
    }
    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public DayOfWeek getDayOfWeek() {
        return DayOfWeek.fromId(this.dayOfWeek);
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek == null ? null : dayOfWeek.getId();
    }
}
