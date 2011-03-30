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
                hLayoutFrom.add(labelFrom);
                hLayoutFrom.add(buttonFrom);
                hLayoutFrom.setSpacing(true);
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
                WebHBoxLayout dateLayout = new WebHBoxLayout();
                dateLayout.setSpacing(true);
                WebLabel dateLabel = new WebLabel();
                dateLabel.setValue(getMessage("date"));
                dateLayout.add(dateLabel);
                WebLabel dateValueLabel = new WebLabel();
                dateValueLabel.setValue(new SimpleDateFormat(MessageUtils.getDateTimeFormat()).format(cardComment.getCreateTs()));
                dateLayout.add(dateValueLabel);
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
                buttonComment.setAlignment(Alignment.MIDDLE_RIGHT);
                hLayoutComment.add(labelComment);
                hLayoutComment.add(buttonComment);
                hLayoutComment.setSpacing(true);
                hLayoutComment.setWidth("100%");
                labelComment.setWidth("100%");
                hLayoutComment.setExpandRatio(WebComponentsHelper.unwrap(labelComment),1.0f);                
                vLayout.add(hLayoutFrom);
                vLayout.add(hLayoutTo);
                vLayout.add(dateLayout);
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