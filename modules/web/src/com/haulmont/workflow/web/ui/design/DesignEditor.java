/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 22.12.10 15:30
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.design;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.TemplateHelper;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.workflow.core.entity.Design;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

public class DesignEditor extends AbstractEditor {

    public DesignEditor(IFrame frame) {
        super(frame);
    }

    @Override
    public void init(Map<String, Object> params) {
        getDsContext().get("designDs").addListener(
                new DsListenerAdapter() {
                    @Override
                    public void valueChanged(Entity source, String property, Object prevValue, Object value) {
                        if (property.equals("name")) {
                            InputStream stream = getClass().getResourceAsStream("new-design-src.ftl");
                            if (stream == null)
                                throw new IllegalStateException("Resource new-design-src.ftl not found");
                            String template;
                            try {
                                template = IOUtils.toString(stream);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } finally {
                                try {
                                    stream.close();
                                } catch (IOException e) {
                                    //
                                }
                            }
                            String src = TemplateHelper.processTemplate(
                                    template,
                                    Collections.singletonMap("name", value)
                            );
                            ((Design) source).setSrc(src);
                        }
                    }
                }
        );
    }
}
