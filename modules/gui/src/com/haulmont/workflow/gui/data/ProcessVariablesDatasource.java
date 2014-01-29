/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.data;

import com.haulmont.chile.core.common.ValueListener;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.AbstractDatasource;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Card;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author krivopustov
 * @version $Id$
 */
public abstract class ProcessVariablesDatasource extends AbstractDatasource<ProcessVariablesEntity> {

    protected DsContext dsContext;
    protected State state = State.NOT_INITIALIZED;
    protected ProcessVariablesEntity item;
    protected MetaClass metaClass;
    protected Card card;

    protected abstract Map<String, Class> getVariableTypes();

    @Override
    public void setup(DsContext dsContext, DataSupplier dataSupplier, String id, MetaClass metaClass, @Nullable View view) {
        this.id = id;
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
            if (dsContext.getFrameContext() == null)
                throw new IllegalStateException("WindowContext is null");

            Map<String, Object> params = dsContext.getFrameContext().getParams();
            card = (Card) params.get("card");
            if (card == null)
                throw new IllegalStateException("No Card instance in the form parameters");
        }
        return card;
    }

    @Override
    public void initialized() {
        state = State.INVALID;
    }

    @Override
    public void valid() {
        state = State.VALID;
    }

    @Override
    public void committed(Set<Entity> entities) {
    }

    @Override
    public DsContext getDsContext() {
        return dsContext;
    }

    @Override
    public DataSupplier getDataSupplier() {
        return null;
    }

    @Override
    public void commit() {
        if (!allowCommit)
            return;

        Card c = getCard();
        Map<String, Object> vars = item.getChanged();
        if (!vars.isEmpty()) {
            if (c.getJbpmProcessId() == null) {
                if (c.getInitialProcessVariables() == null)
                    c.setInitialProcessVariables(new HashMap<>(vars));
                else
                    c.getInitialProcessVariables().putAll(vars);
            } else {
                WfService service = AppBeans.get(WfService.NAME);
                service.setProcessVariables(c, vars);
            }
        }
        modified = false;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public ProcessVariablesEntity getItem() {
        if (State.VALID.equals(state))
            return item;
        else
            throw new IllegalStateException("Datasource state is " + state);
    }

    @Override
    public void setItem(ProcessVariablesEntity item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invalidate() {
    }

    @Override
    public void refresh() {
        Map<String, Object> variables = new HashMap<>();

        if (getCard().getJbpmProcessId() != null) {
            WfService service = AppBeans.get(WfService.NAME);
            Map<String, Object> map = service.getProcessVariables(getCard());
            variables.putAll(map);
        }

        item = new ProcessVariablesEntity(getMetaClass(), variables);
        item.addListener(new ValueListener() {
            public void propertyChanged(Object item, String property, Object prevValue, Object value) {
                modified = true;
            }
        });
        state = State.VALID;
        fireItemChanged(null);
        modified = false;
    }

    @Override
    public MetaClass getMetaClass() {
        return metaClass;
    }

    @Override
    public View getView() {
        return null;
    }
}