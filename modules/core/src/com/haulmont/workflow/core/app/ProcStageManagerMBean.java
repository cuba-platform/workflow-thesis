/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 17.01.11 15:02
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

public interface ProcStageManagerMBean {
    String NAME = "workflow_ProcStageManager";

    void processOverdueStages();
}
