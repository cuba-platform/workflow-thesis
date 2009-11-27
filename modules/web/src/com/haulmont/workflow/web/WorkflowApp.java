/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 19.11.2009 18:41:17
 *
 * $Id$
 */
package com.haulmont.workflow.web;

import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.core.global.MetadataProvider;

public class WorkflowApp extends App {

    static {
        // set up system properties necessary for com.haulmont.cuba.gui.AppConfig
        System.setProperty(AppConfig.MENU_CONFIG_XML_PROP, "workflow/client/web/menu-config.xml");
        System.setProperty(AppConfig.WINDOW_CONFIG_XML_PROP, "workflow/client/web/screen-config.xml");
        System.setProperty(AppConfig.MESSAGES_PACK_PROP, "workflow.client.web");
    }

    @Override
    protected void deployViews() {
        super.deployViews();
        MetadataProvider.getViewRepository().deployViews("/com/haulmont/workflow/web/workflow.views.xml");
    }
}
