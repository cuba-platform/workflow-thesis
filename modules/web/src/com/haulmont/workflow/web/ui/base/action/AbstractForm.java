/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.web.ui.base.action;

import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.settings.Settings;
import com.haulmont.workflow.gui.base.action.WfForm;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * @author krivopustov
 * @version $Id$
 */
public abstract class AbstractForm extends AbstractWindow implements WfForm {

    @Inject
    protected Configuration configuration;

    public void init(Map<String, Object> params) {
        super.init(params);
        initActions();
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

    protected void initActions(){
        ClientConfig clientConfig = configuration.getConfig(ClientConfig.class);
        addAction(new AbstractAction("windowCommit", clientConfig.getCommitShortcut()) {
            public void actionPerform(Component component) {
                onWindowCommit();
            }

            @Override
            public String getCaption() {
                return messages.getMessage(AppConfig.getMessagesPack(), "actions.Ok");
            }
        });

        addAction(new AbstractAction("windowClose", clientConfig.getCloseShortcut()) {
            public void actionPerform(Component component) {
                onWindowClose();
            }

            @Override
            public String getCaption() {
                return messages.getMessage(AppConfig.getMessagesPack(), "actions.Cancel");
            }
        });
    }

    protected void onWindowCommit(){

    }

    protected void onWindowClose(){

    }

    public abstract String getComment();
}