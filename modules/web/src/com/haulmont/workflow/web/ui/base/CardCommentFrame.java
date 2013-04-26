/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.base;

import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.chile.core.datatypes.impl.DateTimeDatatype;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.global.ViewRepository;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.gui.WebWindow;
import com.haulmont.cuba.web.gui.components.*;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardComment;
import com.vaadin.ui.AbstractOrderedLayout;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author novikov
 * @version $Id$
 */
public class CardCommentFrame extends AbstractWindow {

    @Inject
    protected HierarchicalDatasource commentDs;

    @Inject
    protected WidgetsTree treeComment;

    protected com.haulmont.cuba.gui.components.Button buttonCreate;
    protected Card card;
    protected String cardSend;
    protected Boolean justCreated;
    protected int maxWidthDigit = 70;
    protected int maxHeightDigit = 3;

    @Inject
    protected DataService dataService;

    @Inject
    protected ViewRepository viewRepository;

    @Inject
    protected UserSessionSource userSessionSource;

    public void init(Map<String, Object> params) {
        super.init(params);

        card = (Card) params.get("item");
        justCreated = (Boolean) params.get("justCreated");
        if (!PersistenceHelper.isNew(card)) {
            LoadContext ctx = new LoadContext(card.getClass()).setId(card.getId())
                    .setView(viewRepository.getView(card.getClass(), "browse"));
            card = dataService.load(ctx);
        }

        buttonCreate = getComponent("add");
        commentDs.refresh();
        commentDs.addListener(new CollectionDsListenerAdapter<Entity>() {
            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation, List<Entity> items) {
                treeComment.expandTree();
            }
        });

        treeComment.expandTree();
        treeComment.setWidgetBuilder(new WebWidgetsTree.WidgetBuilder() {
            @Override
            public Component build(HierarchicalDatasource datasource, Object itemId, boolean leaf) {
                final CardComment cardComment = (CardComment) commentDs.getItem(itemId);
                WebVBoxLayout vLayout = new WebVBoxLayout();
                WebHBoxLayout hLayoutFrom = new WebHBoxLayout();
                WebLabel labelFrom = new WebLabel();
                labelFrom.setValue(getMessage("fromUser"));
                WebLabel labelSender = new WebLabel();
                labelSender.setValue(getUserNameLogin(cardComment.getSender()));
                WebLabel dateValueLabel = new WebLabel();
                Datatype<Date> datatype = Datatypes.get(DateTimeDatatype.NAME);
                dateValueLabel.setValue(" (" + datatype.format(cardComment.getCreateTs(), userSessionSource.getLocale()) + ") ");
                hLayoutFrom.add(labelFrom);
                hLayoutFrom.add(labelSender);
                hLayoutFrom.add(dateValueLabel);
                hLayoutFrom.setSpacing(true);
                WebComponentsHelper.unwrap(hLayoutFrom).addStyleName("minsize");

                WebHBoxLayout hLayoutTo = new WebHBoxLayout();
                WebLabel labelTo = new WebLabel();
                labelTo.setValue(getMessage("toUser"));
                hLayoutTo.add(labelTo);
                List<User> addressees = cardComment.getAddressees();
                if (addressees != null) {
                    for (final User u : addressees) {
                        WebLabel labelUserTo = new WebLabel();
                        labelUserTo.setValue(getUserNameLogin(u));
                        hLayoutTo.add(labelUserTo);
                    }
                }
                hLayoutTo.setSpacing(true);
                WebComponentsHelper.unwrap(hLayoutTo).addStyleName("minsize");

                WebHBoxLayout hLayoutComment = new WebHBoxLayout();
                AbstractOrderedLayout vhLayoutComment = (AbstractOrderedLayout) WebComponentsHelper.unwrap(hLayoutComment);

                WebTextField labelComment = new WebTextField();
                labelComment.setValue(cardComment.getComment());
                final WebButton buttonComment = new WebButton();
                buttonComment.setCaption(getMessage("answer"));
                com.vaadin.ui.Button vButtoncomment = (com.vaadin.ui.Button) WebComponentsHelper.unwrap(buttonComment);
                vButtoncomment.addClickListener(new com.vaadin.ui.Button.ClickListener() {
                    @Override
                    public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                        if (card != null) {
                            Map<String, Object> paramsCard = new HashMap<>();
                            paramsCard.put("item", card);
                            paramsCard.put("parent", cardComment);
                            Window window = openWindow(card.getMetaClass().getName() + ".send",
                                    WindowManager.OpenType.DIALOG, paramsCard);
                            window.addListener(new CloseListener() {
                                @Override
                                public void windowClosed(String actionId) {
                                    commentDs.refresh();
                                }
                            });
                        }
                    }
                });
                labelComment.setHeight("100px");
                labelComment.setEditable(false);
                labelComment.setStyleName("noborder");
                buttonComment.setAlignment(Alignment.MIDDLE_RIGHT);
                hLayoutComment.add(labelComment);
                hLayoutComment.add(buttonComment);
                WebLabel spaceLabel = new WebLabel();
                spaceLabel.setWidth("5px");
                hLayoutComment.add(spaceLabel);
                hLayoutComment.setSpacing(true);
                hLayoutComment.setWidth("100%");
                labelComment.setWidth("100%");
                vhLayoutComment.setExpandRatio(WebComponentsHelper.unwrap(labelComment), 1.0f);

                String descr = cardComment.getComment();
                String[] parts = descr.split("\n");
                String preview = "<span>";
                boolean isCroped = (parts.length > maxHeightDigit ? true : false);
                for (int i = 0; i < Math.min(parts.length, maxHeightDigit); i++) {
                    String part = parts[i];
                    if (part.length() > maxWidthDigit) {
                        preview += part.substring(0, maxWidthDigit) + "...</br>";
                        isCroped = true;
                    } else
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
                    vhLayoutComment.replaceComponent(WebComponentsHelper.unwrap(labelComment), component);
                    component.setWidth("100%");
                    vhLayoutComment.setExpandRatio(component, 1.0f);
                }
                vLayout.add(hLayoutFrom);
                vLayout.add(hLayoutTo);
                vLayout.add(hLayoutComment);
                return vLayout;
            }
        });

        buttonCreate.setAction(new AbstractAction("add") {
            @Override
            public void actionPerform(com.haulmont.cuba.gui.components.Component component) {
                refreshCard();
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
                                                if (getFrame() instanceof WebWindow.Editor && ((WebWindow.Editor) getFrame()).commit(true)) {
                                                    refreshCard();
                                                    openCardSend(card);
                                                }

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
        Map<String, Object> paramsCard = new HashMap<>();

        CollectionDatasource cardRolesDs = null;
        CardProcFrame cardProcFrame = getComponent("cardProcFrame");
        if (cardProcFrame != null) {
            cardRolesDs = cardProcFrame.getCardRolesFrame().getDsContext().get("tmpCardRolesDs");
        } else {
            IFrame frame = getComponent("cardRolesFrame");
            if (frame != null)
                cardRolesDs = frame.getDsContext().get("tmpCardRolesDs");
        }
        paramsCard.put("cardRolesDs", cardRolesDs);
        paramsCard.put("item", card);
        paramsCard.put("rootFrame", this.<IFrame>getFrame());
        Window window = openWindow(card.getMetaClass().getName() + ".send", WindowManager.OpenType.DIALOG, paramsCard);
        window.addListener(new CloseListener() {
            @Override
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
            @Override
            public void windowClosed(String actionId) {
                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                    LoadContext ctx = new LoadContext(user.getClass()).setId(user.getId()).setView("_local");
                    User reloadUser = dataService.load(ctx);
                    button.setCaption(getUserNameLogin(reloadUser));
                }
            }
        });
    }

    private void refreshCard() {
        card = (Card) getDsContext().get("cardDs").getItem();
    }
}