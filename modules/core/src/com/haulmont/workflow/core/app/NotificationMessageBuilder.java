/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;

import java.util.Map;

public interface NotificationMessageBuilder {
    NotificationMatrixMessage build(Map<String, Object> parameters);
}
