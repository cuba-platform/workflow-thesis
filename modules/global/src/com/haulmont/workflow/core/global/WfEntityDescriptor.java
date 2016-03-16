/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.global;

import com.haulmont.workflow.core.enums.AttributeType;

/**
 */
public interface WfEntityDescriptor {
    String getValue();
    AttributeType getAttributeType();
    String getMetaClassName();
}
