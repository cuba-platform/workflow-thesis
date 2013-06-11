/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.designprocessvariables.browse;

import com.haulmont.workflow.core.entity.Design;

import java.util.Collections;
import java.util.Map;

/**
 * <p>$Id: DesignProcessVariableBrowser.java 10533 2013-02-12 08:55:55Z zaharchenko $</p>
 *
 * @author Zaharchenko
 */
public class DesignProcessVariableBrowser extends AbstractProcVariableBrowser {

    private Design design;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        design = (Design) params.get("design");
    }

    @Override
    protected Map<String, Object> getInitialValuesForCreate() {
        return Collections.<String, Object>singletonMap("design", design);
    }
}
