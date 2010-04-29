/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 27.01.2010 12:28:21
 *
 * $Id$
 */
package com.haulmont.workflow.gui.data;

import com.haulmont.chile.core.common.ValueListener;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.data.DataService;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.AbstractDatasource;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Card;

import java.util.HashMap;
import java.util.Map;

public abstract class ProcessVariablesDatasource extends AbstractDatasource<ProcessVariablesEntity> {

    private DsContext dsContext;
    private State state = State.NOT_INITIALIZED;
    private ProcessVariablesEntity item;
    private MetaClass metaClass;
    private Card card;

    protected abstract Map<String, Class> getVariableTypes();

    public ProcessVariablesDatasource(DsContext dsContext, DataService dataservice,
                                      String id, MetaClass metaClass, String viewName)
    {
        super(id);
        this.dsContext = dsContext;

        this.metaClass = new ProcessVariablesMetaClass();
        initMetaClass();
    }

    protected void initMetaClass() {
        Map<String, Class> varTypes = getVariableTypes();
        for (Map.Entry<String, Class> entry : varTypes.entrySet()) {
            ProcessVariableMetaProperty property = new ProcessVariableMetaProperty(this.metaClass, entry.getKey(), entry.getValue());
            ((ProcessVariablesMetaClass) this.metaClass).addProperty(property);
        }
    }

    private Card getCard() {
        if (card == null) {
            if (dsContext.getWindowContext() == null)
                throw new IllegalStateException("WindowContext is null");

            Map<String, Object> params = dsContext.getWindowContext().getParams();
            card = (Card) params.get("param$card");
            if (card == null)
                throw new IllegalStateException("No Card instance in the form parameters");
        }
        return card;
    }

    public void initialized() {
        state = State.INVALID;
    }

    public void valid() {
        state = State.VALID;
    }

    public void commited(Map<Entity, Entity> map) {
    }

    public DsContext getDsContext() {
        return dsContext;
    }

    public DataService getDataService() {
        return null;
    }

    public void commit() {
        if (!item.getChanged().isEmpty()) {
            WfService service = ServiceLocator.lookup(WfService.NAME);
            service.setProcessVariables(getCard(), item.getChanged());
        }
        modified = false;
    }

    public State getState() {
        return state;
    }

    public ProcessVariablesEntity getItem() {
        if (State.VALID.equals(state))
            return item;
        else
            throw new IllegalStateException("Datasource state is " + state);
    }

    public void setItem(ProcessVariablesEntity item) {
        throw new UnsupportedOperationException();
    }

    public void invalidate() {
    }

    public void refresh() {
        Map<String, Object> variables = new HashMap<String, Object>();
        WfService service = ServiceLocator.lookup(WfService.NAME);
        Map<String, Object> map = service.getProcessVariables(getCard());
        variables.putAll(map);

        item = new ProcessVariablesEntity(getMetaClass(), variables);
        item.addListener(new ValueListener() {
            public void propertyChanged(Object item, String property, Object prevValue, Object value) {
                modified = true;
            }
        });
        state = State.VALID;
        modified = false;
    }

    public MetaClass getMetaClass() {
        return metaClass;
    }

    public View getView() {
        return null;
    }

}
