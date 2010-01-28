/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 25.01.2010 14:25:42
 *
 * $Id$
 */
package com.haulmont.workflow.core.timer;

public interface TimerAction {

    void execute(TimerActionContext context);
}
