Wf.DateTimeField = function(options) {
    options.dateFormat = 'd/m/Y';
    Wf.DateTimeField.superclass.constructor.call(this, options);
};

YAHOO.lang.extend(Wf.DateTimeField, inputEx.DateTimeField, {

    setOptions: function(options) {
        Wf.DateTimeField.superclass.setOptions.call(this, options);
        this.options.className = options.className || 'Wf-DateTimeField';

    }
});

inputEx.registerType("wfDateTimeField", Wf.DateTimeField);