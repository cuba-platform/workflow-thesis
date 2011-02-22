/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Gennady Pavlov
 * Created: 01.07.2010 13:40:36
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.app.EmailerAPI;
import com.haulmont.cuba.core.global.ConfigProvider;
import com.haulmont.cuba.core.global.EmailException;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.global.ScriptingProvider;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardInfo;
import com.haulmont.workflow.core.entity.CardRole;
import groovy.lang.Binding;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.annotation.ManagedBean;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ManagedBean(NotificationMatrixAPI.NAME)
public class NotificationMatrix implements NotificationMatrixMBean, NotificationMatrixAPI {

    static final String MAIL_SHEET = "Mail";
    static final String TRAY_SHEET = "Tray";
    static final String ROLES_SHEET = "Roles";
    static final String STATES_SHEET = "States";
    static final String ACTIONS_SHEET = "Actions";

    private static Log log = LogFactory.getLog(NotificationMatrixService.class);

    private Map<String, Map<String, NotificationType>> cache = new ConcurrentHashMap<String, Map<String, NotificationType>>();
    private Map<String, Map<NotificationType, NotificationMessageBuilder>> messageCache = new ConcurrentHashMap<String, Map<NotificationType, NotificationMessageBuilder>>();

    private Map<String, String> readRoles(HSSFWorkbook hssfWorkbook) {
        HSSFSheet sheet = hssfWorkbook.getSheet(ROLES_SHEET);
        Map<String, String> rolesMap = new HashMap();
        for (Iterator it = sheet.rowIterator(); it.hasNext();) {
            HSSFRow row = (HSSFRow) it.next();

            HSSFCell cell = row.getCell(0);
            HSSFCell cellCode = row.getCell(1);

            String role = null;
            String code = null;

            if (HSSFCell.CELL_TYPE_STRING == cell.getCellType()) {
                code = cell.getRichStringCellValue().getString();
            }

            if (HSSFCell.CELL_TYPE_STRING == cellCode.getCellType()) {
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

        return rolesMap;
    }

    private Map<String, String> readStates(HSSFWorkbook hssfWorkbook) {
        HSSFSheet sheet = hssfWorkbook.getSheet(STATES_SHEET);
        Map<String, String> statesMap = new HashMap<String, String>();
        for (Iterator it = sheet.rowIterator(); it.hasNext();) {
            HSSFRow row = (HSSFRow) it.next();

            HSSFCell cell = row.getCell(0);
            HSSFCell cellCode = row.getCell(1);

            String state = null;
            String code = null;

            if (HSSFCell.CELL_TYPE_STRING == cell.getCellType()) {
                code = cell.getRichStringCellValue().getString();
            }

            if (HSSFCell.CELL_TYPE_STRING == cell.getCellType()) {
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
     * @param hssfWorkbook
     * @return true, if xls file have templeates or groovy scripts
     */
    private void loadMessages(HSSFWorkbook hssfWorkbook, String processPath) {
        HSSFSheet sheet = hssfWorkbook.getSheet(ACTIONS_SHEET);
        for (Iterator it = sheet.rowIterator(); it.hasNext();) {
            HSSFRow row = (HSSFRow) it.next();

            HSSFCell cellNotifType = row.getCell(0);
            HSSFCell cellTempleateType = row.getCell(1);
            HSSFCell cellText = row.getCell(2);

            if ((cellNotifType == null) || (cellTempleateType == null) || (cellText == null)) {
                return;
            }

            String notificationType = null;
            String templeateType = null;
            String text = null;

            if (HSSFCell.CELL_TYPE_STRING == cellNotifType.getCellType()) {
                notificationType = cellNotifType.getStringCellValue();
            }

            if (HSSFCell.CELL_TYPE_STRING == cellTempleateType.getCellType()) {
                templeateType = cellTempleateType.getStringCellValue();
            }

            if (HSSFCell.CELL_TYPE_STRING == cellText.getCellType()) {
                text = cellText.getStringCellValue();
            }

            notificationType = StringUtils.trimToEmpty(notificationType);
            templeateType = StringUtils.trimToEmpty(templeateType);

            if (!StringUtils.isEmpty(notificationType) && !StringUtils.isEmpty(templeateType) && !StringUtils.isEmpty(text)) {
                NotificationMessageBuilder message = getNotificationMessageBuilder(templeateType, text);
                Map<NotificationType, NotificationMessageBuilder> map = messageCache.get(processPath);
                NotificationType type = NotificationType.fromId(notificationType);

                if (type != null) {
                    map.put(type, message);
                }

            } else {
                log.error(String.format("NotificationType %s or TempleateType %s or ScriptText %s is missing or incorrect", notificationType, templeateType, text));
                return;
            }

        }

    }

    private boolean fillMatrixBySheet(Map<String, NotificationType> matrix, String sheetName, HSSFWorkbook matrixTemplate,
                                      Map<String, String> rolesMap, Map<String, String> statesMap) {
        HSSFSheet sheet = matrixTemplate.getSheet(sheetName);
        if (sheet == null) {
            return false;
        }

        HSSFRow statesRow = sheet.getRow(1);

        for (int i = 2; i < sheet.getLastRowNum(); i++) {
            HSSFRow row = sheet.getRow(i);

            if (row == null) {
                continue;
            }

            HSSFCell roleCell = row.getCell(0);
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
                HSSFCell cell = row.getCell(j);

                if (cell == null) {
                    continue;
                }

                String value = cell.getRichStringCellValue().toString();
                if ("".equals(value)) {
                    continue;
                }

                NotificationType type = NotificationType.fromId(value);
                if (type != null && !NotificationType.NO.equals(type)) {
                    String state = statesRow.getCell(j).getRichStringCellValue().getString();

                    String codeState = statesMap.get(StringUtils.trimToEmpty(state));

                    if (codeState != null) {
                        if (codeState.contains(",")) {
                            StringTokenizer tokenizer = new StringTokenizer(codeState, ",");
                            while (tokenizer.hasMoreTokens()) {
                                String s = StringUtils.trimToEmpty(tokenizer.nextToken());
                                if (!"".equals(s)) {
                                    String key = s + '_' + codeAddresseeRole + "_" + sheetName;
                                    matrix.put(key, type);
                                }
                            }
                        } else {
                            String key = codeState + '_' + codeAddresseeRole + "_" + sheetName;
                            matrix.put(key, type);
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
        EntityManager em = PersistenceProvider.getEntityManager();
        Card reloadedCard = em.find(Card.class, card.getId());
        if (reloadedCard == null)
            throw new RuntimeException(String.format("Card not found: %s", card.getId()));

        return reloadedCard;
    }

    private Assignment reloadAssignment(Assignment assignment) {
        EntityManager em = PersistenceProvider.getEntityManager();
        Assignment reloadedAssignment = em.find(Assignment.class, assignment.getId());
        if (reloadedAssignment == null)
            throw new RuntimeException(String.format("Assignment not found: %s", assignment.getId()));

        return reloadedAssignment;
    }

    private synchronized void load(String processPath) throws Exception {
        Map<String, NotificationType> matrix = cache.get(processPath);
        if (matrix != null)
            return;

        String confDir = ConfigProvider.getConfig(GlobalConfig.class).getConfDir();
        File file = new File(confDir + "/" + processPath.replace('.', '/') + "/" + "notification.xls");
        if (!file.exists())
            return;

        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(new FileInputStream(file));

        Map<String, String> rolesMap = readRoles(hssfWorkbook);
        Map<String, String> statesMap = readStates(hssfWorkbook);

        matrix = new HashMap<String, NotificationType>();
        boolean mailMatrixFilled = fillMatrixBySheet(matrix, MAIL_SHEET, hssfWorkbook, rolesMap, statesMap);
        boolean trayMatrixFilled = fillMatrixBySheet(matrix, TRAY_SHEET, hssfWorkbook, rolesMap, statesMap);

        messageCache.put(processPath, new HashMap<NotificationType, NotificationMessageBuilder>());
        loadMessages(hssfWorkbook, processPath);

        if (mailMatrixFilled || trayMatrixFilled)
            cache.put(processPath, matrix);
    }


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

    private void notifyUser(Card card, CardRole cardRole, Assignment assignment, Map<String, NotificationType> matrix, String state,
                            List<User> mailList, List<User> trayList, NotificationMatrixMessage.MessageGenerator messageGenerator) {
        String processPath = StringUtils.trimToEmpty(card.getProc().getMessagesPack());
        NotificationType type;
        String key = state + "_" + cardRole.getCode();
        final User user = cardRole.getUser();

        Map variables = new HashMap();
        variables.put("assignment", assignment);
        variables.put("card", card);
        variables.put("user", user);
        variables.put("cardRole", cardRole);

        //email
        if (StringUtils.trimToNull(user.getEmail()) != null && !mailList.contains(user) &&
                BooleanUtils.isTrue(cardRole.getNotifyByEmail()) &&
                ((type = matrix.get(key + "_" + MAIL_SHEET)) != null)) {
            NotificationMessageBuilder notificationMessage = messageCache.get(processPath).get(type);
            if (notificationMessage != null) {
                variables.put("messagetemplate", notificationMessage);
            } else {
                variables.put("script", getScriptByNotificationType(processPath, type));
            }
            final NotificationMatrixMessage message = messageGenerator.generateMessage(variables);

            new Thread() {
                @Override
                public void run() {
                    EmailerAPI emailer = Locator.lookup(EmailerAPI.NAME);
                    try {
                        emailer.sendEmail(user.getEmail(), message.getSubject(), message.getBody());
                    } catch (EmailException e) {
                        log.warn(e);
                    }
                }
            }.start();

            mailList.add(user);
        }

        //tray && notificationPanel
        if (!trayList.contains(user) && BooleanUtils.isTrue(cardRole.getNotifyByCardInfo()) &&
                ((type = matrix.get(key + "_" + TRAY_SHEET)) != null)) {
            variables.put("script", getScriptByNotificationType(processPath, type));
            createNotificationCardInfo(card, assignment, cardRole.getUser(), getCardInfoTypeByState(type), messageGenerator.generateMessage(variables));
            trayList.add(user);
        }
    }

    public void notifyByCard(Card card, String state) {
        notifyByCard(card, state, Collections.<String>emptyList());
    }

    public void notifyByCard(Card card, String state, List<String> excludedRoles) {
        notifyByCard(card, state, Collections.<String>emptyList(), new DefaultMessageGenerator());
    }

    public void notifyByCard(Card card, String state, List<String> excludedRoles, NotificationMatrixMessage.MessageGenerator messageGenerator) {
        String processPath = StringUtils.trimToEmpty(card.getProc().getMessagesPack());

        Map<String, NotificationType> matrix = getMatrix(processPath);

        if (matrix == null)
            return;

        Transaction tx = Locator.getTransaction();
        try {
            User currentUser = SecurityProvider.currentUserSession().getCurrentOrSubstitutedUser();
            List<User> mailList = new ArrayList<User>();
            List<User> trayList = new ArrayList<User>();

            Card reloadedCard = reloadCard(card);

            Collection<CardRole> roleList = reloadedCard.getRoles();
            if (roleList == null || roleList.isEmpty())
                return;

            for (CardRole cardRole : roleList) {
                if (!currentUser.equals(cardRole.getUser()) && reloadedCard.getProc().equals(cardRole.getProcRole().getProc())
                        && !excludedRoles.contains(cardRole.getCode())) {
                    notifyUser(card, cardRole, null, matrix, state, mailList, trayList, messageGenerator);
                }
            }

            tx.commit();
        } finally {
            tx.end();
        }
    }

    public void notifyByCardAndAssignments(Card card, Map<Assignment, CardRole> assignmentsCardRoleMap, String state) {
        String processPath = StringUtils.trimToEmpty(card.getProc().getMessagesPack());

        Map<String, NotificationType> matrix = getMatrix(processPath);

        if (matrix == null)
            return;

        Transaction tx = Locator.getTransaction();
        try {
            User currentUser = SecurityProvider.currentUserSession().getCurrentOrSubstitutedUser();
            List<User> mailList = new ArrayList<User>();
            List<User> trayList = new ArrayList<User>();
            List<String> excludeRoleCodes = new ArrayList<String>();

            NotificationMatrixMessage.MessageGenerator messageGenerator = new DefaultMessageGenerator();

            if (assignmentsCardRoleMap != null) {
                for (Map.Entry<Assignment, CardRole> entry : assignmentsCardRoleMap.entrySet()) {
                    CardRole cardRole = entry.getValue();
                    if (!currentUser.equals(cardRole.getUser()))
                        notifyUser(card, cardRole, entry.getKey(), matrix, state, mailList, trayList, messageGenerator);

                    excludeRoleCodes.add(cardRole.getCode());
                }
            }

            Card reloadedCard = reloadCard(card);

            Collection<CardRole> roleList = reloadedCard.getRoles();
            if (roleList == null || roleList.isEmpty())
                return;

            for (CardRole cardRole : roleList) {
                if (!currentUser.equals(cardRole.getUser()) && reloadedCard.getProc().equals(cardRole.getProcRole().getProc()) &&
                        !excludeRoleCodes.contains(cardRole.getCode()) &&
                        ((assignmentsCardRoleMap != null && !assignmentsCardRoleMap.containsValue(cardRole)) || assignmentsCardRoleMap == null)) {

                    notifyUser(card, cardRole, null, matrix, state, mailList, trayList, messageGenerator);
                }
            }

            tx.commit();
        } finally {
            tx.end();
        }
    }

    /**
     * Simple Factory for create MessageInstance of concreete type,such as FreeMarkerNotificationMessage
     *
     * @param templateType
     * @param text
     * @return new NotificationMessage
     */
    private NotificationMessageBuilder getNotificationMessageBuilder(String templateType, String text) {
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
        return message;
    }

    private String getScriptByNotificationType(String processPath, NotificationType type) {
        String notificationScript = processPath.replace('.', '/') + "/%s" + "Notification.groovy";
        if (type.equals(NotificationType.ACTION) || type.equals(NotificationType.WARNING)) {
            notificationScript = String.format(notificationScript, "Assignment");
        }
        if (type.equals(NotificationType.SIMPLE)) {
            notificationScript = String.format(notificationScript, "Observer");
        }
        return notificationScript;
    }

    private Map<String, NotificationType> getMatrix(String processPath) {
        Map<String, NotificationType> matrix = cache.get(processPath);
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

    private void createNotificationCardInfo(Card card, Assignment assignment, User user, int cardInfoType, NotificationMatrixMessage message) {
        CardInfo ci = new CardInfo();
        ci.setType(cardInfoType);
        ci.setCard(card);
        ci.setUser(user);
//        ci.setActivity((assignment == null) ? null : assignment.getName());
        ci.setJbpmExecutionId(card.getProc().getJbpmProcessKey());
        ci.setDescription(message.getSubject());

        EntityManager em = PersistenceProvider.getEntityManager();
        em.persist(ci);
    }

    private int getCardInfoTypeByState(NotificationType type) {
        if (type.equals(NotificationType.SIMPLE)) {
            return CardInfo.TYPE_SIMPLE;
        } else if (type.equals(NotificationType.ACTION)) {
            return CardInfo.TYPE_NOTIFICATION;
        } else if (type.equals(NotificationType.WARNING)) {
            return CardInfo.TYPE_OVERDUE;
        }
        return CardInfo.TYPE_NOTIFICATION;
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
                    Binding binding = new Binding(parameters);
                    ScriptingProvider.runGroovyScript(script, binding);
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
                NotificationMessageBuilder notificationMessage = (NotificationMessageBuilder) parameters.get("messagetemplate");
                notificationMessage.setParameters(parameters);
                try {
                    message.setSubject(notificationMessage.getSubject());
                    message.setBody(notificationMessage.getBody());
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
