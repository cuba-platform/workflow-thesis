Wf.CardPropertyField = function(options) {
    Wf.CardPropertyField.superclass.constructor.call(this, options);
};

YAHOO.lang.extend(Wf.CardPropertyField, inputEx.AutoComplete, {

    clazz: null,

    setOptions: function(options) {
        var oDS = new YAHOO.util.XHRDataSource("action/propertyPath.json");
            // Optional to define fields for single-dimensional array
        oDS.responseType = YAHOO.util.XHRDataSource.TYPE_JSARRAY;
            // Define the schema of the delimited results
        oDS.responseSchema = {
            fields : ["path"]
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

    updateContainer: function(result){
        if (result.responseText!=""){
            var result = YAHOO.lang.JSON.parse(result.responseText);
            if (this.options.container instanceof Wf.CardPropertyContainer){
                this.options.container.updateContainer(result[0]);
            }
        }
        else {
            this.options.container.hideAllFields();
            this.options.container.activeField = null;
            this.options.container.redrawAllContainerWires();
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

    requestAttributeType: function(val){
        if (this.clazz!=null){
            var callback = {
                success: function(o) {
                    this.argument[0].updateContainer(o);
                },
                failure: function(o) {/*failure handler code*/},
                argument: [this]
            };
            YAHOO.util.Connect.asyncRequest('GET', 'action/loadAttributeType.json?path=' + val + '&class=' + this.clazz, callback, null);
        }
    },

    setValue: function(val, sendUpdatedEvt) {
        Wf.CardPropertyField.superclass.setValue.call(this, val, sendUpdatedEvt);
        this.requestAttributeType(val);

    },

});

inputEx.registerType("cardPropertyField", Wf.CardPropertyField);