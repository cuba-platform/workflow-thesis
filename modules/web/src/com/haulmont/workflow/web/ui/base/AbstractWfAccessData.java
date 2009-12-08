/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 07.12.2009 13:25:20
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.gui.components.AbstractAccessData;

import java.util.Map;

public abstract class AbstractWfAccessData extends AbstractAccessData {

    public AbstractWfAccessData(Map<String, Object> params) {
        super(params);
    }

    public abstract boolean getSaveEnabled();

    public abstract boolean getStartProcessEnabled();
}
