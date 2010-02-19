/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 29.01.2010 13:31:30
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

public interface WorkCalendarMBean {

    int getCacheSize();

    void invalidateCache();

    String fillWorkCalendar();
}
