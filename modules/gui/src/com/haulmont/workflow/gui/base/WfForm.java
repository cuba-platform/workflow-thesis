/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.gui.base;

import java.util.Map;

/**
 * @author pavlov
 * @version $Id$
 */
public interface WfForm {
    void init(Map<String, Object> params);

    String getComment();
}
