/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.base.action;

import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.ShortcutAction;
import com.haulmont.cuba.gui.settings.Settings;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.workflow.gui.base.action.WfForm;

import javax.inject.Named;
import java.util.Map;

/**
 * @author krivopustov
 * @version $Id$
 */
public abstract class AbstractForm extends AbstractWindow implements WfForm {

    @Named("windowActions.windowCommit")
    private Button windowCommit;

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
//        vaadin7
//        Window window = WebComponentsHelper.unwrap(frame).getWindow();
//        if (window.isModal()) {
//            window.setClosable(false);
//            window.setResizable(false);
//        }
    }

    public abstract String getComment();
}