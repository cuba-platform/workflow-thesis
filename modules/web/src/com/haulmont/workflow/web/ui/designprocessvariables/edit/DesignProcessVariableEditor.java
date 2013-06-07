/**
 *
 * <p>$Id: DesignProcessVariableEditor.java 10533 2013-02-12 08:55:55Z zaharchenko $</p>
 *
 * @author Zaharchenko
 */
package com.haulmont.workflow.web.ui.designprocessvariables.edit;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.workflow.core.entity.DesignProcessVariable;

import java.util.List;
import java.util.Map;


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
        loadContext.getQuery().addParameter("design", designProcessVariable.getDesign());
        loadContext.getQuery().addParameter("alias", designProcessVariable.getAlias());
        loadContext.getQuery().addParameter("id", designProcessVariable.getId());
        List<DesignProcessVariable> variableList = getDsContext().getDataSupplier().loadList(loadContext);
        if (variableList.size() > 0) {
            showNotification(getMessage("parameterWithSameAliasAlreadyExists"), IFrame.NotificationType.ERROR);
            return false;
        }
        return true;
    }
}
