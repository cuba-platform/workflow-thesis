/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.base.attachments;

import com.haulmont.cuba.gui.components.*;

import java.util.ArrayList;
import java.util.Map;

/**
 * <p>$Id$</p>
 *
 * @author pavlov
 */
public class RemoveAttachmentConfirmDialog extends AbstractWindow {
    public static final String OPTION_LAST_VERSION = "option.lastVersion";
    public static final String OPTION_ALL_VERSIONS = "option.allVersions";

    public RemoveAttachmentConfirmDialog(IFrame frame) {
        super(frame);
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        final OptionsGroup options = getComponent("options");

        ArrayList optionList = new ArrayList();
        optionList.add(getMessage(OPTION_LAST_VERSION));
        optionList.add(getMessage(OPTION_ALL_VERSIONS));

        options.setOptionsList(optionList);
        options.setValue(getMessage(OPTION_LAST_VERSION));

        addAction(new AbstractAction("ok") {
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
            public void actionPerform(Component component) {
                RemoveAttachmentConfirmDialog.this.close("cancel");
            }
        });
    }
}
