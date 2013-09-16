/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app.design.modules;

import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.app.design.Module;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

/**
 * <p>$Id$</p>
 *
 * @author devyatkin
 */

public class EmptyModule extends Module {
    public EmptyModule(){
        activityClassName="com.haulmont.workflow.core.activity.EmptyModule";
    }

    public void init(Context context) throws DesignCompilationException {
        super.init(context);

        JSONObject jsOptions = jsValue.optJSONObject("options");
        if (jsOptions != null) {
            String name = jsOptions.optString("name");
            if (!StringUtils.isBlank(name)) {
                this.name = WfUtils.encodeKey(name);
                this.caption = name;
            }
        }
    }
}
