/**
 *
 * <p>$Id: ProcVariableEditor.java 10533 2013-02-12 08:55:55Z zaharchenko $</p>
 *
 * @author Zaharchenko
 */
package com.haulmont.workflow.web.ui.procvariable.edit;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.workflow.core.entity.DesignProcessVariable;
import com.haulmont.workflow.core.entity.ProcVariable;
import com.haulmont.workflow.web.ui.designprocessvariables.edit.AbstractProcVariableEditor;

import java.util.List;
import java.util.Map;


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
        loadContext.getQuery().addParameter("proc", procVariable.getProc());
        loadContext.getQuery().addParameter("alias", procVariable.getAlias());
        loadContext.getQuery().addParameter("id", procVariable.getId());
        List<DesignProcessVariable> variableList = getDsContext().getDataSupplier().loadList(loadContext);
        if (variableList.size() > 0) {
            showNotification(getMessage("parameterWithSameAliasAlreadyExists"), IFrame.NotificationType.ERROR);
            return false;
        }
        return true;
    }
}
