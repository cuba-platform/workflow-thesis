/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 19.01.2010 10:08:33
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base.action;

import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.settings.Settings;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;

public abstract class AbstractForm extends AbstractWindow {

    public AbstractForm(IFrame frame) {
        super(frame);
    }

    @Override
    public void applySettings(Settings settings) {
        super.applySettings(settings);
        WebComponentsHelper.unwrap(frame).getWindow().setClosable(false);
    }

    public abstract String getComment();
}
