/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.global;

import com.haulmont.workflow.core.enums.AttributeType;

/**
 * @author zaharchenko
 * @version $Id$
 */
public interface WfEntityDescriptor {
    String getValue();
    AttributeType getAttributeType();
    String getMetaClassName();
}
