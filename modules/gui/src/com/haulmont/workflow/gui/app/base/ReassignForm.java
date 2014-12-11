/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.app.base;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.haulmont.chile.core.model.utils.InstanceUtils;
import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.workflow.core.app.WfAssignmentService;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author subbotin
 * @version $Id$
 */
public class ReassignForm extends AbstractWindow {
    @Inject
    protected Messages messages;

    @Inject
    protected CardRolesFrame cardRolesFrame;

    @Inject
    private BoxLayout commentTextPane;

    protected TextField commentText;

    @Inject
    protected CollectionDatasource cardRolesDs;
    @Inject
    protected Datasource<Card> cardDs;
    protected CardRolesFrame.CardProcRolesDatasource tmpCardRolesDs;

    @Inject
    protected WfAssignmentService assignmentEngine;

    @Inject
    protected Configuration configuration;

    protected Card card;
    protected Card procContextCard;
    protected String state;
    protected String role;
    protected boolean commentVisible;

    private static final int DEFAULT_FORM_HEIGHT = 500;
    private static final int DEFAULT_FORM_WIDTH = 835;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        commentText = getComponent("commentText");
        tmpCardRolesDs = cardRolesFrame.getDsContext().get("tmpCardRolesDs");
        commentVisible = BooleanUtils.isTrue((Boolean) params.get("commentVisible"));

        card = (Card) params.get("card");
        procContextCard = (Card) params.get("procContextCard");
        if (procContextCard == null) {
            procContextCard = card;
            params.put("procContextCard", procContextCard);
        }
        state = (String) params.get("state");
        cardDs.setItem((Card) InstanceUtils.copy(card));

        getDialogParams().setWidth(DEFAULT_FORM_WIDTH);
        getDialogParams().setHeight(DEFAULT_FORM_HEIGHT);

        if (cardRolesFrame != null) {
            role = (String) params.get("role");
            Iterable<String> visibleRoles = Splitter.on(",").split(StringUtils.defaultIfEmpty((String) params.get("visibleRoles"), ""));
            cardRolesFrame.setInactiveRoleVisible(false);
            cardRolesFrame.init();
            cardRolesFrame.setCard(procContextCard);
            tmpCardRolesDs.setVisibleRoles(ImmutableSet.<String>builder().add(role).addAll(visibleRoles).build());
            cardRolesDs.addListener(new DsListenerAdapter() {
                @Override
                public void stateChanged(Datasource ds, Datasource.State prevState, Datasource.State state) {
                    if (state == Datasource.State.VALID) {
                        cardRolesFrame.procChanged(procContextCard.getProc());
                        cardRolesFrame.setRequiredRolesCodesStr(role);
                        cardRolesFrame.fillMissingRoles();
                    }
                }
            });
            cardRolesDs.refresh();
        }

        if (commentText != null) {
            commentText.setRequired(BooleanUtils.isTrue((Boolean) params.get("commentRequired")));
        }

        setCaption(getMessage("reassign.caption"));

        ClientConfig clientConfig = configuration.getConfig(ClientConfig.class);

        addAction(new AbstractAction(Editor.WINDOW_COMMIT, clientConfig.getCommitShortcut()) {
            @Override
            public void actionPerform(Component component) {
                if (commit())
                    close(COMMIT_ACTION_ID, true);
            }

            @Override
            public String getCaption() {
                return messages.getMessage(AppConfig.getMessagesPack(), "actions.Ok");
            }
        });

        addAction(new AbstractAction(Editor.WINDOW_CLOSE, clientConfig.getCloseShortcut()) {
            @Override
            public void actionPerform(Component component) {
                close("cancel");
            }

            @Override
            public String getCaption() {
                return messages.getMessage(AppConfig.getMessagesPack(), "actions.Cancel");
            }
        });

        setCommentVisible();
    }


    protected boolean commit() {
        if (__validate()) {
            if (cardRolesFrame != null)
                cardRolesDs.commit();
            List<CardRole> roles = new LinkedList<CardRole>();
            for (Object id : cardRolesDs.getItemIds()) {
                @SuppressWarnings("unchecked")
                CardRole cr = (CardRole) cardRolesDs.getItem(id);
                if (ObjectUtils.equals(role, cr.getCode()) && cr.getUser() != null)
                    roles.add(cr);
            }
            assignmentEngine.reassign(card, state, roles, getComment());
            return true;
        }
        return false;
    }

    protected boolean __validate() {
        if (commentText != null && commentText.isRequired() && StringUtils.isBlank((String) commentText.getValue())) {
            showNotification(getMessage("putComments"), NotificationType.WARNING);
            return false;
        }
        if (cardRolesFrame != null) {
            Set<String> emptyRoles = cardRolesFrame.getEmptyRolesNames();
            if (!emptyRoles.isEmpty()) {
                showNotification(messages.formatMessage(ReassignForm.class, "actorNotDefined.msg", Iterables.getFirst(emptyRoles, null)), NotificationType.WARNING);
                return false;
            }
        }
        return true;
    }

    protected String getComment() {
        if (commentText != null) {
            return commentText.getValue();
        }
        return null;
    }

    protected void setCommentVisible() {
        commentTextPane.setVisible(commentVisible);
    }
}
