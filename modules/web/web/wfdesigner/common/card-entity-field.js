/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * @author mishunin
 * @version $Id$
 */

Wf.CardEntityField = function(options) {
    Wf.CardEntityField.superclass.constructor.call(this, options);
};

YAHOO.lang.extend(Wf.CardEntityField, Wf.SelectAutoComplete, {

    clazz: null,

    setOptions: function(options) {
        this.oDS = new YAHOO.util.LocalDataSource([]);
        this.oDS.responseType =
        this.oDS.responseSchema = {fields : ["value", "label"]};
        options.datasource = this.oDS;

        options.autoComp = new Object();
        options.autoComp.mainField = this;
        options.autoComp.maxResultsDisplayed = options.maxResultsDisplayed ? options.maxResultsDisplayed : 10000;
        options.autoComp.animVert = false;

        Wf.CardEntityField.superclass.setOptions.call(this, options);
        this.options.className = options.className || 'Wf-CardEntityField';

    },

    buildAutocomplete: function() {
        Wf.CardEntityField.superclass.buildAutocomplete.call(this);

        if (this.oAutoComp) {
            this.baseFormatResult = this.oAutoComp.formatResult;
            this.oAutoComp.formatResult = this.formatResult;
        }
    },

    formatResult: function(oResultData, sQuery, sResultMatch) {
        var sMarkup = (oResultData && oResultData[1]) ? oResultData[1] : sResultMatch;
        return sMarkup ? sMarkup : "";
    },

    itemSelectHandler: function(sType, aArgs) {
        var aData = aArgs[2];
        if (this.options.returnValue) {
            this.setValue(this.options.returnValue(aData));
        } else {
            var value = {};
            value.entityId = aData[0];
            value.fieldValue = aData[1];
            this.setValue(value);
        }
    },

    updateValue: function(result){
        if (result.responseText!=""){
            var result = YAHOO.lang.JSON.parse(result.responseText);
            var value = {};
            value.entityId = result[0].value;
            value.fieldValue = result[0].label;
            this.setValue(value);
        }
        else {
            this.entityId = null;
        }
    },

    setContainer: function(container) {
         this.options.container = container;
    },

    onChange: function(e){
	    if (this.hiddenEl.value != this.el.value) {
	        this.requestAttributeType(this.el.value);
	    }
	    Wf.CardEntityField.superclass.onChange.call(this, e);
    },

    requestAttributeType: function(val){
        if (this.clazz != null){
            var callback = {
                success: function(o) {
                    this.argument[0].updateValue(o);
                },
                failure: function(o) {/*failure handler code*/},
                argument: [this]
            };
            YAHOO.util.Connect.asyncRequest('GET', 'action/loadEntity.json?id=' + val + '&class=' + this.clazz, callback, null);
        }
    },

    setValue: function(val, sendUpdatedEvt) {
        if (val && !(val instanceof Object)) {
            this.requestAttributeType(val);
        } else {
            var fieldValue = val;
            this.entityId = "";
            if (val.fieldValue !== undefined) {
                fieldValue = Wf.parseHtmlEntities(val.fieldValue);
            }
            if (val.entityId !== undefined) {
                this.entityId = val.entityId;
            }
            Wf.CardEntityField.superclass.setValue.call(this, fieldValue, sendUpdatedEvt);
        }
    },

    getValue: function() {
        var fieldValue = Wf.CardEntityField.superclass.getValue.call(this);
        var propertyValues = {};
        propertyValues.fieldValue = fieldValue;
        if (this.entityId) {
            propertyValues.entityId = this.entityId;
        } else {
            propertyValues.entityId = "";
        }
        propertyValues.toString = function() {
            return this.entityId;
        };
        return propertyValues;
    },

    clear: function() {
        while(this.choicesList.length > 0) {
            this.oDS.liveData.pop();
        }
    },

    getChoicePosition: function(element) {
        for(var i = 0; i < this.oDS.liveData.length; i++) {
            if (element == this.oDS.liveData[i]) {
                return i;
            }
        }
        return -1;
    },

    addChoice: function(element) {
        this.oDS.liveData.push(element);
    },

    setClazz: function(clazz) {
        this.clazz = clazz;
    },

});

inputEx.registerType("cardEntityField", Wf.CardEntityField);
