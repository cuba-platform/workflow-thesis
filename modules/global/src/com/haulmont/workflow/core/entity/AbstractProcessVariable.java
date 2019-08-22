/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.workflow.core.enums.AttributeType;
import com.haulmont.workflow.core.global.WfEntityDescriptor;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@MappedSuperclass
public abstract class AbstractProcessVariable extends StandardEntity implements WfEntityDescriptor {
    private static final long serialVersionUID = -8393724105946755178L;

    public static final String VARIABLE_TAG_PATTERN = "<([a-zA-Z0-9]*)>";

    @Column(name = "NAME", length = 100)
    protected String name;

    @Column(name = "MODULE_NAME")
    private String moduleName;

    @Column(name = "PROPERTY_NAME", length = 100)
    protected String propertyName;

    @Column(name = "ALIAS", length = 100)
    protected String alias;

    @Column(name = "VALUE")
    @Lob
    protected String value;

    @Column(name = "ATTRIBUTE_TYPE")
    protected String attributeType;

    @Column(name = "META_CLASS_NAME")
    protected String metaClassName;

    @Column(name = "OVERRIDDEN")
    protected Boolean overridden = Boolean.FALSE;

    @Column(name = "VARIABLE_COMMENT")
    @Lob
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

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.fromId(attributeType);
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType != null ? attributeType.getId() : null;
    }

    @Override
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

    public Set<String> getTagsFromComment() {
        Set<String> tags = new HashSet<>();
        if (StringUtils.isBlank(getComment()))
            return tags;

        Pattern variablePattern = Pattern.compile(VARIABLE_TAG_PATTERN);
        Matcher matcher = variablePattern.matcher(comment);
        while (matcher.find()) {
            tags.add(matcher.group(1));
        }
        return tags;
    }

    public void addTagsToComment(Set<String> tags) {
        Set<String> ownTags = getTagsFromComment();
        ownTags.addAll(tags);
        StringBuilder stringBuilder = new StringBuilder();
        for (String tag : ownTags) {
            stringBuilder.append("<").append(tag).append("> ");
        }
        setComment(stringBuilder.toString() + getCommentWithoutTags());
    }

    public String getCommentWithoutTags() {
        if (StringUtils.isBlank(getComment()))
            return "";
        Pattern variablePattern = Pattern.compile(VARIABLE_TAG_PATTERN);
        Matcher matcher = variablePattern.matcher(getComment());
        String result = matcher.replaceAll("");
        return result.trim();
    }
}