/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.cuba.core.global.ScriptingProvider;
import com.haulmont.workflow.core.global.TimeUnit;
import groovy.lang.Binding;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

@Entity(name = "wf$ProcStage")
@Table(name = "WF_PROC_STAGE")
@NamePattern("%s|name")
@SystemLevel
public class ProcStage extends StandardEntity {
    private static final long serialVersionUID = 6943011386558120006L;

    @Column(name = "NAME", length = 255)
    protected String name;

    @Column(name = "START_ACTIVITY", length = 200)
    protected String startActivity;

    @Column(name = "END_ACTIVITY", length = 200)
    protected String endActivity;

    @Column(name = "END_TRANSITION", length = 200)
    protected String endTransition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_STAGE_TYPE_ID")
    protected ProcStageType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROC_ID")
    protected Proc proc;

    @Column(name = "TIME_UNIT")
    protected String timeUnit = "D";

    @Column(name = "DURATION", length = 3)
    protected Integer duration;

    @Column(name = "NOTIFICATION_SCRIPT")
    protected String notificationScript;

    @Column(name = "DURATION_SCRIPT_ENABLED")
    protected Boolean durationScriptEnabled = false;

    @Column(name = "DURATION_SCRIPT")
    protected String durationScript;

    @Column(name = "NOTIFY_CURRENT_ACTOR")
    protected Boolean notifyCurrentActor = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "WF_PROC_STAGE_PROC_ROLE",
        joinColumns = @JoinColumn(name = "PROC_STAGE_ID", referencedColumnName = "ID"),
        inverseJoinColumns = @JoinColumn(name = "PROC_ROLE_ID", referencedColumnName = "ID"))
    protected List<ProcRole> procRoles;

    @MetaProperty
    public String getLocStartActivity() {
        return MessageProvider.getMessage(proc.getMessagesPack(), startActivity);
    }

    @MetaProperty
    public String getLocEndActivity() {
        return MessageProvider.getMessage(proc.getMessagesPack(), endActivity);
    }

    public String getStartActivity() {
        return startActivity;
    }

    public void setStartActivity(String startActivity) {
        this.startActivity = startActivity;
    }

    public String getEndActivity() {
        return endActivity;
    }

    public void setEndActivity(String endActivity) {
        this.endActivity = endActivity;
    }

    public String getEndTransition() {
        return endTransition;
    }

    public void setEndTransition(String endTransition) {
        this.endTransition = endTransition;
    }

//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Proc getProc() {
        return proc;
    }

    public void setProc(Proc proc) {
        this.proc = proc;
    }

    public TimeUnit getTimeUnit() {
        return TimeUnit.fromId(timeUnit);
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit == null ? null : timeUnit.getId();
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getNotificationScript() {
        return notificationScript;
    }

    public void setNotificationScript(String notificationScript) {
        this.notificationScript = notificationScript;
    }

    public List<ProcRole> getProcRoles() {
        return procRoles;
    }

    public void setProcRoles(List<ProcRole> procRoles) {
        this.procRoles = procRoles;
    }

    public ProcStageType getType() {
        return type;
    }

    public void setType(ProcStageType type) {
        this.type = type;
    }

    public Boolean getNotifyCurrentActor() {
        return notifyCurrentActor;
    }

    public void setNotifyCurrentActor(Boolean notifyCurrentActor) {
        this.notifyCurrentActor = notifyCurrentActor;
    }

    public String getDurationScript() {
        return durationScript;
    }

    public void setDurationScript(String durationScript) {
        this.durationScript = durationScript;
    }

    public Boolean getDurationScriptEnabled() {
        return durationScriptEnabled;
    }

    public void setDurationScriptEnabled(Boolean durationScriptEnabled) {
        this.durationScriptEnabled = durationScriptEnabled;
    }

    public Map calculateStageDuration(Card card) {
        if (durationScriptEnabled && (durationScript != null)) {
            Binding binding = new Binding();
            binding.setVariable("card", card);
            ScriptingProvider.evaluateGroovy(durationScript, binding);
            return binding.getVariables();
        }
        Map result = new java.util.HashMap();
        result.put("duration", duration);
        result.put("timeUnit", getTimeUnit());
        return result;
    }
}
