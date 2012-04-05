/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 02.12.2009 10:11:47
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.app.LinkColumnHelper;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

public class ResolutionsFrame extends AbstractFrame {
    private Table table;
    private CollectionDatasource<Assignment, UUID> resolutionsDs;
    private static final long serialVersionUID = 3993679675921614604L;

    public ResolutionsFrame(IFrame frame) {
        super(frame);
    }

    public void init() {
        table = getComponent("resolutionsTable");
        resolutionsDs = getDsContext().get("resolutionsDs");

        com.vaadin.ui.Table vTable = (com.vaadin.ui.Table) WebComponentsHelper.unwrap(table);
        vTable.setPageLength(5);
        vTable.addGeneratedColumn(resolutionsDs.getMetaClass().getPropertyPath("comment"),
                new com.vaadin.ui.Table.ColumnGenerator() {
                    @Override
                    public com.vaadin.ui.Component generateCell(com.vaadin.ui.Table source, Object itemId, Object columnId) {
                        com.vaadin.ui.Component component;
                        Assignment assignment = resolutionsDs.getItem((UUID) itemId);
                        String comment = assignment.getComment();
                        int enterIdx = comment != null ? (comment.length() > 50 ? 50 : comment.indexOf('\n')) : -1;
                        if (enterIdx != -1) {
                            com.vaadin.ui.TextField content = new com.vaadin.ui.TextField(null, comment);
                            content.setReadOnly(true);
                            content.setWidth("300px");
                            content.setHeight("300px");
                            component = new com.vaadin.ui.PopupView("<span>" + comment.substring(0, enterIdx) + "...</span>", content);
                            component.addStyleName("longtext");
                        } else {
                            component = new com.vaadin.ui.Label(comment == null ? "" : comment);
                        }
                        component.setWidth("-1px");
                        return component;
                    }
                });

//       vTable.setAllowMultiStringCells(true);

        vTable.setCellStyleGenerator(new com.vaadin.ui.Table.CellStyleGenerator() {
            public String getStyle(Object itemId, Object propertyId) {
                if (propertyId == null) {
                    if (!(itemId instanceof UUID))
                        return "";
                    Assignment assignment = resolutionsDs.getItem((UUID) itemId);
                    if ((assignment.getDueDate() != null) &&
                            (((assignment.getFinished() == null && equalsDateBeforeDate(assignment.getDueDate(), TimeProvider.currentTimestamp())))
                                    || (assignment.getFinished() != null && equalsDateBeforeDate(assignment.getDueDate(), assignment.getFinished())))) {
                        return "overdue";
                    }
                    if (assignment.getFinished() == null)
                        return "taskremind";
                    else
                        return "";
                }
                return "";
            }

        });

        vTable.addGeneratedColumn(resolutionsDs.getMetaClass().getPropertyEx("locOutcomeResult"),
                new com.vaadin.ui.Table.ColumnGenerator() {
                    private static final long serialVersionUID = 5218754905457505133L;

                    public com.vaadin.ui.Component generateCell(com.vaadin.ui.Table table, Object itemId, Object columnId) {
                        final Assignment assignment = resolutionsDs.getItem((UUID) itemId);
                        String state = assignment.getLocOutcome();
                        if (state != null && StringUtils.contains(state, ".Saved")) {
                            final com.vaadin.ui.Button vButton = new com.vaadin.ui.Button(assignment.getLocOutcomeResult());
                            vButton.setStyleName("link");
                            vButton.addListener(new com.vaadin.ui.Button.ClickListener() {
                                private static final long serialVersionUID = 8866649082801744357L;

                                public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                                    WindowInfo windowInfo = AppContext.getBean(WindowConfig.class).getWindowInfo("task.log.dialog");
                                    App.getInstance().getWindowManager().openWindow(windowInfo, WindowManager.OpenType.DIALOG,
                                            Collections.<String, Object>singletonMap("item", assignment));
                                }
                            });
                            return vButton;
                        } else
                            return new com.vaadin.ui.Label(assignment.getLocOutcomeResult());
                    }
                });

        table.addAction(new AbstractAction("openResolution") {
            public void actionPerform(Component component) {
                openResolution(table.getDatasource().getItem());
            }
        });

        LinkColumnHelper.initColumn(table, "createTs", new LinkColumnHelper.Handler() {
            private static final long serialVersionUID = -6702999877103154970L;

            public void onClick(Entity entity) {
                openResolution(entity);
            }
        });

        final CollectionDatasource ds = table.getDatasource();
//        vTable.removeGeneratedColumn(ds.getMetaClass().getPropertyEx("hasAttachments"));
//        vTable.addGeneratedColumn(ds.getMetaClass().getPropertyEx("hasAttachments"),
//                new com.vaadin.ui.Table.ColumnGenerator() {
//                    private static final long serialVersionUID = -9060082478585490858L;
//
//                    public com.vaadin.ui.Component generateCell(com.vaadin.ui.Table table, Object itemId, Object columnId) {
//                        Assignment assignment = (Assignment) ds.getItem(itemId);
//                        java.util.List<AssignmentAttachment> attachmentList = assignment.getAttachments();
//
//                        if (attachmentList != null && !attachmentList.isEmpty()) {
//                            VerticalLayout layout = new VerticalLayout();
//
//                            for (AssignmentAttachment assignmentAttachment : attachmentList) {
//                                final FileDescriptor fd = assignmentAttachment.getFile();
//                                if (fd != null) {
//                                    com.vaadin.ui.Component component;
//                                    component = new com.vaadin.ui.Button(fd.getName(),
//                                            new com.vaadin.ui.Button.ClickListener() {
//                                                private static final long serialVersionUID = -4083361053049572380L;
//
//                                                public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
//                                                    FileDisplay fileDisplay = new FileDisplay(true);
//                                                    fileDisplay.show(fd.getName(), fd, false);
//                                                }
//                                            });
//                                    component.setStyleName("link");
//                                    layout.addComponent(component);
//                                }
//                            }
//
//                            if (layout.getComponentIterator().hasNext()) {
//                                return layout;
//                            }
//                        }
//
//                        return new com.vaadin.ui.Label();
//                    }
//                });
    }

    private void openResolution(Entity entity) {
        final Window window = openEditor("wf$Assignment.edit", entity, WindowManager.OpenType.DIALOG);

        window.addListener(new Window.CloseListener() {
            public void windowClosed(String actionId) {
                if (Window.COMMIT_ACTION_ID.equals(actionId) && window instanceof Window.Editor) {
                    Object item = ((Window.Editor) window).getItem();
                    if (item instanceof Entity) {
                        table.getDatasource().updateItem((Entity) item);
                    }
                }
            }
        });
    }

    public void setCard(final Card card) {
//        Preconditions.checkArgument(card != null, "Card is null");

        CollectionDatasource<Assignment, UUID> ds = getDsContext().get("resolutionsDs");
        ds.refresh(Collections.<String, Object>singletonMap("cardId", card != null ? card.getId() : null));
    }

    public void refreshDs() {
        getDsContext().get("resolutionsDs").refresh();
    }

    private boolean equalsDateBeforeDate(Date fromDate, Date toDate) {
        return fromDate != null && toDate != null
                && DateUtils.truncate(fromDate, Calendar.MINUTE).before(DateUtils.truncate(toDate, Calendar.MINUTE));
    }
}