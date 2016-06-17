/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.global;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;
import com.haulmont.cuba.core.config.defaults.DefaultBoolean;

@Source(type = SourceType.APP)
public interface WfConfig extends Config {

    @Property("workflow.designerUrl")
    @Default("wfdesigner/workflow/main.ftl")
    String getDesignerUrl();

    @Property("workflow.defaultAttachmentType")
    @Default("AttachmentType.attachment")
    String getDefaultAttachmentType();

    @Property("workflow.notificationTemplatePath")
    @Default("/com/haulmont/workflow/core/NotificationMatrixTemplate.xls")
    String getNotificationTemplatePath();

    @Property("workflow.systemAssignmentOutcomes")
    String getSystemAssignmentOutcomes();

    @Property("workflow.oneAttachmentUploaderEnabled")
    @DefaultBoolean(false)
    boolean getOneAttachmentUploaderEnabled();

    @Property("workflow.defaultTimersFactory")
    @Default("com.haulmont.workflow.core.timer.GenericAssignmentTimersFactory")
    String getDefaultTimersFactory();

    void setOneAttachmentUploaderEnabled(boolean value);
}
