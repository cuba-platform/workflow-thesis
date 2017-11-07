/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 */

Wf.CardEntityField = function(options) {
    Wf.CardEntityField.superclass.constructor.call(this, options);
};

YAHOO.lang.extend(Wf.CardEntityField, Wf.SelectAutoComplete, {

    clazz: null,

    setOptions: function(options) {
        this.oDS = new YAHOO.util.LocalDataSource([]);
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

    updateValue: function(result, isLoadEvent){
        var value;
        if (result.responseText!=""){
            var result = YAHOO.lang.JSON.parse(result.responseText);
            value = this.createValue(result[0].value, result[0].label);
        } else {
            this.entityId = null;
            value = this.createValue(null, this.el.value);
        }
        this.setValue(value, !isLoadEvent);
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

    requestAttributeType: function(val, sendUpdatedEvt){
        var isLoadEvent = sendUpdatedEvt === false || (Wf.editor && Wf.editor.preventLayerChangedEvent);
        if (this.clazz != null){
            var callback = {
                success: function(o) {
                    this.argument[0].updateValue(o, isLoadEvent);
                },
                failure: function(o) {/*failure handler code*/},
                argument: [this]
            };
            YAHOO.util.Connect.asyncRequest('GET', 'action/loadEntity.json?id=' + val + '&class=' + this.clazz, callback, null);
        }
    },

    setValue: function(val, sendUpdatedEvt) {
        if (val && !(val instanceof Object)) {
            this.requestAttributeType(val, sendUpdatedEvt);
        } else {
            var fieldValue = val;
            this.entityId = null;
            if (val) {
                if (val.fieldValue !== undefined) {
                    fieldValue = Wf.parseHtmlEntities(val.fieldValue);
                }
                if (val.entityId !== undefined) {
                    this.entityId = val.entityId;
                }
            }
            Wf.CardEntityField.superclass.setValue.call(this, fieldValue, sendUpdatedEvt);

            var update = sendUpdatedEvt == false && !(Wf.editor && Wf.editor.preventLayerChangedEvent);
            if (val && update && this.options.container) {
                var value = this.createValue(this.entityId, fieldValue);
                this.options.container.updateValue(value, sendUpdatedEvt);
            }
        }
    },

    getValue: function() {
        var fieldValue = Wf.CardEntityField.superclass.getValue.call(this);
        var propertyValues = this.createValue(this.entityId, fieldValue);
        return propertyValues;
    },

    createValue: function(entityId, fieldValue) {
        if (!entityId && !fieldValue)
            return "";
        var value = {};
        value.entityId = entityId ? entityId : null;
        value.fieldValue = fieldValue ? fieldValue : "";
        value.toString = function() {
            return value.entityId ? value.entityId : value.fieldValue;
        };
        return value;
    },

    clear: function() {
        while(this.oDS.liveData.length > 0) {
            this.oDS.liveData.pop();
        }
    },

    getChoicePosition: function(element) {
        for(var i = 0; i < this.oDS.liveData.length; i++) {
            if (element == this.oDS.liveData[i] || element == this.oDS.liveData[i].value) {
                return i;
            }
        }
        return -1;
    },

    addChoice: function(element) {
        this.oDS.liveData.push(element);
    },

    doSortChoice: function() {
        if (this.oDS.liveData.length > 0) {
            this.oDS.liveData.sort(function(a, b) {
                return (a.label > b.label) - (b.label > a.label);
            })
        }
    },

    setClazz: function(clazz) {
        this.clazz = clazz;
    },

});

inputEx.registerType("cardEntityField", Wf.CardEntityField);
