/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 22.12.10 15:30
 *
 * $Id$
 */
package workflow.client.web.ui.design;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.TemplateHelper;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.workflow.core.entity.Design;

import java.util.Collections;
import java.util.Map;

public class DesignEditor extends AbstractEditor {

    public DesignEditor(IFrame frame) {
        super(frame);
    }

    @Override
    protected void init(Map<String, Object> params) {
        getDsContext().get("designDs").addListener(
                new DsListenerAdapter() {
                    @Override
                    public void valueChanged(Entity source, String property, Object prevValue, Object value) {
                        if (property.equals("name")) {
                            String src = TemplateHelper.processTemplateFromFile(
                                    "/workflow/client/web/ui/design/new-design-src.ftl",
                                    Collections.singletonMap("name", value)
                            );
                            ((Design) source).setSrc(src);
                        }
                    }
                }
        );
    }
}
