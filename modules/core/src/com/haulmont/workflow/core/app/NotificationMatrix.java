/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;

import com.google.common.collect.Lists;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardInfo;
import com.haulmont.workflow.core.entity.CardRole;
import groovy.lang.Binding;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.*;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pavlov
 * @version $Id$
 */
@ManagedBean(NotificationMatrixAPI.NAME)
public class NotificationMatrix implements NotificationMatrixAPI {

    protected static final String MAIL_SHEET = "Mail";
    protected static final String TRAY_SHEET = "Tray";
    protected static final String SMS_SHEET = "Sms";
    protected static final String ROLES_SHEET = "Roles";
    protected static final String STATES_SHEET = "States";
    protected static final String ACTIONS_SHEET = "Actions";
    public static final String OVERDUE_CARD_STATE = "Overdue";

    private static Log log = LogFactory.getLog(NotificationMatrixService.class);

    @Inject
    protected UserSessionSource userSessionSource;

    @Inject
    protected Persistence persistence;

    @Inject
    protected Scripting scripting;

    @Inject
    protected Resources resources;

    @Inject
    protected SmsSenderAPI smsSenderAPI;

    @Inject
    protected Metadata metadata;

    /**
     * Allows specific WfMailWorker implementations to handle email notification event
     */
    protected boolean sendNotificationToBlankEmail = false;

    protected Map<String, Map<String, String>> cache = new ConcurrentHashMap<String, Map<String, String>>();
    protected Map<String, Map<String, NotificationMessageBuilder>> messageCache = new ConcurrentHashMap<String, Map<String, NotificationMessageBuilder>>();

    public void setSendNotificationToBlankEmail(boolean sendNotificationToBlankEmail) {
        this.sendNotificationToBlankEmail = sendNotificationToBlankEmail;
    }

    private Map<String, String> readRoles(Workbook Workbook) {
        Sheet sheet = Workbook.getSheet(ROLES_SHEET);
        Map<String, String> rolesMap = new HashMap<>();
        for (Iterator it = sheet.rowIterator(); it.hasNext(); ) {
           Row row = (Row) it.next();

            Cell cell = row.getCell(0);
            Cell cellCode = row.getCell(1);

            if (cell != null && cellCode != null) {

                String role = null;
                String code = null;

                if (Cell.CELL_TYPE_STRING == cell.getCellType()) {
                    code = cell.getRichStringCellValue().getString();
                }

                if (Cell.CELL_TYPE_STRING == cellCode.getCellType()) {
                    role = cellCode.getRichStringCellValue().getString();
                }

                code = StringUtils.trimToEmpty(code);
                role = StringUtils.trimToEmpty(role);

                if (!StringUtils.isEmpty(code) && !StringUtils.isEmpty(role)) {
                    rolesMap.put(code, role);
                } else {
                    log.error(String.format("code %s or role %s must not be empty", code, role));
                }
            }

        }

        return rolesMap;
    }

    private Map<String, String> readStates(Workbook Workbook) {
        Sheet sheet = Workbook.getSheet(STATES_SHEET);
        Map<String, String> statesMap = new HashMap<>();
        for (Iterator it = sheet.rowIterator(); it.hasNext(); ) {
            Row row = (Row) it.next();

            Cell cell = row.getCell(0);
            Cell cellCode = row.getCell(1);
            if ((cell == null) || (cellCode == null)) {
                break;
            }
            String state = null;
            String code = null;

            if (Cell.CELL_TYPE_STRING == cell.getCellType()) {
                code = cell.getRichStringCellValue().getString();
            }

            if (Cell.CELL_TYPE_STRING == cell.getCellType()) {
                state = cellCode.getRichStringCellValue().getString();
            }

            code = StringUtils.trimToEmpty(code);
            state = StringUtils.trimToEmpty(state);

            if (!StringUtils.isEmpty(code) && !StringUtils.isEmpty(state)) {
                statesMap.put(code, state);
            } else {
                log.error(String.format("code %s or state %s must not be empty", code, state));
            }
        }
        return statesMap;
    }

    /**
     * Method for load message templates or groovy scripts
     *
     * @param workbook
     * @return true, if xls file have templeates or groovy scripts
     */
    private void loadMessages(Workbook workbook, String processPath) {
        Sheet sheet = workbook.getSheet(ACTIONS_SHEET);
        for (Iterator it = sheet.rowIterator(); it.hasNext(); ) {
            Row row = (Row) it.next();

            Cell cellNotifType = row.getCell(0);
            Cell cellTempleateType = row.getCell(1);
            Cell cellText = row.getCell(2);

            if ((cellNotifType == null) || (cellTempleateType == null) || (cellText == null)) {
                return;
            }

            String notificationType = null;
            String templeateType = null;
            String text = null;

            if (Cell.CELL_TYPE_STRING == cellNotifType.getCellType()) {
                notificationType = cellNotifType.getStringCellValue();
            }

            if (Cell.CELL_TYPE_STRING == cellTempleateType.getCellType()) {
                templeateType = cellTempleateType.getStringCellValue();
            }

            if (Cell.CELL_TYPE_STRING == cellText.getCellType()) {
                text = cellText.getStringCellValue();
            }

            notificationType = StringUtils.trimToEmpty(notificationType);
            templeateType = StringUtils.trimToEmpty(templeateType);

            if (!StringUtils.isEmpty(notificationType) && !StringUtils.isEmpty(templeateType) && !StringUtils.isEmpty(text)) {
                NotificationMessageBuilder message = getNotificationMessageBuilder(templeateType, text);
                Map<String, NotificationMessageBuilder> map = messageCache.get(processPath);
//                NotificationType type = NotificationType.fromId(notificationType);
//                if (type != null) {
                map.put(notificationType, message);
//                }

            } else {
                log.error(String.format("NotificationType %s or TempleateType %s or ScriptText %s is missing or incorrect", notificationType, templeateType, text));
            }

        }

    }

    private boolean fillMatrixBySheet(Map<String, String> matrix, String sheetName, Workbook matrixTemplate,
                                      Map<String, String> rolesMap, Map<String, String> statesMap, String processPath) {
        Sheet sheet = matrixTemplate.getSheet(sheetName);
        if (sheet == null) {
            return false;
        }

        final CellStyle ColumnHeaderStyle = sheet.getRow(2).getCell(1).getCellStyle();
        final CellStyle RowHeaderStyle = sheet.getRow(3).getCell(0).getCellStyle();
        final CellStyle CellStyle = sheet.getRow(3).getCell(1).getCellStyle();

        Row statesRow = sheet.getRow(1);

        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (row == null) {
                continue;
            }

            Cell roleCell = row.getCell(0);
            if (roleCell == null || "".equals(roleCell.getRichStringCellValue().toString())) {
                continue;
            }
            String addresseeRole = roleCell.getRichStringCellValue().getString();
            String codeAddresseeRole = rolesMap.get(StringUtils.trimToEmpty(addresseeRole));

            if (codeAddresseeRole == null) {
                throw new RuntimeException(String.format("Unable to get code for role %s in %s notification matrix",
                        addresseeRole, sheetName));
            }

            for (int j = 1; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);

                if (cell == null) {
                    continue;
                }

                String value = cell.getRichStringCellValue().toString();
                if ("".equals(value)) {
                    continue;
                }

//                NotificationType type = NotificationType.fromId(value);
                if (value != null && !NotificationType.NO.toString().equals(value) &&
                        (NotificationType.fromId(value) != null || messageCache.get(processPath).containsKey(value))) {
                    String state = statesRow.getCell(j).getRichStringCellValue().getString();

                    String codeState = statesMap.get(StringUtils.trimToEmpty(state));

                    if (codeState != null) {
                        if (codeState.contains(",")) {
                            StringTokenizer tokenizer = new StringTokenizer(codeState, ",");
                            while (tokenizer.hasMoreTokens()) {
                                String s = StringUtils.trimToEmpty(tokenizer.nextToken());
                                if (!"".equals(s)) {
                                    String key = s + '_' + codeAddresseeRole + "_" + sheetName;
                                    matrix.put(key, value);
                                }
                            }
                        } else {
                            String key = codeState + '_' + codeAddresseeRole + "_" + sheetName;
                            matrix.put(key, value);
                        }
                    } else {
                        throw new RuntimeException(String.format("Unable to get code for state %s in %s notification matrix",
                                state, sheetName));
                    }
                }
            }
        }

        return true;
    }

    private Card reloadCard(Card card) {
        EntityManager em = persistence.getEntityManager();
        Card reloadedCard = em.find(Card.class, card.getId());
        if (reloadedCard == null)
            throw new RuntimeException(String.format("Card not found: %s", card.getId()));

        return reloadedCard;
    }

    @SuppressWarnings("unchecked")
    private Assignment reloadAssignment(Assignment assignment) {
        EntityManager em = persistence.getEntityManager();
        Assignment reloadedAssignment = (Assignment) em.find(
                metadata.getExtendedEntities().getEffectiveClass(Assignment.class), assignment.getId());
        if (reloadedAssignment == null)
            throw new RuntimeException(String.format("Assignment not found: %s", assignment.getId()));

        return reloadedAssignment;
    }

    private synchronized void load(String processPath) throws Exception {
        Map<String, String> matrix = cache.get(processPath);
        if (matrix != null)
            return;

        InputStream fis = resources.getResourceAsStream(processPath.replace('.', '/') + "/" + "notification.xls");
        if (fis == null)
            return;

        try {
            Workbook Workbook = WorkbookFactory.create(fis);

            Map<String, String> rolesMap = readRoles(Workbook);
            Map<String, String> statesMap = readStates(Workbook);

            messageCache.put(processPath, new HashMap<String, NotificationMessageBuilder>());
            loadMessages(Workbook, processPath);

            matrix = new HashMap<>();
            boolean mailMatrixFilled = fillMatrixBySheet(matrix, MAIL_SHEET, Workbook, rolesMap, statesMap, processPath);
            boolean trayMatrixFilled = fillMatrixBySheet(matrix, TRAY_SHEET, Workbook, rolesMap, statesMap, processPath);
            boolean smsMatrixFilled = fillMatrixBySheet(matrix, SMS_SHEET, Workbook, rolesMap, statesMap, processPath);

            postLoad(processPath, matrix);

            if (mailMatrixFilled || trayMatrixFilled || smsMatrixFilled)
                cache.put(processPath, matrix);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    protected void postLoad(String processPath, Map<String, String> matrix) {
    }

    @Override
    public void reload(String processPath) throws Exception {
        String path = StringUtils.trimToNull(processPath);
        if (path == null)
            throw new IllegalArgumentException("Path to notification matrix must not be empty or null");

        cache.remove(path);

        try {
            load(path);
        } catch (Exception e) {
            log.warn(String.format("Can't load %s notification matrix", processPath), e);
            throw e;
        }
    }

    protected void notifyUser(Card card, CardRole cardRole, Assignment assignment, Map<String, String> matrix, String state,
                              List<User> mailList, List<User> trayList, List<User> smsList, NotificationMatrixMessage.MessageGenerator messageGenerator) {
        String processPath = StringUtils.trimToEmpty(card.getProc().getMessagesPack());
        String type;
        String key = state + "_" + cardRole.getCode();
        final User user = cardRole.getUser();

        if (user == null) {
            log.warn("User in card role with id '" + cardRole.getId() + "' is null. Notification will not be sent");
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("assignment", assignment);
        variables.put("card", card);
        variables.put("user", user);
        variables.put("cardRole", cardRole);

        //email
        notifyUserByEmail(cardRole, mailList, matrix, processPath, key, variables, messageGenerator);

        //tray && notificationPanel
        if (!trayList.contains(user) && BooleanUtils.isTrue(cardRole.getNotifyByCardInfo()) &&
                ((type = matrix.get(key + "_" + TRAY_SHEET)) != null)) {
            NotificationMessageBuilder notificationMessage = messageCache.get(processPath).get(type);
            if (notificationMessage != null) {
                variables.put("messagetemplate", notificationMessage);
            } else {
                variables.put("script", getScriptByNotificationType(processPath, type));
            }
            createNotificationCardInfo(card, assignment, cardRole.getUser(), getCardInfoTypeByState(type), messageGenerator.generateMessage(variables));
            trayList.add(user);
        }

        //sms
        if (!smsList.contains(user) && ((type = matrix.get(key + "_" + SMS_SHEET)) != null) && isUserNotifiedBySms(user)) {
            NotificationMessageBuilder notificationMessage = messageCache.get(processPath).get(type);
            if (notificationMessage != null) {
                variables.put("messagetemplate", notificationMessage);
                variables.remove("script");
            } else {
                variables.put("script", getScriptByNotificationType(processPath, type));
            }
            final NotificationMatrixMessage message = messageGenerator.generateMessage(variables);

            smsSenderAPI.sendSmsAsync(message.getSubject(), user.getName(), message.getBody());

            smsList.add(user);
        }
    }

    protected void notifyUserByEmail(CardRole cardRole, List<User> mailList, Map<String, String> matrix,
                                     String processPath, String key, Map<String, Object> variables,
                                     NotificationMatrixMessage.MessageGenerator messageGenerator){
        String type = matrix.get(key + "_" + MAIL_SHEET);
        User user = cardRole.getUser();

        boolean notifyByEmail = StringUtils.isNotBlank(user.getEmail()) || sendNotificationToBlankEmail;
        if (notifyByEmail && !mailList.contains(user) &&
                BooleanUtils.isTrue(cardRole.getNotifyByEmail()) && (type != null)) {
            NotificationMessageBuilder notificationMessage = messageCache.get(processPath).get(type);
            if (notificationMessage != null) {
                variables.put("messagetemplate", notificationMessage);
            } else {
                variables.put("script", getScriptByNotificationType(processPath, type));
            }
            final NotificationMatrixMessage message = messageGenerator.generateMessage(variables);
            if (message.getSubject().length() > 500) {
                message.setSubject(message.getSubject().substring(0, 500));
            }

            WfMailWorker wfMailWorker = AppBeans.get(WfMailWorker.NAME);
            try {
                wfMailWorker.sendEmail(user, message.getSubject(), message.getBody());
            } catch (EmailException e) {
                log.warn(e);
            }

            mailList.add(user);
        }
    }

    protected boolean isUserNotifiedBySms(User user) {
        Transaction tx = persistence.getTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createQuery("select e from wf$UserNotifiedBySms e where e.user.id = :user").setParameter("user", user);
            List list = query.getResultList();
            tx.commit();
            return !list.isEmpty();
        } catch (Exception e) {
            log.error(e);
            return false;
        } finally {
            tx.end();
        }
    }

    public void notifyByCard(Card card, String state) {
        notifyByCard(card, state, Collections.<String>emptyList());
    }

    public void notifyByCard(Card card, String state, List<String> excludedRoles) {
        notifyByCard(card, state, excludedRoles, new DefaultMessageGenerator());
    }

    public void notifyByCard(Card card, String state, List<String> excludedRoles, NotificationMatrixMessage.MessageGenerator messageGenerator) {
        String processPath = StringUtils.trimToEmpty(card.getProc().getMessagesPack());

        Map<String, String> matrix = getMatrix(processPath);

        if (matrix == null)
            return;

        Transaction tx = persistence.getTransaction();
        try {
            User currentUser = userSessionSource.getUserSession().getCurrentOrSubstitutedUser();
            List<User> mailList = new ArrayList<>();
            List<User> trayList = new ArrayList<>();
            List<User> smsList = new ArrayList<>();

            Card reloadedCard = reloadCard(card);

            Collection<CardRole> roleList = reloadedCard.getRoles();
            if (roleList != null) {
                for (CardRole cardRole : roleList) {
                    if (!currentUser.equals(cardRole.getUser()) && reloadedCard.getProc().equals(cardRole.getProcRole().getProc())
                            && !excludedRoles.contains(cardRole.getCode())) {
                        notifyUser(card, cardRole, null, matrix, state, mailList, trayList, smsList, messageGenerator);
                    }
                }
            }

            tx.commit();
        } finally {
            tx.end();
        }
    }

    public void notifyByCardAndAssignments(Card card, Map<Assignment, CardRole> assignmentsCardRoleMap, String state) {
        notifyByCardAndAssignments(card, assignmentsCardRoleMap, state, Lists.<String>newArrayList());
    }

    public void notifyByCardAndAssignments(Card card, Map<Assignment, CardRole> assignmentsCardRoleMap, String state,
                                           List<String> extraExcludedRoles) {
        String processPath = StringUtils.trimToEmpty(card.getProc().getMessagesPack());

        Map<String, String> matrix = getMatrix(processPath);

        if (matrix == null)
            return;

        Transaction tx = persistence.getTransaction();
        try {
            User currentUser = userSessionSource.getUserSession().getCurrentOrSubstitutedUser();
            List<User> mailList = new ArrayList<>();
            List<User> trayList = new ArrayList<>();
            List<User> smsList = new ArrayList<>();
            List<String> excludeRoleCodes = new ArrayList<>(extraExcludedRoles);

            NotificationMatrixMessage.MessageGenerator messageGenerator = new DefaultMessageGenerator();

            if (assignmentsCardRoleMap != null) {
                for (Map.Entry<Assignment, CardRole> entry : assignmentsCardRoleMap.entrySet()) {
                    CardRole cardRole = entry.getValue();
                    if (!currentUser.equals(cardRole.getUser()))
                        notifyUser(card, cardRole, entry.getKey(), matrix, state, mailList, smsList, trayList,
                                messageGenerator);

                    excludeRoleCodes.add(cardRole.getCode());
                }
            }

            Card reloadedCard = reloadCard(card);

            Collection<CardRole> roleList = reloadedCard.getRoles();

            if (roleList != null) {
                for (CardRole cardRole : roleList) {
                    if (!currentUser.equals(cardRole.getUser()) && reloadedCard.getProc().equals(
                            cardRole.getProcRole().getProc()) &&
                            !excludeRoleCodes.contains(cardRole.getCode()) &&
                            ((assignmentsCardRoleMap != null && !assignmentsCardRoleMap.containsValue(cardRole)) ||
                                    assignmentsCardRoleMap == null)) {

                        notifyUser(card, cardRole, null, matrix, state, mailList, smsList, trayList, messageGenerator);
                    }
                }
            }

            tx.commit();
        } catch (Exception ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
        } finally {
            tx.end();
        }
    }

    @Override
    public void notifyUser(Card card, String state, User user) {
        notifyUser(card, state, user, new DefaultMessageGenerator());
    }

    @Override
    public void notifyUser(Card card, String state, User user, NotificationMatrixMessage.MessageGenerator messageGenerator) {
        String processPath = StringUtils.trimToEmpty(card.getProc().getMessagesPack());

        Map<String, String> matrix = getMatrix(processPath);

        if (matrix == null)
            return;

        Transaction tx = persistence.getTransaction();
        try {
            List<User> mailList = new ArrayList<>();
            List<User> trayList = new ArrayList<>();
            List<User> smsList = new ArrayList<>();

            Card reloadedCard = reloadCard(card);

            Collection<CardRole> roleList = reloadedCard.getRoles();

            if (roleList != null && user != null) {
                for (CardRole cardRole : roleList) {
                    if (user.equals(cardRole.getUser())) {
                        notifyUser(card, cardRole, null, matrix, state, mailList, trayList, smsList, messageGenerator);
                        break;
                    }
                }
            }

            tx.commit();
        } catch (Exception e) {
            log.error(e);
        } finally {
            tx.end();
        }
    }

    /**
     * Notifies user of given <code>cardRole</code> by notification, specified for <code>state</code>
     * in notification matrix
     *
     * @param state
     * @param cardRole
     */
    @Override
    public void notifyCardRole(Card card, CardRole cardRole, String state, Assignment assignment) {

        try {
            String processPath = StringUtils.trimToEmpty(card.getProc().getMessagesPack());
            Map<String, String> matrix = getMatrix(processPath);
            if (matrix == null)
                return;

            List<User> mailList = new ArrayList<>();
            List<User> trayList = new ArrayList<>();
            List<User> smsList = new ArrayList<>();

            notifyUser(card, cardRole, assignment, matrix, state, mailList, trayList, smsList, new DefaultMessageGenerator());

        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * Simple Factory for create MessageInstance of concreete type,such as FreeMarkerNotificationMessage
     *
     * @param templateType
     * @param text
     * @return new NotificationMessage
     */
    protected NotificationMessageBuilder getNotificationMessageBuilder(String templateType, String text) {
        NotificationMessageBuilder message = null;
        if (templateType.equals("Groovy")) {
            message = new GroovyNotificationMessageBuilder(text);
        }
        if (templateType.equals("Text")) {
            message = new TextNotificationMessageBuilder(text);
        }
        if (templateType.equals("FreeMarker")) {
            message = new FreeMarkerNotificationMessageBuilder(text);
        }
        if (templateType.equals("Script")) {
            message = new ScriptNotificationMessageBuilder(text);
        }
        return message;
    }

    protected String getScriptByNotificationType(String processPath, String type) {
        String notificationScript = processPath.replace('.', '/') + "/%s" + "Notification.groovy";
        if (type.equals(NotificationType.ACTION.toString()) || type.equals(NotificationType.WARNING.toString())) {
            notificationScript = String.format(notificationScript, "Assignment");
        } else if (type.equals(NotificationType.SIMPLE.toString())) {
            notificationScript = String.format(notificationScript, "Observer");
        } else
            notificationScript = String.format(notificationScript, StringUtils.capitalize(type.toLowerCase()));
        return notificationScript;
    }

    private Map<String, String> getMatrix(String processPath) {
        Map<String, String> matrix = cache.get(processPath);
        if (matrix == null) {
            try {
                load(processPath);
            } catch (Exception e) {
                log.warn(String.format("Can't load %s notification matrix", processPath), e);
            }
            matrix = cache.get(processPath);
        }

        return matrix != null ? Collections.unmodifiableMap(matrix) : null;
    }

    protected void createNotificationCardInfo(Card card, Assignment assignment, User user, int cardInfoType, NotificationMatrixMessage message) {
        createNotificationCardInfo(card, user, cardInfoType, message);
    }

    protected CardInfo createNotificationCardInfo(Card card, User user, int cardInfoType, NotificationMatrixMessage message) {
        CardInfo ci = metadata.create(CardInfo.class);
        ci.setType(cardInfoType);
        if (card.isSubProcCard()) {
            ci.setCard(card.getFamilyTop());
            ci.setJbpmExecutionId(card.getProcFamily().getJbpmProcessId());
        } else {
            ci.setCard(card);
            ci.setJbpmExecutionId(card.getProc().getJbpmProcessKey());
        }
        ci.setUser(user);
        ci.setDescription(message.getSubject());
        EntityManager em = persistence.getEntityManager();
        em.persist(ci);
        return ci;
    }

    protected int getCardInfoTypeByState(String type) {
        if (type.endsWith(NotificationType.SIMPLE.toString())) {
            return CardInfo.TYPE_SIMPLE;
        } else if (type.equals(NotificationType.ACTION.toString()) || type.equals(NotificationType.REASSIGN.toString())) {
            return CardInfo.TYPE_NOTIFICATION;
        } else if (type.endsWith(NotificationType.WARNING.toString()) || type.equals(NotificationType.OVERDUE.toString())) {
            return CardInfo.TYPE_OVERDUE;
        }
        return CardInfo.TYPE_SIMPLE;
    }

    private class DefaultMessageGenerator implements NotificationMatrixMessage.MessageGenerator {
        public NotificationMatrixMessage generateMessage(Map<String, Object> parameters) {
            NotificationMatrixMessage message = new NotificationMatrixMessage(null, null);
            Assignment assignment = (Assignment) parameters.get("assignment");
            Card card = (Card) parameters.get("card");
            String script = (String) parameters.get("script");
            if (script != null) {
                //Old mechanism to run Groovy scripts for create message
                try {
                    String scriptStr = resources.getResourceAsString(script);
                    Binding binding = new Binding(parameters);
                    scripting.evaluateGroovy(scriptStr, binding);
                    message.setSubject(binding.getVariable("subject").toString());
                    message.setBody(binding.getVariable("body").toString());
                } catch (Exception e) {
                    log.warn("Unable to get email subject and body, using defaults", e);
                    message.setSubject(String.format("%s: %s - %s",
                            (assignment != null ? "New Assignment" : "Notification"), card.getDescription(), card.getLocState()));
                    message.setBody(String.format("Card %s has become %s", card.getDescription(), card.getLocState()));
                }
            } else {
                //New mechanism to create message for user
                NotificationMessageBuilder messageBuilder = (NotificationMessageBuilder) parameters.get("messagetemplate");
                try {
                    message = messageBuilder.build(parameters);
                } catch (Exception e) {
                    log.warn("Unable to get email subject and body, using defaults", e);
                    message.setSubject(String.format("%s: %s - %s",
                            (assignment != null ? "New Assignment" : "Notification"), card.getDescription(), card.getLocState()));
                    message.setBody(String.format("Card %s has become %s", card.getDescription(), card.getLocState()));
                }
            }

            return message;
        }
    }
}
