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
import com.haulmont.cuba.core.config.Prefix;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;

@Prefix("workflow.")
@Source(type = SourceType.APP)
public interface WfConfig extends Config {

    @Default("wfdesigner/workflow/main.ftl")
    String getDesignerUrl();

    @Default("AttachmentType.attachment")
    String getDefaultAttachmentType();
    
    @Default("/workflow/NotificationMatrixTemplate.xls")
    String getNotificationTemplatePath();
}
