/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.web.app.design;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.workflow.gui.app.design.DesignBrowser;

public class WebDesignBrowserCompanion implements DesignBrowser.Companion {
    @Override
    public void openDesigner(String designerUrl) {
        String webAppUrl = ControllerUtils.getLocationWithoutParams();
        String url = webAppUrl + designerUrl;
        App.getInstance().getWindowManager().showWebPage(url, ParamsMap.of("tryToOpenAsPopup", Boolean.TRUE));
    }
}
