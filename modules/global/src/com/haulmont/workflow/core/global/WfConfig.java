/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 03.02.11 9:52
 *
 * $Id$
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

    @Property("workflow.oneAttachmentUploaderEnabled")
    @DefaultBoolean(false)
    boolean getOneAttachmentUploaderEnabled();
    void setOneAttachmentUploaderEnabled(boolean value);
}
