/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 14.01.11 18:02
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.cuba.web.gui.components.WebHBoxLayout;
import com.haulmont.workflow.core.entity.CardStage;
import com.haulmont.workflow.core.entity.ProcStageType;
import com.vaadin.ui.HorizontalLayout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CardStagesFrame extends AbstractFrame {
    private static final String DEFAULT_TYPE = "1";
    private CollectionDatasource stagesDs;
    private LookupField type;
    private Button applyType;
    private Table stagesTable;
    private WebHBoxLayout typeFilter;

    public CardStagesFrame(IFrame frame) {
        super(frame);
    }

    public void init() {
        init(DEFAULT_TYPE);
    }

    public void init(String typeCode) {
        stagesDs = getDsContext().get("stagesDs");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", typeCode);
        stagesDs.refresh(params);

        stagesTable = getComponent("stagesTable");
        typeFilter = getComponent("typeFilter");
        type = getComponent("type");
        applyType = getComponent("applyType");
        applyType.setAction(new AbstractAction("applyType") {

            public void actionPerform(Component component) {
                Map<String, Object> params = new HashMap<String, Object>();

                ProcStageType typeValue = type.<ProcStageType>getValue();
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

            public String getStyle(Object itemId, Object propertyId) {
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

