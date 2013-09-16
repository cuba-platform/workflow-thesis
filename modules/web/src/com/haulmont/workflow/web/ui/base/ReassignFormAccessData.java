/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.gui.components.AbstractAccessData;
import org.apache.commons.lang.BooleanUtils;

import java.util.Map;

/**
 * @author subbotin
 * @version $Id$
 */
public class ReassignFormAccessData extends AbstractAccessData {
    private Map<String, Object> params;

    public ReassignFormAccessData(Map<String, Object> params) {
        super(params);
        this.params = params;
    }

    public boolean isCommentVisible() {
        return BooleanUtils.isTrue((Boolean) params.get("commentVisible"));
    }
}
