/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.cuba.web.gui.components.WebHBoxLayout;
import com.haulmont.workflow.core.entity.CardStage;
import com.haulmont.workflow.core.entity.ProcStageType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class CardStagesFrame extends AbstractFrame {
    protected static final String DEFAULT_TYPE = "1";
    protected CollectionDatasource stagesDs;
    protected LookupField type;
    protected Table stagesTable;
    protected WebHBoxLayout typeFilter;

    public void init() {
        init(DEFAULT_TYPE);
    }

    public void init(String typeCode) {
        stagesDs = getDsContext().get("stagesDs");
        Map<String, Object> params = new HashMap<>();
        params.put("type", typeCode);
        stagesDs.refresh(params);

        stagesTable = getComponent("stagesTable");
        typeFilter = getComponent("typeFilter");
        type = getComponent("type");
        Button applyType = getComponent("applyType");
        applyType.setAction(new AbstractAction("applyType") {

            public void actionPerform(Component component) {
                Map<String, Object> params = new HashMap<>();

                ProcStageType typeValue = type.getValue();
                String code = typeValue == null ? null : typeValue.getCode();
                params.put("type", code);
                stagesDs.refresh(params);
            }

            @Override
            public String getCaption() {
                return getMessage("apply");
            }
        });

        com.vaadin.ui.Table vTable = (com.vaadin.ui.Table) WebComponentsHelper.unwrap(stagesTable);
        vTable.setCellStyleGenerator(new com.vaadin.ui.Table.CellStyleGenerator() {

            @Override
            public String getStyle(com.vaadin.ui.Table source, Object itemId, Object propertyId) {
                if (propertyId == null) {
                    if ((itemId instanceof UUID)) {
                        CardStage cardStage = (CardStage) stagesTable.getDatasource().getItem(itemId);
                        if ((cardStage.getEndDateFact() != null && (cardStage.getEndDateFact().after(cardStage.getEndDatePlan()))
                                || (cardStage.getEndDateFact() == null && cardStage.getEndDatePlan().before(TimeProvider.currentTimestamp())))) {
                            return "overdue";
                        }
                    }
                }
                return "";
            }
        });
    }

    public void setTypeFilterVisible(boolean visible) {
        typeFilter.setVisible(visible);
    }
}