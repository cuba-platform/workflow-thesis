/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.gui.app.design;

import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.core.global.TemplateHelper;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.workflow.core.entity.Design;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

public class DesignEditor extends AbstractEditor {

    @Inject
    protected Datasource<Design> designDs;

    @Override
    public void init(Map<String, Object> params) {
        getDialogOptions().setWidthAuto();

        designDs.addItemPropertyChangeListener(e -> {
            Design design = (Design) getItem();
            if ("name".equals(e.getProperty())) {
                InputStream stream = getClass().getResourceAsStream("new-design-src.ftl");
                if (stream == null) {
                    throw new IllegalStateException("Resource new-design-src.ftl not found");
                }
                String template;
                try {
                    template = IOUtils.toString(stream);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                } finally {
                    try {
                        stream.close();
                    } catch (IOException ioe) {
                        //
                    }
                }
                String name = StringUtils.trimToNull(design.getName());
                if (StringUtils.isNotBlank(name)) {
                    if (PersistenceHelper.isNew(design)) {
                        String src = TemplateHelper.processTemplate(
                                template,
                                Collections.singletonMap("name", e.getValue())
                        );
                        design.setSrc(src);

                    } else {
                        design.setSrc(design.getSrc().replace("\"name\":\"" + e.getPrevValue() + "\",", "\"name\":\"" + name + "\","));
                    }
                }
            }
        });
    }
}
