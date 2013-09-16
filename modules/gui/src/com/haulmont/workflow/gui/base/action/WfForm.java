/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.base.action;

import java.util.Map;

/**
 * Interface to be implemented by process forms.
 *
 * @author pavlov
 * @version $Id$
 */
public interface WfForm {
    /**
     * Called by Wf framework for initializing process form.
     * @param params parameters passed from {@link com.haulmont.workflow.gui.base.action.FormManager.ScreenFormManager#doBefore}
     */
    void init(Map<String, Object> params);

    /**
     * Called by Wf framework for setting user's comment to assignment
     * @return comment or empty string
     */
    String getComment();
}
