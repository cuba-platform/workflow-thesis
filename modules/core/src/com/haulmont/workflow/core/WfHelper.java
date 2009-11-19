/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 19.11.2009 12:14:01
 *
 * $Id$
 */
package com.haulmont.workflow.core;

import com.haulmont.workflow.core.app.WfEngineAPI;
import com.haulmont.workflow.core.app.WfEngineMBean;
import com.haulmont.cuba.core.Locator;

public class WfHelper {

    public static WfEngineAPI getWfEngineAPI() {
        WfEngineMBean mbean = Locator.lookupMBean(WfEngineMBean.class);
        return mbean.getAPI();
    }
}
