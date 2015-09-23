/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.base.action;

import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.settings.Settings;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Map;

/**
 * @author krivopustov
 * @version $Id$
 */
public abstract class AbstractForm extends AbstractWindow implements WfForm {

    @Inject
    protected Configuration configuration;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        initActions();
    }

    @Override
    public void applySettings(Settings settings) {
        super.applySettings(settings);
    }

    protected void initActions() {
        ClientConfig clientConfig = configuration.getConfig(ClientConfig.class);
        addAction(new AbstractAction("windowCommit", clientConfig.getCommitShortcut()) {
            @Override
            public void actionPerform(Component component) {
                onWindowCommit();
            }

            @Override
            public String getCaption() {
                return messages.getMessage(AppConfig.getMessagesPack(), "actions.Ok");
            }
        });

        addAction(new AbstractAction("windowClose", clientConfig.getCloseShortcut()) {
            @Override
            public void actionPerform(Component component) {
                onWindowClose();
            }

            @Override
            public String getCaption() {
                return messages.getMessage(AppConfig.getMessagesPack(), "actions.Cancel");
            }
        });
    }

    protected void onWindowCommit() {

    }

    protected void onWindowClose() {

    }

    @Override
    public abstract String getComment();

    @Override
    @Nullable
    public FormResult getFormResult() {
        return null;
    }
}