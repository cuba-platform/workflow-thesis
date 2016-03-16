/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.gui.app.base;

import com.google.common.collect.Sets;
import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.global.AssignmentInfo;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.gui.app.attachment.ProcessAttachmentsManager;
import com.haulmont.workflow.gui.base.action.AbstractForm;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.*;

/**
 */
public class TransitionForm extends AbstractForm {

    protected static final int DEFAULT_FORM_HEIGHT = 500;
    protected static final int DEFAULT_FORM_WIDTH = 835;

    protected int noCardRolesHeight = 350;

    @Inject
    protected TextArea commentText;
    @Inject
    protected CardRolesFrame cardRolesFrame;
    @Inject
    protected CollectionDatasource<CardRole, UUID> cardRolesDs;
    @Inject
    protected DateField dueDate;
    @Inject
    protected TextField outcomeText;
    @Inject
    private BoxLayout dueDatePane;
    @Inject
    protected CardAttachmentsFrame attachmentsFrame;
    @Inject
    protected TabSheet.Tab attachmentsTab;
    @Inject
    protected TabSheet tabsheet;
    @Inject
    protected BoxLayout mainPane;
    @Inject
    protected BoxLayout commentTextPane;
    @Inject
    protected CheckBox refusedOnly;
    @Inject
    protected TimeSource timeSource;
    @Inject
    protected Datasource<Assignment> assignmentDs;
    @Inject
    protected Datasource<Card> cardDs;
    @Inject
    protected CollectionDatasource<Attachment, UUID> attachmentsDs;
    @Inject
    protected Datasource varsDs;
    @Inject
    protected Metadata metadata;
    @Inject
    protected WfService wfService;
    @Inject
    protected UserSession userSession;
    @Inject
    protected DataSupplier dataSupplier;
    @Inject
    protected ProcessAttachmentsManager processAttachments;

    protected Card card;
    protected Card procContextCard;
    protected Boolean hideAttachments = false;
    protected List<String> requiredAttachmentTypes = new ArrayList<>();
    protected Map<String, AttachmentType> attachmentTypes;
    protected Map<Card, AssignmentInfo> cardAssignmentInfoMap;
    protected String visibleRoles;
    protected boolean cardProcRolesInited = false;

    protected String commentVisible;
    protected String cardRolesVisible;
    protected String dueDateVisible;
    protected String attachmentsVisible;
    protected String refusedOnlyVisible;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        getDialogParams().setWidth(DEFAULT_FORM_WIDTH);
        card = (Card) params.get("card");
        procContextCard = (Card) params.get("procContextCard");
        if (procContextCard == null) {
            procContextCard = card;
            params.put("procContextCard", procContextCard);
        }

        String messagesPack = procContextCard.getProc().getMessagesPack();
        String activity = (String) params.get("activity");
        String transition = (String) params.get("transition");
        Object assignmentId = params.get("assignmentId");
        initComponentVisibilityParameters(params);
        setComponentsVisibility();

        cardDs.setItem(dataSupplier.reload(card, cardDs.getView()));

        String formHeightStr = (String) params.get("formHeight");
        Integer formHeight = Boolean.valueOf((String) params.get("cardRolesVisible")) ? DEFAULT_FORM_HEIGHT : noCardRolesHeight;
        try {
            formHeight = Integer.valueOf(formHeightStr);
        } catch (NumberFormatException ignored) {
        }

        getDialogParams().setHeight(formHeight);
        if (Boolean.valueOf((String) params.get("cardRolesVisible"))) {
            if (cardRolesFrame != null) {
                initCardRolesFrame(params);
            }
        } else {
            collapseFormWithoutRoles();
        }

        LoadContext<Assignment> ctx = new LoadContext<>(Assignment.class);
        //when starting process
        if (assignmentId != null) {
            ctx.setId(assignmentId);
            ctx.setView("resolution-edit");
            Assignment assignment = getDsContext().getDataSupplier().load(ctx);
            assignmentDs.setItem(assignment);
            String parentMessagesPack = messagesPack;
            if (procContextCard.isSubProcCard())
                parentMessagesPack = procContextCard.getProcFamily().getCard().getProc().getMessagesPack();
            outcomeText.setValue(messages.getMessage(parentMessagesPack, activity + "." + transition));
            setCommentTextAssignmentDatasource();
        } else {
            outcomeText.setValue(messages.getMessage(messagesPack, WfConstants.ACTION_START));
        }
        outcomeText.setEditable(false);

        String formCaption = (String) params.get("formCaption");
        if (StringUtils.isNotBlank(formCaption))
            setCaption(messages.getMessage(messagesPack, formCaption));

        if (commentText != null) {
            String commentRequired = (String) params.get("commentRequired");
            commentText.setRequired(commentRequired != null && Boolean.valueOf(commentRequired).equals(Boolean.TRUE));
        }

        initDueDate(params, messagesPack);

        initAttachments(params);

        cardAssignmentInfoMap = getContext().getParamValue("cardAssignmentInfoMap");
    }

    protected void collapseFormWithoutRoles() {
        if (commentTextPane != null) {
            mainPane.expand(commentTextPane);
        }
    }

    protected void setCommentTextAssignmentDatasource() {
        if (commentText != null)
            commentText.setDatasource(assignmentDs, "comment");
    }

    protected void initDueDate(Map<String, Object> params, String messagesPack) {
        if (dueDate != null) {
            dueDate.setRequired(isDueDateRequired(params));

            String dueDateLabelParam = (String) params.get("dueDateLabel");
            if (StringUtils.isNotBlank(dueDateLabelParam)) {
                Label dueDateLabel = (Label) getComponent("dueDateLabel");
                if (dueDateLabel != null)
                    dueDateLabel.setValue(messages.getMessage(messagesPack, dueDateLabelParam));
            }

            String dueDateFormatParam = (String) params.get("dueDateFormat");
            if (StringUtils.isNotBlank(dueDateFormatParam)) {
                if ("dateTimeFormat".equals(dueDateFormatParam)) {
                    dueDate.setResolution(DateField.Resolution.MIN);
                    dueDate.setDateFormat(Datatypes.getFormatStrings(userSession.getLocale()).getDateTimeFormat());
                }
            }
        }

        if (dueDate != null || refusedOnly != null) {
            varsDs.refresh();
            if (!isDueDateVisible()) {
                dueDate.setValue(null);
            }
        }
    }

    protected boolean isDueDateRequired(Map<String, Object> params) {
        String dueDateRequired = (String) params.get("dueDateRequired");
        return dueDateRequired != null && Boolean.valueOf(dueDateRequired).equals(Boolean.TRUE);
    }

    protected void initAttachments(Map<String, Object> params) {
        String requiredAttachmentTypesParam = getRequiredAttachmentTypes();
        if (!StringUtils.isEmpty(requiredAttachmentTypesParam))
            requiredAttachmentTypes = Arrays.asList(requiredAttachmentTypesParam.split("\\s*,\\s*"));

        hideAttachments = params.get("hideAttachments") == null ? false : BooleanUtils.toBooleanObject(params.get("hideAttachments").toString());

        attachmentsTab = tabsheet.getTab("attachmentsTab");
        attachmentsTab.setCaption(getAttachmentsTabCaption());

        attachmentsDs.addCollectionChangeListener(e -> {
            attachmentsTab.setCaption(getAttachmentsTabCaption());
            updateRequiredAttachmentsPane();
        });

        attachmentsFrame.setCardCommitCheckRequired(false);
        attachmentsTab.setCaption(getAttachmentsTabCaption());
        updateRequiredAttachmentsPane();

        if (hideAttachments) {
            attachmentsTab.setVisible(false);
        }
    }

    protected void initComponentVisibilityParameters(Map<String, Object> params) {
        commentVisible = (String) params.get("commentVisible");
        cardRolesVisible = (String) params.get("cardRolesVisible");
        dueDateVisible = (String) params.get("dueDateVisible");
        attachmentsVisible = (String) params.get("attachmentsVisible");
        refusedOnlyVisible = (String) params.get("refusedOnlyVisible");
    }

    protected void setComponentsVisibility() {
        refusedOnly.setVisible(isRefusedOnlyVisible());
        dueDatePane.setVisible(isDueDateVisible());
        commentTextPane.setVisible(isCommentVisible());
        cardRolesFrame.setVisible(isCardRolesVisible());
    }

    @Override
    protected void onWindowCommit() {
        if (doCommit())
            close(Window.COMMIT_ACTION_ID, true);
    }

    @Override
    protected void onWindowClose() {
        close("cancel");
    }

    protected void initCardRolesFrame(Map<String, Object> params) {
        cardRolesFrame.setInactiveRoleVisible(false);
        cardRolesFrame.init();
        visibleRoles = (String) params.get("visibleRoles");
        if (StringUtils.isNotBlank(visibleRoles))
            cardRolesFrame.tmpCardRolesDs.setVisibleRoles(Sets.newHashSet(visibleRoles.split("\\s*,\\s*")));

        cardRolesFrame.setCard(procContextCard);

        cardRolesDs.addStateChangeListener(e -> {
            if (e.getState() == Datasource.State.VALID) {
                cardRolesFrame.procChanged(procContextCard.getProc());
                cardRolesFrame.setRequiredRolesCodesStr(getRequiredRoles());
                cardRolesFrame.fillMissingRoles();
            }
        });
        cardRolesDs.refresh();
        cardProcRolesInited = true;
    }

    protected String getRequiredRoles() {
        return getContext().getParamValue("requiredRoles");
    }

    protected String getRequiredAttachmentTypes() {
        return getContext().getParamValue("requiredAttachmentTypes");
    }

    protected String getAttachmentsTabCaption() {
        Set<String> presentAttachmentTypes = getPresentAttachmentTypes();

        if (!requiredAttachmentTypes.isEmpty()) {
            return messages.formatMessage(getClass(), "attachmentsTabWithRequired",
                    attachmentsDs.getItemIds().size(), presentAttachmentTypes.size(), requiredAttachmentTypes.size());
        } else {
            if (attachmentsDs.getItemIds().size() > 0)
                return messages.formatMessage(getClass(), "attachmentsTabWithoutRequired",
                        attachmentsDs.getItemIds().size(), presentAttachmentTypes.size(), requiredAttachmentTypes.size());
            else
                return getMessage("attachments");
        }
    }

    protected Set<String> getPresentAttachmentTypes() {
        Set<String> presentAttachmentTypes = new HashSet<String>();
        for (UUID itemId : attachmentsDs.getItemIds()) {
            Attachment attachment = attachmentsDs.getItem(itemId);
            if (attachment != null && attachment.getAttachType() != null
                    && requiredAttachmentTypes.contains(attachment.getAttachType().getCode()))
                presentAttachmentTypes.add(attachment.getAttachType().getCode());
        }
        return presentAttachmentTypes;
    }

    protected void updateRequiredAttachmentsPane() {
        BoxLayout requiredAttachmentsPane = (BoxLayout) getComponent("requiredAttachmentsPane");
        for (Component component : requiredAttachmentsPane.getComponents()) {
            requiredAttachmentsPane.remove(component);
        }
        requiredAttachmentsPane.add(createRequiredAttachmentsLayout());
    }

    protected GridLayout createRequiredAttachmentsLayout() {
        final int columnHeight = 3;
        final GridLayout grid = AppConfig.getFactory().createComponent(GridLayout.class);
        grid.setColumns(1);
        grid.setRows(columnHeight);
        int row = 0;
        int column = 0;
        Set<String> presentAttachmentTypes = getPresentAttachmentTypes();
        for (String attachmentTypeCode : requiredAttachmentTypes) {
            if (row == columnHeight) {
                row = 0;
                grid.setColumns(column + 2);
                column++;
            }
            Label label = AppConfig.getFactory().createComponent(Label.class);
            label.setValue(getAttachmentTypeLabelValue(attachmentTypeCode));
            grid.add(label, column, row++);

            if (presentAttachmentTypes.contains(attachmentTypeCode)) {
                label.setStyleName("attachment-type-present");
            } else {
                label.setStyleName("attachment-type-missing");
            }

        }

        return grid;
    }

    protected String getAttachmentTypeLabelValue(String attachmentTypeCode) {
        AttachmentType type = getAttachmentType(attachmentTypeCode);
        return type != null ? type.getName() : attachmentTypeCode;
    }

    protected AttachmentType getAttachmentType(String code) {
        if (attachmentTypes == null) attachmentTypes = new HashMap<>();
        if (!attachmentTypes.containsKey(code)) {
            LoadContext ctx = new LoadContext(AttachmentType.class);
            ctx.setView("_local");
            ctx.setQueryString("select att from wf$AttachmentType att where att.code = :code").setParameter("code", code);
            List list = getDsContext().getDataSupplier().loadList(ctx);
            attachmentTypes.put(code, list.isEmpty() ? null : (AttachmentType) list.get(0));
        }
        return attachmentTypes.get(code);
    }

    protected List<Entity> copyAttachments() {
        Collection<Assignment> assignments = new ArrayList<>();
        for (Map.Entry<Card, AssignmentInfo> entry : cardAssignmentInfoMap.entrySet()) {
            Assignment assignment = getDsContext().getDataSupplier().<Assignment>load(new
                    LoadContext(Assignment.class).setView("resolutions").setId(entry.getValue().getAssignmentId()));
            assignments.add(assignment);
        }
        return processAttachments.copyAttachments(assignments);
    }

    protected boolean doCommit() {
        if (!validated()) return false;

        if (commentText != null) {
            if (Datasource.State.VALID.equals(assignmentDs.getState()))
                assignmentDs.commit();
            else {
                if (card.getInitialProcessVariables() == null) {
                    card.setInitialProcessVariables(new HashMap<String, Object>(1));
                }
                card.getInitialProcessVariables().put("startProcessComment", commentText.getValue());
            }
        }

        if (cardRolesFrame != null)
            cardRolesDs.commit();

        if (dueDate != null || refusedOnly != null)
            varsDs.commit();
        if (cardAssignmentInfoMap != null) {
            CommitContext commitContext = new CommitContext();
            commitContext.getCommitInstances().addAll(copyAttachments());
            getDsContext().getDataSupplier().commit(commitContext);
        } else {
            if (attachmentsDs.size() > 0) {
                attachmentsDs.commit();
                wfService.setHasAttachmentsInCard(card, true);
            }
        }

        return true;
    }

    protected boolean isTwoProcessStarting() {
        if (card.getJbpmProcessId() == null) {
            if (wfService.processStarted(card)) {
                showNotification(getMessage("processAlreadyStarted"), NotificationType.ERROR);
                return true;
            }
        }
        return false;
    }

    protected boolean validated() {

        boolean isTwoProcessStarting = isTwoProcessStarting();
        if (isTwoProcessStarting) {
            return false;
        }

        if (commentText != null && commentText.isRequired() && StringUtils.isBlank((String) commentText.getValue())) {
            showNotification(getMessage("putComments"), NotificationType.WARNING);
            return false;
        }
        if (dueDateIsLessThanNow()) {
            showNotification(getMessage("dueDateIsLessThanNow"), NotificationType.WARNING);
            return false;
        }
        if ((dueDate != null) && dueDate.isRequired() && (dueDate.getValue() == null)) {
            showNotification(getMessage("putDueDate"), NotificationType.WARNING);
            return false;
        }
        if (cardRolesFrame != null && cardProcRolesInited) {
            Set<String> emptyRolesNames = getEmptyRolesNames();
            if (!emptyRolesNames.isEmpty()) {
                String message = "";
                for (String emptyRoleName : emptyRolesNames) {
                    message += messages.formatMessage(TransitionForm.class, "actorNotDefined.msg", emptyRoleName) + "\n";
                }
                showNotification(message, NotificationType.WARNING);
                return false;
            }
        }

        for (Component component : getComponents()) {
            if (component instanceof Field) {
                Field field = (Field) component;
                if (field.isVisible() && field.isEditable() && field.isEnabled() && !field.isValid()) {
                    showNotification(getMessage("fillRequiredFields"), NotificationType.WARNING);
                    return false;
                }
            }
        }

        if (!hideAttachments) {
            if (requiredAttachmentTypes != null) {
                return attachmentsValidated();
            }
        }

        return true;
    }

    protected boolean dueDateIsLessThanNow() {
        return (dueDate != null) && (dueDate.getValue() != null) &&
                (((Date) dueDate.getValue()).compareTo(timeSource.currentTimestamp()) < 0);
    }

    protected boolean attachmentsValidated() {
        List<String> missingAttachments = new ArrayList<>(requiredAttachmentTypes);
        for (UUID itemId : attachmentsDs.getItemIds()) {
            Attachment attachment = attachmentsDs.getItem(itemId);
            if (attachment != null && attachment.getAttachType() != null) {
                missingAttachments.remove(attachment.getAttachType().getCode());
            }
        }

        if (!missingAttachments.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<ul style=\"text-align:left;\">");
            for (String attachmentTypeCode : missingAttachments) {
                final AttachmentType attachmentType = getAttachmentType(attachmentTypeCode);
                String attachmentTypeName = attachmentType == null ? attachmentTypeCode : attachmentType.getName();
                sb.append("<li>").append(attachmentTypeName).append("</li>");
            }
            sb.append("</ul>");
            showNotification(getMessage("missingAttachments.msg"), sb.toString(), NotificationType.WARNING_HTML);
            tabsheet.setTab(attachmentsTab);
            return false;
        }
        return true;
    }

    protected Set<String> getEmptyRolesNames() {
        return cardRolesFrame.getEmptyRolesNames();
    }

    @Override
    public String getComment() {
        if (commentText != null)
            return commentText.getValue();
        else
            return null;
    }

    protected boolean isCommentVisible() {
        return commentVisible != null && Boolean.valueOf(commentVisible);
    }

    protected boolean isCardRolesVisible() {
        return cardRolesVisible != null && Boolean.valueOf(cardRolesVisible);
    }

    protected boolean isDueDateVisible() {
        //If duration in process is enabled we won't show duration field in transition form.
        //Assignments' dueDates will be set by roles' durations.
        if (card != null && card.getProc() != null && BooleanUtils.isTrue(card.getProc().getDurationEnabled()))
            return false;
        return dueDateVisible != null && Boolean.valueOf(dueDateVisible);
    }

    protected boolean isAttachmentsVisible() {
        return attachmentsVisible != null && Boolean.valueOf(attachmentsVisible);
    }

    protected boolean isRefusedOnlyVisible() {
        return refusedOnlyVisible != null && Boolean.valueOf(refusedOnlyVisible);
    }
}
