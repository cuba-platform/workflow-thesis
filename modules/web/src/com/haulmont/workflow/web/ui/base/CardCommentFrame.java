/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Valery Novikov
 * Created: 05.08.2010 19:21:16
 *
 * $Id$
 */

package com.haulmont.workflow.web.ui.base;

import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.chile.core.datatypes.impl.DateTimeDatatype;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.CollectionDatasourceListener;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.gui.WebWindow;
import com.haulmont.cuba.web.gui.components.*;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardComment;
import com.vaadin.ui.*;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardCommentFrame extends AbstractWindow {

    protected HierarchicalDatasource commentDs;
    protected WidgetsTree treeComment;
    protected com.haulmont.cuba.gui.components.Button buttonCreate;
    protected Card card;
    protected String cardSend;
    protected Boolean justCreated;
    protected int maxWidthDigit = 70;
    protected int maxHeightDigit = 3;

    public CardCommentFrame(IFrame frame) {
        super(frame);
    }

    protected void init(Map<String, Object> params) {
        super.init(params);
        card = (Card) params.get("item");
        justCreated = (Boolean) params.get("justCreated");
        if (!PersistenceHelper.isNew(card)) {
            LoadContext ctx = new LoadContext(card.getClass()).setId(card.getId())
                    .setView(MetadataProvider.getViewRepository().getView(card.getClass(), "browse"));
            card = ServiceLocator.getDataService().load(ctx);
        }
        commentDs = getDsContext().get("commentDs");
        buttonCreate = (com.haulmont.cuba.gui.components.Button)getComponent("add");
        treeComment = (WidgetsTree)getComponent("treeComment");
        commentDs.refresh();
        commentDs.addListener(new CollectionDsListenerAdapter(){
            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation) {
                treeComment.expandTree();
            }
        });
        treeComment.expandTree();
        treeComment.setWidgetBuilder(new WebWidgetsTree.WidgetBuilder() {
            public Component build(HierarchicalDatasource datasource, Object itemId, boolean leaf){
                final CardComment cardComment = (CardComment) commentDs.getItem(itemId);
                WebVBoxLayout vLayout = new WebVBoxLayout();
                WebHBoxLayout hLayoutFrom = new WebHBoxLayout();
                WebLabel labelFrom = new WebLabel();
                labelFrom.setValue(getMessage("fromUser"));
                final WebButton buttonFrom = new WebButton();
                buttonFrom.setCaption(getUserNameLogin(cardComment.getSender()));
                buttonFrom.setStyleName("link");
                com.vaadin.ui.Button vButtonFrom = (com.vaadin.ui.Button)WebComponentsHelper.unwrap(buttonFrom);
                vButtonFrom.addListener(new com.vaadin.ui.Button.ClickListener() {
                    public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                        openUser(cardComment.getSender(), buttonFrom);
                    }
                });
                WebLabel dateValueLabel = new WebLabel();
                Datatype<Date> datatype = Datatypes.get(DateTimeDatatype.NAME);
                dateValueLabel.setValue(" (" + datatype.format(cardComment.getCreateTs(), UserSessionProvider.getLocale()) + ") ");
                hLayoutFrom.add(labelFrom);
                hLayoutFrom.add(buttonFrom);
                hLayoutFrom.add(dateValueLabel);
                hLayoutFrom.setSpacing(true);
 	            ((com.vaadin.ui.Component)WebComponentsHelper.unwrap(hLayoutFrom)).addStyleName("minsize");

                WebHBoxLayout hLayoutTo = new WebHBoxLayout();
                WebLabel labelTo = new WebLabel();
                labelTo.setValue(getMessage("toUser"));
                hLayoutTo.add(labelTo);
                List<User> addressees = cardComment.getAddressees();
                if (addressees != null) {
                    for (final User u : addressees) {
                        final WebButton buttonTo = new WebButton();
                        buttonTo.setCaption(getUserNameLogin(u));
                        buttonTo.setStyleName("link");
                        com.vaadin.ui.Button vButtonTo = (com.vaadin.ui.Button) WebComponentsHelper.unwrap(buttonTo);
                        vButtonTo.addListener(new com.vaadin.ui.Button.ClickListener() {
                            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                                openUser(u, buttonTo);
                            }
                        });
                        hLayoutTo.add(buttonTo);
                    }
                }
                hLayoutTo.setSpacing(true);
                ((com.vaadin.ui.Component)WebComponentsHelper.unwrap(hLayoutTo)).addStyleName("minsize");

                WebHBoxLayout hLayoutComment = new WebHBoxLayout();
                WebTextField labelComment = new WebTextField();
                labelComment.setValue(cardComment.getComment());
                final WebButton buttonComment = new WebButton();
                buttonComment.setCaption(getMessage("answer"));
                com.vaadin.ui.Button vButtoncomment = (com.vaadin.ui.Button) WebComponentsHelper.unwrap(buttonComment);
                vButtoncomment.addListener(new com.vaadin.ui.Button.ClickListener() {
                    public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                        if (card != null) {
                            Map paramsCard = new HashMap();
                            paramsCard.put("item", card);
                            paramsCard.put("parent", cardComment);
                            Window window = openWindow(card.getMetaClass().getName()+".send", WindowManager.OpenType.DIALOG, paramsCard);
                            window.addListener(new CloseListener() {
                                public void windowClosed(String actionId) {
                                    commentDs.refresh();
                                }
                            });
                        }
                    }
                });
                labelComment.setRows(3);
                labelComment.setEditable(false);
                labelComment.setStyleName("noborder");
                buttonComment.setAlignment(Alignment.MIDDLE_RIGHT);
                hLayoutComment.add(labelComment);
                hLayoutComment.add(buttonComment);
                hLayoutComment.setSpacing(true);
                hLayoutComment.setWidth("100%");
                labelComment.setWidth("100%");
                hLayoutComment.setExpandRatio(WebComponentsHelper.unwrap(labelComment),1.0f);

                String descr = cardComment.getComment();
                String[] parts = descr.split("\n");
                String preview = "<span>";
                boolean isCroped = (parts.length > maxHeightDigit ? true : false);
                for (int i=0; i<Math.min(parts.length, maxHeightDigit); i++) {
                    String part = parts[i];
                    if (part.length() > maxWidthDigit) {
                        preview += part.substring(0, maxWidthDigit) + "...</br>";
                        isCroped = true;
                    }
                    else
                        preview += part + "</br>";
                }
                preview += "</span>";

                if (isCroped) {
                    com.vaadin.ui.TextField content = new com.vaadin.ui.TextField(null, descr);
                    content.setReadOnly(true);
                    content.setWidth("500px");
                    content.setHeight("300px");
                    com.vaadin.ui.Component component = new com.vaadin.ui.PopupView(preview, content);
                    component.setStyleName("longtext");
                    hLayoutComment.replaceComponent((com.vaadin.ui.TextField) WebComponentsHelper.unwrap(labelComment), component);
                    component.setWidth("100%");
                    hLayoutComment.setExpandRatio(component, 1.0f);
                }
                vLayout.add(hLayoutFrom);
                vLayout.add(hLayoutTo);
                vLayout.add(hLayoutComment);
                return vLayout;
            }
        });

        buttonCreate.setAction(new AbstractAction("add") {
            public void actionPerform(com.haulmont.cuba.gui.components.Component component) {
                if (card != null) {
                    if (PersistenceHelper.isNew(card) || (justCreated != null)) {
                        showOptionDialog(
                                getMessage("cardComment.dialogHeader"),
                                getMessage("cardComment.dialogMessage"),
                                IFrame.MessageType.CONFIRMATION,
                                new Action[]{
                                        new DialogAction(DialogAction.Type.YES) {
                                            @Override
                                            public void actionPerform(Component component) {
                                                boolean isCommited = true;
                                                if (getFrame() instanceof WebWindow.Editor)
                                                    isCommited = ((AbstractEditor)((WebWindow.Editor) getFrame()).getWrapper()).commit(true);
                                                else
                                                    getFrame().getDsContext().commit();
                                                if (isCommited)
                                                    openCardSend(card);
                                            }
                                        },
                                        new DialogAction(DialogAction.Type.NO)
                                }
                        );
                    } else {
                        openCardSend(card);
                    }
                }
            }
        });
    }

    protected void openCardSend(Card card) {
        Map paramsCard = new HashMap();

        CollectionDatasource cardRolesDs;
        CardProcFrame cardProcFrame = (CardProcFrame) getComponent("cardProcFrame");
        if (cardProcFrame != null) {
            cardRolesDs = cardProcFrame.getCardRolesFrame().getDsContext().get("tmpCardRolesDs");
        } else {
            cardRolesDs = ((IFrame) getComponent("cardRolesFrame")).getDsContext().get("tmpCardRolesDs");
        }
        paramsCard.put("cardRolesDs", cardRolesDs);
        paramsCard.put("item", card);
        paramsCard.put("rootFrame", this.<IFrame>getFrame());
        Window window = openWindow(card.getMetaClass().getName() + ".send", WindowManager.OpenType.DIALOG, paramsCard);
        window.addListener(new CloseListener() {
            public void windowClosed(String actionId) {
                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                    commentDs.refresh();
                }
            }
        });
    }

    protected String getUserNameLogin(User user) {
        if (user != null)
            return StringUtils.isBlank(user.getName()) ? user.getLogin() : user.getName();
        else
            return "";
    }

    protected void openUser(final User user, final WebButton button) {
        Window window = openEditor("sec$User.edit", user, WindowManager.OpenType.THIS_TAB);
        window.addListener(new CloseListener() {
            public void windowClosed(String actionId) {
                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                    LoadContext ctx = new LoadContext(user.getClass()).setId(user.getId()).setView("_local");
                    User reloadUser = ServiceLocator.getDataService().load(ctx);
                    button.setCaption(getUserNameLogin(reloadUser));
                }
            }
        });
    }
}