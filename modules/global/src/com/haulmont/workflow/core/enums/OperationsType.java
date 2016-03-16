/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.enums;

import java.util.Arrays;
import java.util.EnumSet;

/**
 */

public enum OperationsType {
    EQUAL("=", false),
    NOT_EQUAL("<>", false),
    GREATER(">", false),
    GREATER_OR_EQUAL(">=", false),
    LESSER("<", false),
    LESSER_OR_EQUAL("<=", false),
    CONTAINS("like", false),
    DOES_NOT_CONTAIN("not like", false),
    EMPTY("is null", true),
    NOT_EMPTY("is not null", true),
    STARTS_WITH("starts with", false),
    ENDS_WITH("ends with", false);

    private String id;
    private boolean unary;

    OperationsType(String text, boolean unary) {
        this.id = text;
        this.unary = unary;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isUnary() {
        return unary;
    }

    public static OperationsType fromId(String str) {
        for (OperationsType op : values()) {
            if (op.id.equals(str))
                return op;
        }
        return null;
    }

    public static EnumSet<OperationsType> availableOps(AttributeType attributeType) {
        if (AttributeType.STRING == attributeType)
            return EnumSet.of(EQUAL, NOT_EQUAL, CONTAINS, DOES_NOT_CONTAIN, EMPTY, NOT_EMPTY, STARTS_WITH, ENDS_WITH);

        else if (Arrays.asList(AttributeType.DATE, AttributeType.DATE_TIME, AttributeType.INTEGER, AttributeType.DOUBLE).contains(attributeType))
            return EnumSet.of(EQUAL, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESSER, LESSER_OR_EQUAL, EMPTY, NOT_EMPTY);

        else if (AttributeType.BOOLEAN == attributeType)
            return EnumSet.of(EQUAL, NOT_EQUAL, EMPTY, NOT_EMPTY);

        else if (Arrays.asList(AttributeType.ENTITY, AttributeType.ENUM).contains(attributeType))
            return EnumSet.of(EQUAL, NOT_EQUAL, EMPTY, NOT_EMPTY);

        else
            throw new UnsupportedOperationException("Unsupported attribute type: " + attributeType.getId());
    }
}
