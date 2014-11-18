/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.app.procvariable.browse;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.workflow.core.entity.DesignProcessVariable;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.entity.ProcVariable;
import com.haulmont.workflow.gui.app.designprocessvariables.browse.AbstractProcVariableBrowser;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * <p>$Id: ProcVariableBrowser.java 10560 2013-02-13 08:20:22Z zaharchenko $</p>
 *
 * @author Zaharchenko
 */
public class ProcVariableBrowser extends AbstractProcVariableBrowser {

    private Proc proc;

    private static final long serialVersionUID = 4880567976812400606L;

    public ProcVariableBrowser() {
        super();
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        proc = (Proc) params.get("proc");
        table.addAction(new AbstractAction("override") {
            @Override
            public void actionPerform(Component component) {
                getDialogParams().setHeight(300);
                openLookup("wf$DesignProcessVariable.browse", new Window.Lookup.Handler() {
                    @Override
                    public void handleLookup(Collection items) {
                        for (Object item : items) {
                            ProcVariable processVariable = (ProcVariable) ((DesignProcessVariable) item).copyTo(new ProcVariable());
                            processVariable.setProc(proc);
                            processVariable.setOverridden(true);
                            table.getDatasource().addItem(processVariable);
                        }
                        table.getDatasource().commit();
                    }
                }, WindowManager.OpenType.DIALOG, Collections.<String, Object>singletonMap("design", proc.getDesign()));
            }
        });
        if (proc.getDesign() == null) table.getAction("override").setEnabled(false);
    }

    @Override
    protected Map<String, Object> getInitialValuesForCreate() {
        return Collections.<String, Object>singletonMap("proc", proc);
    }
}
