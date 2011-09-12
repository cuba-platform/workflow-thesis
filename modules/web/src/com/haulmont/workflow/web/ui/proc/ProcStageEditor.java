/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 08.12.2010 11:48:34
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.proc;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.entity.ProcStage;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProcStageEditor extends AbstractEditor {
    private ProcStage stage;
    private LookupField startActivity;
    private LookupField endActivity;
    private LookupField type;
    private Table procRolesTable;
    private CollectionDatasource procRolesDs;
    private Datasource stageDs;
    private TextField durationScript;

    public ProcStageEditor(IFrame frame) {
        super(frame);
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        startActivity = getComponent("startActivity");
        endActivity = getComponent("endActivity");
        type = getComponent("type");
        procRolesTable = getComponent("procRolesTable");
        durationScript = getComponent("durationScript");
        procRolesDs = getDsContext().get("procRolesDs");
        stageDs = getDsContext().get("stageDs");


        TableActionsHelper procRolesHelper = new TableActionsHelper(this, procRolesTable);
        Map<String, Object> procRoleLookupParams = new HashMap<String, Object>();
        procRoleLookupParams.put("proc", ((ProcStage) params.get("param$item")).getProc());
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
        Map<String, Object> statesMap = new HashMap<String, Object>();
        Proc proc = stage.getProc();

        String states = proc.getStates();
        if (StringUtils.isNotBlank(states)) {
            for (String state : states.split("\\s*,\\s*")) {
                String locState = MessageProvider.getMessage(proc.getMessagesPack(), state);
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
