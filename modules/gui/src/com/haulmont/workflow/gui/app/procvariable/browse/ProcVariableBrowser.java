/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.gui.app.procvariable.browse;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.workflow.core.entity.DesignProcessVariable;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.entity.ProcVariable;
import com.haulmont.workflow.gui.app.designprocessvariables.browse.AbstractProcVariableBrowser;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ProcVariableBrowser extends AbstractProcVariableBrowser {

    private Proc proc;

    @Inject
    private Metadata metadata;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        proc = (Proc) params.get("proc");
        table.addAction(new AbstractAction("override") {
            @Override
            public void actionPerform(Component component) {
                getDialogOptions().setHeight("300px");
                openLookup("wf$DesignProcessVariable.browse", new Handler() {
                    @Override
                    public void handleLookup(Collection items) {
                        for (Object item : items) {
                            ProcVariable processVariable = (ProcVariable)
                                    ((DesignProcessVariable) item).copyTo(metadata.create(ProcVariable.class));
                            processVariable.setProc(proc);
                            processVariable.setOverridden(true);
                            table.getDatasource().addItem(processVariable);
                        }
                        table.getDatasource().commit();
                    }
                }, WindowManager.OpenType.DIALOG, ParamsMap.of("design", proc.getDesign()));
            }
        });
        if (proc.getDesign() == null) table.getAction("override").setEnabled(false);
    }

    @Override
    protected Map<String, Object> getInitialValuesForCreate() {
        return Collections.<String, Object>singletonMap("proc", proc);
    }
}