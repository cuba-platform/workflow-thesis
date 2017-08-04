/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app.prettytime;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.DefaultString;

@Source(type = SourceType.APP)
public interface PrettyTimeConfig extends Config {

    @Property("cuba.prettyTimeProperties")
    @DefaultString("com/haulmont/workflow/core/app/prettytime/prettytime.properties")
    String getPrettyTimeProperties();
}