/**
 *
 * <p>$Id$</p>
 *
 * @author Zaharchenko
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.workflow.core.enums.AttributeType;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractProcessVariable extends StandardEntity {
    private static final long serialVersionUID = -8393724105946755178L;

    @Column(name = "NAME", length = 100)
    protected String name;

    @Column(name = "MODULE_NAME")
    private String moduleName;

    @Column(name = "PROPERTY_NAME", length = 100)
    protected String propertyName;

    @Column(name = "ALIAS", length = 100)
    protected String alias;

    @Column(name = "VALUE", length = 0)
    protected String value;

    @Column(name = "ATTRIBUTE_TYPE")
    protected String attributeType;

    @Column(name = "META_CLASS_NAME")
    protected String metaClassName;

    @Column(name = "OVERRIDDEN")
    protected Boolean overridden;

    @Column(name = "VARIABLE_COMMENT")
    protected String comment;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AttributeType getAttributeType() {
        return AttributeType.fromId(attributeType);
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType != null ? attributeType.getId() : null;
    }

    public String getMetaClassName() {
        return metaClassName;
    }

    public void setMetaClassName(String metaClassName) {
        this.metaClassName = metaClassName;
    }


    public Boolean getOverridden() {
        return overridden;
    }

    public void setOverridden(Boolean overridden) {
        this.overridden = overridden;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public final AbstractProcessVariable copyTo(AbstractProcessVariable to) {
        to.setName(getName());
        to.setAlias(getAlias());
        to.setValue(getValue());
        to.setAttributeType(getAttributeType());
        to.setMetaClassName(getMetaClassName());
        to.setPropertyName(getPropertyName());
        to.setModuleName(getModuleName());
        to.setComment(getComment());
        return to;
    }
}
