/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.folders;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.TwinColumn;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.web.gui.components.WebButton;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.cuba.web.gui.components.WebLookupField;
import com.haulmont.cuba.web.gui.components.WebTwinColumn;
import com.haulmont.cuba.web.toolkit.VersionedThemeResource;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.entity.ProcCondition;
import com.haulmont.workflow.core.entity.ProcState;
import com.vaadin.ui.*;
import org.apache.commons.lang.BooleanUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * @author pavlov
 * @version $Id$
 */
public class ProcConditionEditWindow extends Window {

    protected CollectionDatasource<ProcCondition, UUID> procConditionDatasource;

    public ProcConditionEditWindow(CollectionDatasource<ProcCondition, UUID> procConditionDatasource) {
        super(AppBeans.get(Messages.class).getMessage(ProcConditionEditWindow.class, "ProcCondition"));

        this.procConditionDatasource = procConditionDatasource;

        VerticalLayout procConditionRootLayout = new VerticalLayout();
        procConditionRootLayout.setSpacing(true);
        setContent(procConditionRootLayout);

        GridLayout grid = new GridLayout(2, 3);
        grid.setMargin(true);
        grid.setSpacing(true);
        procConditionRootLayout.addComponent(grid);

        Label label = new Label(getMessage("proc"));
        grid.addComponent(label);

        final LookupField procField = new WebLookupField();
        procField.setWidth("300px");
        grid.addComponent(WebComponentsHelper.unwrap(procField));

        CollectionDatasource<Proc, UUID> procDs = new DsBuilder()
                .setMetaClass(AppBeans.get(Metadata.class).getClass("wf$Proc"))
                .setViewName(View.LOCAL)
                .buildCollectionDatasource();

        procDs.refresh();

        procField.setOptionsDatasource(procDs);

        Label inExprLabel = new Label(getMessage("inExpr"));
        grid.addComponent(inExprLabel);

        final CheckBox inExprField = new CheckBox();
        grid.addComponent(inExprField);
        inExprField.setImmediate(true);
        inExprField.setValue(true);


        Label emptyLabel = new Label(getMessage("states"));
        grid.addComponent(emptyLabel);

        final TwinColumn statesField = new WebTwinColumn();
        statesField.setWidth("500px");
        statesField.setHeight("208px");
        grid.addComponent(WebComponentsHelper.unwrap(statesField));

        statesField.setOptionsDatasource(getStatesList(null));
        statesField.setCaptionProperty("locName");

        procDs.addListener(new CollectionDsListenerAdapter<Proc>() {
            @Override
            public void itemChanged(Datasource<Proc> ds, Proc prevItem, Proc item) {
                statesField.setOptionsDatasource(getStatesList(item));
                statesField.setValue(null);
            }
        });

        HorizontalLayout btnPane = new HorizontalLayout();
        btnPane.setSpacing(true);
        procConditionRootLayout.addComponent(btnPane);

        Button okBtn = new Button(getMessage("actions.Ok"));
        okBtn.setIcon(new VersionedThemeResource("icons/ok.png"));
        okBtn.addStyleName(WebButton.ICON_STYLE);
        btnPane.addComponent(okBtn);
        okBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ProcConditionEditWindow.this.close();
                ProcCondition procCondition = new ProcCondition();
                Proc proc = procField.getValue();
                procCondition.setProc(proc);
                procCondition.setInExpr(BooleanUtils.isTrue(inExprField.getValue()));
                Collection<ProcState> states = statesField.getValue();
                procCondition.setStates(states);
                if (!states.isEmpty() || proc != null) {
                    ProcConditionEditWindow.this.procConditionDatasource.addItem(procCondition);
                }
            }
        });

        Button cancelBtn = new Button(getMessage("actions.Cancel"));
        cancelBtn.setIcon(new VersionedThemeResource("icons/cancel.png"));
        cancelBtn.addStyleName(WebButton.ICON_STYLE);
        btnPane.addComponent(cancelBtn);
        cancelBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ProcConditionEditWindow.this.close();
            }
        });
    }

    protected CollectionDatasource getStatesList(Proc proc) {
        CollectionDatasource<ProcState, UUID> procStateDs = new DsBuilder()
                .setMetaClass(AppBeans.get(Metadata.class).getClass("wf$ProcState"))
                .setViewName("browse")
                .buildCollectionDatasource();

        procStateDs.setQuery("select e from wf$ProcState e where e.proc.id = :custom$proc or :custom$proc is null");
        procStateDs.refresh(Collections.<String, Object>singletonMap("proc", proc));

        return procStateDs;
    }

    protected String getMessage(String key) {
        return AppBeans.get(Messages.class).getMessage(this.getClass(), key);
    }
}