/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 20.04.2010 12:11:14
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.chile.core.model.utils.InstanceUtils;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.gui.WebWindow;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.cuba.web.gui.components.WebHBoxLayout;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.global.AssignmentInfo;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.web.ui.base.action.AbstractForm;
import com.vaadin.ui.ComponentContainer;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.*;

import static com.haulmont.cuba.gui.ServiceLocator.getDataService;

public class TransitionForm extends AbstractForm {

    @Inject
    private TextField commentText;
    @Inject
    protected CardRolesFrame cardRolesFrame;
    @Inject
    protected CollectionDatasource cardRolesDs;
    @Inject
    protected DateField dueDate;
    @Inject
    private TextField outcomeText;
    @Inject
    protected CardAttachmentsFrame attachmentsFrame;
    @Inject
    protected Tabsheet.Tab attachmentsTab;
    @Inject
    private Tabsheet tabsheet;
    @Inject
    private BoxLayout mainPane;
    @Inject
    private BoxLayout commentTextPane;
    @Inject
    private CheckBox refusedOnly;

    protected Boolean enableAttachments;

    @Inject
    protected Datasource assignmentDs;
    @Inject
    protected Datasource cardDs;
    @Inject
    protected CollectionDatasource attachmentsDs;
    @Inject
    protected Datasource varsDs;

    protected Card card;
    protected Card cardCopy;
    protected List<String> requiredAttachmentTypes = new ArrayList<String>();
    private Set<Tabsheet.Tab> initedTabs = new HashSet<Tabsheet.Tab>();
    private String requiredRolesCodesStr;
    private Map<String, AttachmentType> attachmentTypes;

    private final int DEFAULT_FORM_HEIGHT = 500;

    protected Map<Card, AssignmentInfo> cardAssignmentInfoMap;

    public TransitionForm(IFrame frame) {
        super(frame);
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        getDialogParams().setWidth(835);
        card = (Card) params.get("param$card");
        cardCopy = (Card) InstanceUtils.copy(card);
        cardDs = getDsContext().get("cardDs");
        cardDs.setItem(cardCopy);

        String formHeightStr = (String) params.get("formHeight");
        Integer formHeight = DEFAULT_FORM_HEIGHT;
        try {
            formHeight = Integer.valueOf(formHeightStr);
        } catch (NumberFormatException e) {
        }

        getDialogParams().setHeight(formHeight);


        if (cardRolesFrame != null) {
            requiredRolesCodesStr = (String) params.get("param$requiredRoles");
            String additionalRolesCodes = (String) params.get("param$additionalRoles");
            if (!StringUtils.isEmpty(additionalRolesCodes)) {
                requiredRolesCodesStr += "," + additionalRolesCodes;
            }

            cardRolesFrame.init();
            cardRolesFrame.setCard(card);
            cardRolesDs = getDsContext().get("cardRolesDs");
            cardRolesDs.addListener(new DsListenerAdapter() {
                @Override
                public void stateChanged(Datasource ds, Datasource.State prevState, Datasource.State state) {
                    if (state == Datasource.State.VALID) {
                        cardRolesFrame.procChanged(card.getProc());
                        cardRolesFrame.setRequiredRolesCodesStr(requiredRolesCodesStr);
                        cardRolesFrame.fillMissingRoles();
                    }
                }
            });
            cardRolesDs.refresh();
        } else {
            if (commentTextPane != null) {
                mainPane.expand(commentTextPane);
                commentTextPane.expand(commentText);
            }
        }

        if (dueDate != null) {
            String dueDateRequired = (String) params.get("dueDateRequired");
            dueDate.setRequired(dueDateRequired != null && Boolean.valueOf(dueDateRequired).equals(Boolean.TRUE));
        }

        if (dueDate != null || refusedOnly != null) {
            varsDs.refresh();
        }

        String requiredAttachmentTypesParam = (String) params.get("requiredAttachmentTypes");
        if (!StringUtils.isEmpty(requiredAttachmentTypesParam))
            requiredAttachmentTypes = Arrays.asList(requiredAttachmentTypesParam.split("\\s*,\\s*"));


        String messagesPack = card.getProc().getMessagesPack();
        String activity = (String) params.get("param$activity");
        String transition = (String) params.get("param$transition");

        LoadContext ctx = new LoadContext(Assignment.class);
        Object assignmentId = params.get("param$assignmentId");
        //when starting process
        if (assignmentId != null) {
            ctx.setId(assignmentId);
            ctx.setView("resolution-edit");
            Assignment assignment = ServiceLocator.getDataService().load(ctx);
            assignmentDs.setItem(assignment);
            outcomeText.setValue(MessageProvider.getMessage(messagesPack, activity + "." + transition));
            if (commentText != null)
                commentText.setDatasource(assignmentDs, "comment");
        } else {
            outcomeText.setValue(MessageProvider.getMessage(messagesPack, WfConstants.ACTION_START));
        }
        outcomeText.setEditable(false);

        String formCaption = (String) params.get("param$formCaption");
        if (StringUtils.isNotBlank(formCaption))
            setCaption(MessageProvider.getMessage(messagesPack, formCaption));

        String dueDateLabelParam = (String) params.get("param$dueDateLabel");
        if (StringUtils.isNotBlank(dueDateLabelParam)) {
            Label dueDateLabel = getComponent("dueDateLabel");
            if (dueDateLabel != null)
                dueDateLabel.setValue(MessageProvider.getMessage(messagesPack, dueDateLabelParam));
        }

        String dueDateFormatParam = (String) params.get("param$dueDateFormat");
        if (StringUtils.isNotBlank(dueDateFormatParam)) {
            if ("dateTimeFormat".equals(dueDateFormatParam)) {
                dueDate.setResolution(DateField.Resolution.MIN);
                dueDate.setDateFormat(Datatypes.getFormatStrings(UserSessionProvider.getLocale()).getDateTimeFormat());
            }
        }

        if (commentText != null) {
            String commentRequired = (String) params.get("param$commentRequired");
            commentText.setRequired(commentRequired != null && Boolean.valueOf(commentRequired).equals(Boolean.TRUE));
        }

        enableAttachments = params.get("enableAttachments") == null ? false : BooleanUtils.toBooleanObject(params.get("enableAttachments").toString());

        attachmentsTab = tabsheet.getTab("attachmentsTab");
        attachmentsTab.setCaption(getAttachmentsTabCaption());

        attachmentsDs.addListener(new CollectionDsListenerAdapter() {
            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation) {
                attachmentsTab.setCaption(getAttachmentsTabCaption());
                initRequiredAttachmentsPane();
            }
        });

        attachmentsFrame.init();
        attachmentsFrame.setCardCommitCheckRequired(false);
        attachmentsTab.setCaption(getAttachmentsTabCaption());
        initRequiredAttachmentsPane();

        if (!enableAttachments) {
            attachmentsTab.setVisible(false);
        }

        addAction(new AbstractAction("windowCommit") {
            public void actionPerform(Component component) {
                if (doCommit())
                    close(COMMIT_ACTION_ID, true);
            }

            @Override
            public String getCaption() {
                return MessageProvider.getMessage(AppConfig.getMessagesPack(), "actions.Ok");
            }
        });

        addAction(new AbstractAction("windowClose") {
            public void actionPerform(Component component) {
                close("cancel");
            }

            @Override
            public String getCaption() {
                return MessageProvider.getMessage(AppConfig.getMessagesPack(), "actions.Cancel");
            }
        });

        cardAssignmentInfoMap = getContext().getParamValue("cardAssignmentInfoMap");
    }

//    private void initLazyTabs() {
//        tabsheet.addListener(new Tabsheet.TabChangeListener() {
//            public void tabChanged(Tabsheet.Tab newTab) {
//                if ("attachmentsTab".equals(newTab.getName()) && !initedTabs.contains(newTab)) {
//                    initedTabs.add(newTab);
//                    attachmentsFrame = getComponent("attachmentsFrame");
//                    attachmentsFrame.init();
//                    attachmentsFrame.setCardCommitCheckRequired(false);
//                    attachmentsTab.setCaption(getAttachmentsTabCaption());
//                    initRequiredAttachmentsPane();
//                }
//            }
//        });
//    }

    private String getAttachmentsTabCaption() {
        Set<String> presentAttachmentTypes = new HashSet<String>();
        for (Object itemId : attachmentsDs.getItemIds()) {
            CardAttachment attachment = (CardAttachment) attachmentsDs.getItem(itemId);
            if ((attachment.getAttachType() != null) && requiredAttachmentTypes.contains(attachment.getAttachType().getCode()))
                presentAttachmentTypes.add(attachment.getAttachType().getCode());
        }

        if (!requiredAttachmentTypes.isEmpty()) {
            return MessageProvider.formatMessage(getClass(), "attachmentsTabWithRequired",
                    attachmentsDs.getItemIds().size(), presentAttachmentTypes.size(), requiredAttachmentTypes.size());
        } else {
            if (attachmentsDs.getItemIds().size() > 0)
                return MessageProvider.formatMessage(getClass(), "attachmentsTabWithoutRequired",
                        attachmentsDs.getItemIds().size(), presentAttachmentTypes.size(), requiredAttachmentTypes.size());
            else
                return getMessage("attachments");
        }
    }

    private void initRequiredAttachmentsPane() {
        WebHBoxLayout requiredAttachmentsPane = getComponent("requiredAttachmentsPane");
        requiredAttachmentsPane.removeAllComponents();
        requiredAttachmentsPane.add(createRequiredAttachmentsLayout());
    }

    private GridLayout createRequiredAttachmentsLayout() {
        final int columnHeight = 3;
        final GridLayout grid = AppConfig.getFactory().createComponent(GridLayout.NAME);
        grid.setColumns(1);
        grid.setRows(columnHeight);
        int row = 0;
        int column = 0;
        List<String> presentAttachmentTypes = new ArrayList<String>();
        for (Object itemId : attachmentsDs.getItemIds()) {
            final Attachment attachment = (Attachment) attachmentsDs.getItem(itemId);
            AttachmentType attachType = attachment.getAttachType();
            if (attachType != null) {
                presentAttachmentTypes.add(attachType.getCode());
            }
        }
        for (String attachmentTypeCode : requiredAttachmentTypes) {
            if (row++ == columnHeight) {
                row = 0;
                grid.setColumns(column + 2);
                column++;
            }
            final AttachmentType type = getAttachmentType(attachmentTypeCode);
            Label label = AppConfig.getFactory().createComponent(Label.NAME);
            label.setValue(type != null ? type.getName() : attachmentTypeCode);
            grid.add(label, column, row);

            if (presentAttachmentTypes.contains(attachmentTypeCode)) {
                label.setStyleName("attachment-type-present");
            } else {
                label.setStyleName("attachment-type-missing");
            }

        }

        return grid;
    }


    private AttachmentType getAttachmentType(String code) {
        if (attachmentTypes == null) attachmentTypes = new HashMap<String, AttachmentType>();
        if (!attachmentTypes.containsKey(code)) {
            DataService dataService = getDataService();
            LoadContext ctx = new LoadContext(AttachmentType.class);
            ctx.setView("_local");
            ctx.setQueryString("select att from wf$AttachmentType att where att.code = :code").addParameter("code", code);
            List list = dataService.loadList(ctx);
            attachmentTypes.put(code, list.isEmpty() ? null : (AttachmentType) list.get(0));
        }
        return attachmentTypes.get(code);
    }

    protected List<Entity> copyAttachments() {
        List<Entity> commitList = new ArrayList<Entity>();
        WfService wfService = ServiceLocator.lookup(WfService.NAME);
        for (Map.Entry<Card, AssignmentInfo> entry : cardAssignmentInfoMap.entrySet()) {
            AssignmentInfo assignmentInfo = entry.getValue();
            if (assignmentInfo != null && !assignmentDs.getItem().getUuid().equals(assignmentInfo.getAssignmentId())) {
                Assignment loadAssignment = ServiceLocator.getDataService().<Assignment>load(new
                        LoadContext(Assignment.class).setView("resolutions").setId(assignmentInfo.getAssignmentId()));
                for (UUID uuid : (Collection<UUID>) attachmentsDs.getItemIds()) {
                    CardAttachment attachment = (CardAttachment) attachmentsDs.getItem(uuid);
                    CardAttachment cardAttachment = MetadataProvider.create(CardAttachment.class);
                    cardAttachment.setAssignment(loadAssignment);
                    cardAttachment.setCard(loadAssignment.getCard());
                    cardAttachment.setFile(attachment.getFile());
                    cardAttachment.setName(attachment.getName());
                    commitList.add(cardAttachment);
                }
                commitList.add(card);
            }
            wfService.setHasAttachmentsInCard(entry.getKey(), true);
        }
        return commitList;
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
            CommitContext<Entity> commitContext = new CommitContext<Entity>();
            commitContext.getCommitInstances().addAll(copyAttachments());
            getDsContext().getDataService().commit(commitContext);
        } else {
            if (attachmentsDs.size() > 0) {
                WfService wfService = ServiceLocator.lookup(WfService.NAME);
                wfService.setHasAttachmentsInCard(card, true);
            }
        }

        return true;
    }

    protected boolean validated() {
        if (commentText != null && commentText.isRequired() && StringUtils.isBlank((String) commentText.getValue())) {
            showNotification(getMessage("putComments"), NotificationType.WARNING);
            return false;
        }
        if ((dueDate != null) && (dueDate.getValue() != null) && (((Date) dueDate.getValue()).compareTo(TimeProvider.currentTimestamp()) < 0)) {
            showNotification(getMessage("dueDateIsLessThanNow"), NotificationType.WARNING);
            return false;
        }
        if ((dueDate != null) && dueDate.isRequired() && (dueDate.getValue() == null)) {
            showNotification(getMessage("putDueDate"), NotificationType.WARNING);
            return false;
        }
        if (cardRolesFrame != null) {
            Set<String> emptyRolesNames = getEmptyRolesNames();
            if (!emptyRolesNames.isEmpty()) {
                String message = "";
                for (String emptyRoleName : emptyRolesNames) {
                    message += MessageProvider.formatMessage(TransitionForm.class, "actorNotDefined.msg", emptyRoleName) + "<br/>";
                }
                showNotification(message, NotificationType.WARNING);
                return false;
            }
        }

        WebWindow component = getComponent();
        Collection<com.vaadin.ui.Field> fields = WebComponentsHelper.getComponents((ComponentContainer) component.getComponent(), com.vaadin.ui.Field.class);
        for (com.vaadin.ui.Field field : fields) {
            if (field.isVisible() && !field.isReadOnly() && field.isEnabled() && !field.isValid()) {
                showNotification(getMessage("fillRequiredFields"), NotificationType.WARNING);
                return false;
            }
        }

        if (enableAttachments != null && enableAttachments) {
            if (requiredAttachmentTypes != null) {
                List<String> missingAttachments = new ArrayList<String>(requiredAttachmentTypes);
                for (Object itemId : attachmentsDs.getItemIds()) {
                    CardAttachment attachment = (CardAttachment) attachmentsDs.getItem(itemId);
                    AttachmentType attachType = attachment.getAttachType();
                    if (attachType != null) {
                        missingAttachments.remove(attachType.getCode());
                    }
                }

                if (!missingAttachments.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<ul>");
                    for (String attachmentTypeCode : missingAttachments) {
                        final AttachmentType attachmentType = getAttachmentType(attachmentTypeCode);
                        String attachmentTypeName = attachmentType == null ? attachmentTypeCode : attachmentType.getName();
                        sb.append("<li>").append(attachmentTypeName).append("</li>");
                    }
                    sb.append("</ul>");
                    showNotification(getMessage("missingAttachments.msg"), sb.toString(), NotificationType.WARNING);
                    tabsheet.setTab(attachmentsTab);
                    return false;
                }
            }
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
}
