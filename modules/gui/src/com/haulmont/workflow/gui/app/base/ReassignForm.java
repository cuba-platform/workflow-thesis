/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.gui.app.base;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.DevelopmentException;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.workflow.core.app.WfAssignmentService;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.core.global.ReassignInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.*;

public class ReassignForm extends AbstractWindow {
    @Inject
    protected Messages messages;
    @Inject
    protected CardRolesFrame cardRolesFrame;
    @Inject
    protected BoxLayout commentTextPane;
    @Inject
    protected CollectionDatasource<CardRole, UUID> cardRolesDs;
    @Inject
    protected Datasource<Card> cardDs;
    @Inject
    protected WfAssignmentService assignmentEngine;
    @Inject
    protected Configuration configuration;
    @Inject
    protected Metadata metadata;

    protected Card card;
    protected Card procContextCard;
    protected TextArea commentText;
    protected CardRolesFrame.CardProcRolesDatasource tmpCardRolesDs;
    protected ReassignInfo reassignmentInfo;

    private static final int DEFAULT_FORM_HEIGHT = 500;
    private static final int DEFAULT_FORM_WIDTH = 835;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        initStandardComponents();
        initReassignmentInfo(params);
        getDialogOptions()
                .setWidth(DEFAULT_FORM_WIDTH)
                .setHeight(DEFAULT_FORM_HEIGHT);
        initCardRolesFrame();
        initFormActions();
    }

    protected void initCardRolesFrame() {
        if (cardRolesFrame != null) {
            Iterable<String> visibleRoles = reassignmentInfo.getVisibleRoles();
            cardRolesFrame.setInactiveRoleVisible(false);
            cardRolesFrame.init();
            cardRolesFrame.setCard(procContextCard);
            tmpCardRolesDs.setVisibleRoles(ImmutableSet.<String>builder().add(reassignmentInfo.getRole()).addAll(visibleRoles).build());

            cardRolesDs.addStateChangeListener(e -> {
                if (e.getState() == Datasource.State.VALID) {
                    doAfterCardRolesDsInitialization();
                }
            });
            cardRolesDs.refresh();
        }
    }

    protected void initReassignmentInfo(Map<String, Object> params) {
        if (params.get("reassignmentInfo") == null)
            throw new DevelopmentException("Correct reassignment info must be present in parameters map");

        reassignmentInfo = (ReassignInfo) params.get("reassignmentInfo");
        card = (Card) params.get("card");
        procContextCard = reassignmentInfo.getCard();
        if (procContextCard == null) {
            procContextCard = card;
        }

        cardDs.setItem(metadata.getTools().copy(card));

        if (commentText != null)
            commentText.setRequired(reassignmentInfo.isCommentRequired());

        commentTextPane.setVisible(reassignmentInfo.isCommentVisible());
    }

    protected void doAfterCardRolesDsInitialization() {
        cardRolesFrame.procChanged(procContextCard.getProc());
        cardRolesFrame.setRequiredRolesCodesStr(reassignmentInfo.getRole());
        cardRolesFrame.fillMissingRoles();
    }

    protected void initFormActions() {
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
                close("cancel", true);
            }

            @Override
            public String getCaption() {
                return messages.getMessage(AppConfig.getMessagesPack(), "actions.Cancel");
            }
        });
    }

    protected void initStandardComponents() {
        commentText = (TextArea) getComponent("commentText");
        tmpCardRolesDs = (CardRolesFrame.CardProcRolesDatasource) cardRolesFrame.getDsContext().get("tmpCardRolesDs");
    }

    protected boolean commit() {
        if (__validate()) {
            Card reloadedCard = getDsContext().getDataSupplier().reload(procContextCard, metadata.getViewRepository()
                    .getView(Card.class, "with-roles"));
            if (cardRolesFrame != null)
                cardRolesDs.commit();

            doReassign(reloadedCard);
            return true;
        }
        return false;
    }

    protected void doReassign(Card reloadedCard) {
        List<CardRole> rolesToReassign = getCardRoles();
        if(CollectionUtils.isNotEmpty(rolesToReassign))
            assignmentEngine.reassign(procContextCard, reassignmentInfo.getState(), rolesToReassign, reloadedCard.getRoles(), getComment());
    }

    protected boolean __validate() {
        if (commentText != null && commentText.isRequired() && StringUtils.isBlank((String) commentText.getValue())) {
            showNotification(getMessage("putComments"), NotificationType.WARNING);
            return false;
        }
        if (cardRolesFrame != null) {
            Set<String> emptyRoles = cardRolesFrame.getEmptyRolesNames();
            if (!emptyRoles.isEmpty()) {
                showNotification(messages.formatMessage(ReassignForm.class, "actorNotDefined.msg",
                        Iterables.getFirst(emptyRoles, null)), NotificationType.WARNING);
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

    protected List<CardRole> getCardRoles() {
        List<CardRole> roles = new LinkedList<>();
        for (UUID id : cardRolesDs.getItemIds()) {
            CardRole cr = cardRolesDs.getItemNN(id);
            if (ObjectUtils.equals(reassignmentInfo.getRole(), cr.getCode()) && cr.getUser() != null)
                roles.add(cr);
        }
        return roles;
    }
}
