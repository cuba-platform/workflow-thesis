/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.app.base.attachments;

import com.haulmont.cuba.gui.components.*;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author pavlov
 * @version $Id$
 */
public class RemoveAttachmentConfirmDialog extends AbstractWindow {
    public static final String OPTION_LAST_VERSION = "option.lastVersion";
    public static final String OPTION_ALL_VERSIONS = "option.allVersions";

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        final OptionsGroup options = (OptionsGroup) getComponentNN("options");

        ArrayList<String> optionList = new ArrayList<>();
        optionList.add(getMessage(OPTION_LAST_VERSION));
        optionList.add(getMessage(OPTION_ALL_VERSIONS));

        options.setOptionsList(optionList);
        options.setValue(getMessage(OPTION_LAST_VERSION));

        addAction(new AbstractAction("ok") {
            @Override
            public void actionPerform(Component component) {
                String value = options.getValue();
                if (getMessage(OPTION_LAST_VERSION).equals(value)) {
                    close(OPTION_LAST_VERSION);
                } else {
                    close(OPTION_ALL_VERSIONS);
                }
            }
        });

        addAction(new AbstractAction("cancel") {
            @Override
            public void actionPerform(Component component) {
                RemoveAttachmentConfirmDialog.this.close("cancel");
            }
        });
    }
}