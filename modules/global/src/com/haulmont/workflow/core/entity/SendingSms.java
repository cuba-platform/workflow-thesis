/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.workflow.core.enums.SmsStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */

@Entity(name = "wf$SendingSms")
@Table(name = "WF_SENDING_SMS")
@SystemLevel
public class SendingSms extends BaseUuidEntity {

    private static final long serialVersionUID = 8858370316370416179L;

    @Column(name = "PHONE", length = 50)
    private String phone;

    @Column(name = "MESSAGE", length = 255)
    private String message;

    @Column(name = "ERROR_CODE")
    private Integer errorCode;

    @Column(name = "STATUS")
    private Integer status;

    @Column(name = "LAST_CHANGE_DATE")
    private Date lastChangeDate;

    @Column(name = "ATTEMPTS_COUNT")
    private Integer attemptsCount;

    @Column(name = "SMS_ID", length = 255)
    private String smsId;

    @Column(name = "ADDRESSEE", length = 200)
    private String addressee;

    @Column(name = "START_SENDING_DATE")
    private Date startSendingDate;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public SmsStatus getStatus() {
        return SmsStatus.fromId(status);
    }

    public void setStatus(SmsStatus smsStatus) {
        this.status = (smsStatus == null ? null : smsStatus.getId());
    }

    public Date getLastChangeDate() {
        return lastChangeDate;
    }

    public void setLastChangeDate(Date lastChangeDate) {
        this.lastChangeDate = lastChangeDate;
    }

    public Integer getAttemptsCount() {
        return attemptsCount;
    }

    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    public String getSmsId() {
        return smsId;
    }

    public void setSmsId(String smsId) {
        this.smsId = smsId;
    }

    public String getAddressee() {
        return addressee;
    }

    public void setAddressee(String addressee) {
        this.addressee = addressee;
    }

    public Date getStartSendingDate() {
        return startSendingDate;
    }

    public void setStartSendingDate(Date startSendingDate) {
        this.startSendingDate = startSendingDate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(": phone=").append(phone).append(", message=").append(message).append(", smsId=").append(smsId);
        return super.toString() + sb.toString();
    }
}
