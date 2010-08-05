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

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Locator;
import com.haulmont.cuba.core.PersistenceProvider;
import com.haulmont.cuba.core.Transaction;
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
import java.io.FileInputStream;
import java.util.*;

@ManagedBean(NotificationMatrixAPI.NAME)
public class NotificationMatrix implements NotificationMatrixMBean, NotificationMatrixAPI {

    static final String MAIL_SHEET = "Mail";
    static final String TRAY_SHEET = "Tray";
    static final String ROLES_SHEET = "Roles";
    static final String STATES_SHEET = "States";

    private static Log log = LogFactory.getLog(NotificationMatrixService.class);

    private volatile Map<String, Map<String, NotificationType>> cache = new HashMap<String, Map<String, NotificationType>>();

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

            if ("".equals(code) || "".equals(role)) {
                throw new RuntimeException(String.format("code %s or role %s must not be empty", code, role));
            }

            rolesMap.put(code, role);
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

            if ("".equals(code) || "".equals(state)) {
                throw new RuntimeException(String.format("code %s or state %s must not be empty", code, state));
            }

            statesMap.put(code, state);
        }
        return statesMap;
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
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(new FileInputStream(confDir + "/" + processPath.replace('.', '/') + "/" + "notification.xls"));

        Map<String, String> rolesMap = readRoles(hssfWorkbook);
        Map<String, String> statesMap = readStates(hssfWorkbook);

        matrix = new HashMap<String, NotificationType>();
        boolean mailMatrixFilled = fillMatrixBySheet(matrix, MAIL_SHEET, hssfWorkbook, rolesMap, statesMap);
        boolean trayMatrixFilled = fillMatrixBySheet(matrix, TRAY_SHEET, hssfWorkbook, rolesMap, statesMap);

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

    private void notifyUser(Card card, CardRole cardRole, Assignment assignment, Map<String, NotificationType> matrix, String state) {
        String processPath = StringUtils.trimToEmpty(card.getProc().getMessagesPack());
        NotificationType type;
        String key = state + "_" + cardRole.getCode();

        if (BooleanUtils.isTrue(cardRole.getNotifyByEmail()) &&
                ((type = matrix.get(key + "_" + MAIL_SHEET)) != null)) {
            sendEmail(card, assignment, cardRole.getUser(), getScriptByNotificationType(processPath, type));
        }

        if (BooleanUtils.isTrue(cardRole.getNotifyByCardInfo()) &&
                ((type = matrix.get(key + "_" + TRAY_SHEET)) != null)) {
            createNotificationCardInfo(card, assignment, cardRole.getUser(), getScriptByNotificationType(processPath, type));
        }
    }

    public void notifyByCard(Card card, String state) {
        notifyByCard(card, state, null);
    }

    public void notifyByCard(Card card, String state, String excludedRole) {
        String processPath = StringUtils.trimToEmpty(card.getProc().getMessagesPack());

        Map<String, NotificationType> matrix = getMatrix(processPath);

        if (matrix == null)
            return;

        Transaction tx = Locator.getTransaction();
        try {
            Card reloadedCard = reloadCard(card);

            Collection<CardRole> roleList = reloadedCard.getRoles();
            if (roleList == null || roleList.isEmpty())
                return;

            for (CardRole cardRole : roleList) {
                if (reloadedCard.getProc().equals(cardRole.getProcRole().getProc()) && !cardRole.getCode().equals(excludedRole)) {
                    notifyUser(card, cardRole, null, matrix, state);
                }
            }

            tx.commit();
        } finally {
            tx.end();
        }
    }

    public void notifyByAssignment(Assignment assignment, CardRole cardRole, String state) {
        if (cardRole == null) {
            return;
        }

        String processPath = StringUtils.trimToEmpty(assignment.getProc().getMessagesPack());

        Map<String, NotificationType> matrix = getMatrix(processPath);

        if (matrix == null)
            return;

        Transaction tx = Locator.getTransaction();
        try {
            Assignment reloadAssignment = reloadAssignment(assignment);

            notifyUser(reloadAssignment.getCard(), cardRole, reloadAssignment, matrix, state);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    private String getScriptByNotificationType(String processPath, NotificationType type) {
        String notificationScript = processPath.replace('.', '/') + "/%s" + "Notification.groovy";
        if (type.equals(NotificationType.ACTION)) {
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

    private void sendEmail(Card card, Assignment assignment, final User user, String notificationScript) {
        String subject;
        String body;

        if (user.getEmail() == null)
            return;

        try {
            Map variables = new HashMap();
            variables.put("assignment", assignment);
            variables.put("card", card);
            variables.put("user", user);
            Binding binding = new Binding(variables);
            ScriptingProvider.runGroovyScript(notificationScript, binding);
            subject = binding.getVariable("subject").toString();
            body = binding.getVariable("body").toString();
        } catch (Exception e) {
            log.warn("Unable to get email subject and body, using defaults", e);
            subject = String.format("%s: %s - %s",
                    (assignment != null ? "New Assignment" : "Notification"), card.getDescription(), card.getLocState());
            body = String.format("Card %s has become %s", card.getDescription(), card.getLocState());
        }

        final String finalSubject = subject;
        final String finalBody = body;
        new Thread() {
            @Override
            public void run() {
                EmailerAPI emailer = Locator.lookup(EmailerAPI.NAME);
                try {
                    emailer.sendEmail(user.getEmail(), finalSubject, finalBody);
                } catch (EmailException e) {
                    log.warn(e);
                }
            }
        }.start();
    }

    private void createNotificationCardInfo(Card card, Assignment assignment, User user, String notificationScript) {
        CardInfo ci = new CardInfo();
        ci.setType(CardInfo.TYPE_NOTIFICATION);
        ci.setCard(card);
        ci.setUser(user);
        ci.setActivity(card.getState());
        ci.setJbpmExecutionId(card.getProc().getJbpmProcessKey());

        String subject = getNotificationSubject(card, assignment, user, notificationScript);
        ci.setDescription(subject);

        EntityManager em = PersistenceProvider.getEntityManager();
        em.persist(ci);
    }

    private String getNotificationSubject(Card card, Assignment assignment, User user, String notificationScript) {
        String subject;
        try {
            Map variables = new HashMap();
            variables.put("assignment", assignment);
            variables.put("card", card);
            variables.put("user", user);
            Binding binding = new Binding(variables);
            ScriptingProvider.runGroovyScript(notificationScript, binding);
            subject = binding.getVariable("subject").toString();
        } catch (Exception e) {
            log.warn("Unable to get notification text, using defaults", e);
            subject = String.format("%s: %s - %s",
                    (assignment != null ? "New Assignment" : "Notification"), card.getDescription(), card.getLocState());
        }
        return subject;
    }
}
