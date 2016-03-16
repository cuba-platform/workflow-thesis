/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.gui.base.action;

import java.util.HashMap;
import java.util.Map;

/**
 */
@SuppressWarnings("unused")
public class FormResult {

    protected Map<String, Object> resultParams = new HashMap<>();


    public void addParam(String key, Object value) {
        resultParams.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getParam(String key) {
        return (T) resultParams.get(key);
    }

    public Map<String, Object> getResultParams() {
        return resultParams;
    }

}
