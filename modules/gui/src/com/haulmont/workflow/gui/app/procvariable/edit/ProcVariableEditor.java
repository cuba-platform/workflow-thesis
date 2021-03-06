/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.app.procvariable.edit;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.workflow.core.entity.DesignProcessVariable;
import com.haulmont.workflow.core.entity.ProcVariable;
import com.haulmont.workflow.gui.app.designprocessvariables.edit.AbstractProcVariableEditor;

import java.util.List;
import java.util.Map;

/**
 *
 * <p>$Id: ProcVariableEditor.java 10533 2013-02-12 08:55:55Z zaharchenko $</p>
 *
 * @author Zaharchenko
 */
public class ProcVariableEditor extends AbstractProcVariableEditor {

    public ProcVariableEditor() {
        super();
    }

    @Override
    public void setItem(Entity item) {
        super.setItem(item);
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
    }

    @Override
    protected boolean checkProcessValue() {
        ProcVariable procVariable = (ProcVariable) processVariable;
        LoadContext loadContext = new LoadContext(ProcVariable.class);
        loadContext.setQueryString("select dpv from wf$ProcVariable dpv where dpv.proc.id = :proc and dpv.alias = :alias and dpv.id <> :id");
        loadContext.getQuery().setParameter("proc", procVariable.getProc());
        loadContext.getQuery().setParameter("alias", procVariable.getAlias());
        loadContext.getQuery().setParameter("id", procVariable.getId());
        List<DesignProcessVariable> variableList = getDsContext().getDataSupplier().loadList(loadContext);
        if (variableList.size() > 0) {
            showNotification(getMessage("parameterWithSameAliasAlreadyExists"), IFrame.NotificationType.ERROR);
            return false;
        }
        return true;
    }
}
