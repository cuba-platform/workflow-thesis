/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.gui.components.AbstractAccessData;
import org.apache.commons.lang.BooleanUtils;

import java.util.Map;

/**
 * @author subbotin
 * @version $Id$
 */
public class ReassignFormAccessData extends AbstractAccessData

{
    private Map<String, Object> params;

    public ReassignFormAccessData(Map<String, Object> params) {
        super(params);
        this.params = params;
    }

    public boolean isCommentVisible() {
        return BooleanUtils.isTrue((Boolean) params.get("commentVisible"));
    }
}
