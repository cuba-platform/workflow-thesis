/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.gui.app.designprocessvariables.edit;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.workflow.core.entity.DesignProcessVariable;

import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class DesignProcessVariableEditor extends AbstractProcVariableEditor {

    public DesignProcessVariableEditor() {
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
        DesignProcessVariable designProcessVariable = (DesignProcessVariable) processVariable;
        LoadContext loadContext = new LoadContext(DesignProcessVariable.class);
        loadContext.setQueryString("select dpv from wf$DesignProcessVariable dpv where dpv.design.id = :design and dpv.alias = :alias and dpv.id <> :id");
        loadContext.getQuery().setParameter("design", designProcessVariable.getDesign());
        loadContext.getQuery().setParameter("alias", designProcessVariable.getAlias());
        loadContext.getQuery().setParameter("id", designProcessVariable.getId());
        List<DesignProcessVariable> variableList = getDsContext().getDataSupplier().loadList(loadContext);
        if (variableList.size() > 0) {
            showNotification(getMessage("parameterWithSameAliasAlreadyExists"), Frame.NotificationType.ERROR);
            return false;
        }
        return true;
    }
}
