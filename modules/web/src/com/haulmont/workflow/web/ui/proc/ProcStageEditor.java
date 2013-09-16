/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.ui.proc;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.entity.ProcStage;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class ProcStageEditor extends AbstractEditor {

    private ProcStage stage;
    private LookupField startActivity;
    private LookupField endActivity;
    private LookupField type;
    private Table procRolesTable;
    private CollectionDatasource procRolesDs;
    private Datasource stageDs;

    @Inject
    protected TextArea durationScript;

    @Inject
    protected Messages messages;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        startActivity = getComponent("startActivity");
        endActivity = getComponent("endActivity");
        type = getComponent("type");
        procRolesTable = getComponent("procRolesTable");
        procRolesDs = getDsContext().get("procRolesDs");
        stageDs = getDsContext().get("stageDs");

        TableActionsHelper procRolesHelper = new TableActionsHelper(this, procRolesTable);
        Map<String, Object> procRoleLookupParams = new HashMap<>();
        procRoleLookupParams.put("proc", ((ProcStage) WindowParams.ITEM.getEntity(params)).getProc());
        procRolesHelper.createAddAction(new Lookup.Handler() {

            public void handleLookup(Collection items) {
                if ((items != null) && !items.isEmpty()) {
                    for (Object item : items) {
                        if (!procRolesDs.containsItem(item))
                            procRolesDs.addItem((Entity) item);
                    }
                }
            }
        }, procRoleLookupParams, "wf$ProcRole.lookup");

        procRolesHelper.createRemoveAction(false);

        stageDs.addListener(new DsListenerAdapter() {
            @Override
            public void valueChanged(Entity source, String property, Object prevValue, Object value) {
                if ("durationScriptEnabled".equals(property)) {
                    durationScript.setEnabled((Boolean) value);
                }
            }
        });
    }

    private void fillActivityLookups() {
        Map<String, Object> statesMap = new HashMap<>();
        Proc proc = stage.getProc();

        String states = proc.getStates();
        if (StringUtils.isNotBlank(states)) {
            for (String state : states.split("\\s*,\\s*")) {
                String locState = messages.getMessage(proc.getMessagesPack(), state);
                statesMap.put(locState, state);
            }
        }
        startActivity.setOptionsMap(statesMap);
        startActivity.setValue(stage.getStartActivity());

        endActivity.setOptionsMap(statesMap);
        endActivity.setValue(stage.getEndActivity());
    }

    @Override
    public void setItem(Entity item) {
        super.setItem(item);
        stage = (ProcStage)getItem();
        durationScript.setEnabled(stage.getDurationScriptEnabled());
        fillActivityLookups();
        com.vaadin.ui.Table vProcRolesTable = (com.vaadin.ui.Table) WebComponentsHelper.unwrap(procRolesTable);
        vProcRolesTable.setColumnCollapsingAllowed(false);
    }

    @Override
    public void commitAndClose() {
        stage.setStartActivity(startActivity.<String>getValue());
        stage.setEndActivity(endActivity.<String>getValue());
        super.commitAndClose();
    }
}