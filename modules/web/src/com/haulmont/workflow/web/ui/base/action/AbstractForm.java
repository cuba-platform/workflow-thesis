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
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.ShortcutAction;
import com.haulmont.cuba.gui.settings.Settings;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.vaadin.ui.Window;

import javax.inject.Named;
import java.util.Map;

public abstract class AbstractForm extends AbstractWindow {

    @Named("windowActions.windowCommit")
    private Button windowCommit;

    public AbstractForm(IFrame frame) {
        super(frame);
    }

    public void init(Map<String, Object> params) {
        super.init(params);
        if (windowCommit != null) {
            com.vaadin.ui.Button vWindowCommit = (com.vaadin.ui.Button) WebComponentsHelper.unwrap(windowCommit);
            vWindowCommit.setClickShortcut(ShortcutAction.Key.ENTER.getCode(), ShortcutAction.Modifier.CTRL.getCode());
        }
    }

    @Override
    public void applySettings(Settings settings) {
        super.applySettings(settings);
        Window window = WebComponentsHelper.unwrap(frame).getWindow();
        if (window.isModal()) {
            window.setClosable(false);
            window.setResizable(false);
        }
    }

    public abstract String getComment();
}
