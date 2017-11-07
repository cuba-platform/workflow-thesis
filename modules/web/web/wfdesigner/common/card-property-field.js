Wf.CardPropertyField = function(options) {
    Wf.CardPropertyField.superclass.constructor.call(this, options);
};

YAHOO.lang.extend(Wf.CardPropertyField, Wf.SelectAutoComplete, {

    clazz: null,

    setOptions: function(options) {
        var oDS = new YAHOO.util.XHRDataSource("action/propertyPath.json");
            // Optional to define fields for single-dimensional array
        oDS.responseType = YAHOO.util.XHRDataSource.TYPE_JSARRAY;
            // Define the schema of the delimited results
        oDS.responseSchema = {
            fields : ["path", "property", "locProperty", "isEntity"]
        };

        oDS.maxCacheEntries = 20;

        options.autoComp = new Object();
        options.autoComp.mainField = this;
        options.autoComp.queryDelay = .5;
        options.autoComp.maxResultsDisplayed = 150;
        options.autoComp.animVert = false;
        options.autoComp.generateRequest = function(sQuery) {
            var clazz = this.mainField.clazz;
            return "?query=" + sQuery + '&class=' + clazz ;
        };
        options.datasource = oDS;

        Wf.CardPropertyField.superclass.setOptions.call(this, options);
        this.options.className = options.className || 'Wf-CardPropertyField';

    },

    buildAutocomplete: function() {
        Wf.CardPropertyField.superclass.buildAutocomplete.call(this);

        if (this.oAutoComp) {
            this.baseFormatResult = this.oAutoComp.formatResult;
            this.oAutoComp.formatResult = this.formatResult;
        }
    },

    onClickPopup: function() {
        if (this.oAutoComp._elContent && this.oAutoComp._elContent.style["display"] != "none") {
            this.oAutoComp._toggleContainer(false);
        } else {
            var fieldValue = this.el.value;
            if (/\./.test(fieldValue)) {
                var path = fieldValue.slice(0,fieldValue.lastIndexOf(".") + 1);
                this.oAutoComp.sendQuery(path);
            } else {
                this.oAutoComp.sendQuery("");
            }
        }
    },

    formatResult: function(oResultData, sQuery, sResultMatch) {
        var sMarkup = (oResultData && oResultData[2]) ? oResultData[2] : sResultMatch;
        if (sMarkup && oResultData[3]) {
            sMarkup = '\u25B6 ' + sMarkup;
        }
        return sMarkup ? sMarkup : "";
    },

    itemSelectHandler: function(sType, aArgs) {
        var aData = aArgs[2];
        if (this.options.returnValue) {
            this.setValue(this.options.returnValue(aData));
        } else {
            var value = {};
            var path = (aData[0] && aData[0] != "") ? (aData[0] + ".") : "";
            value.systemPropertyValue = path + aData[1];
            value.fieldValue = path + aData[2];
            this.setValue(value);
        }
    },

    updateContainer: function(result, isLoadEvent){
        if (result.responseText!=""){
            var result = YAHOO.lang.JSON.parse(result.responseText);
            this.systemPropertyPath = result[0].systemPropertyPath;
            if (this.options.container instanceof Wf.CardPropertyContainer){
                this.options.container.updateContainer(result[0], isLoadEvent);
            }
        }
        else {
            this.options.container.hideAllFields();
            this.options.container.activeField = null;
            this.options.container.redrawAllContainerWires();
            this.systemPropertyPath = null;
        }
    },

    setContainer: function(container) {
         this.options.container = container;
    },

    onChange: function(e){
	    if (this.hiddenEl.value != this.el.value) {
	        this.requestAttributeType(this.el.value);
	    }
	    Wf.CardPropertyField.superclass.onChange.call(this, e);
    },

    requestAttributeType: function(val, sendUpdatedEvt){
        var isLoadEvent = sendUpdatedEvt === false || (Wf.editor && Wf.editor.preventLayerChangedEvent);
        if (this.clazz!=null){
            var callback = {
                success: function(o) {
                    this.argument[0].updateContainer(o, isLoadEvent);
                },
                failure: function(o) {/*failure handler code*/},
                argument: [this]
            };
            YAHOO.util.Connect.asyncRequest('GET', 'action/loadAttributeType.json?path=' + val + '&class=' + this.clazz, callback, null);
        }
    },

    setValue: function(val, sendUpdatedEvt) {
        var fieldValue = val;
        var systemPropertyPath = val;
        if (val) {
            if (val.fieldValue) {
                fieldValue = val.fieldValue;
                systemPropertyPath = fieldValue;
            }
            if (val.systemPropertyValue) {
                systemPropertyPath = val.systemPropertyValue;
            } else {
                systemPropertyPath = fieldValue;
            }
        }
        Wf.CardPropertyField.superclass.setValue.call(this, fieldValue, sendUpdatedEvt);
        this.requestAttributeType(systemPropertyPath, sendUpdatedEvt);
    },

    getValue: function() {
        var fieldValue = Wf.CardPropertyField.superclass.getValue.call(this);
        var propertyValues = {};
        propertyValues.fieldValue = fieldValue;
        if (this.systemPropertyPath) {
            propertyValues.systemPropertyValue = this.systemPropertyPath;
        } else {
            propertyValues.systemPropertyValue = fieldValue;
        }
        propertyValues.toString = function() {
            return this.systemPropertyValue;
        };
        return propertyValues;
    }

});

inputEx.registerType("cardPropertyField", Wf.CardPropertyField);