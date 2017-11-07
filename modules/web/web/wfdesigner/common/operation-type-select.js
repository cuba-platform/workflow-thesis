Wf.OperationTypeSelect = function(options) {
    Wf.OperationTypeSelect.superclass.constructor.call(this, options);
    this.refresh(false);
};

YAHOO.lang.extend(Wf.OperationTypeSelect, inputEx.SelectField, {

    allowVariable: false,
    customChoises: false,

    setOptions: function(options) {
        Wf.OutputSelect.superclass.setOptions.call(this, options);

        this.options.className = options.className || 'Wf-OperationTypeSelect';
        this.options.container = options.container;
    },

    setValue: function(val, sendUpdatedEvt) {
        if (!val)
            return;
        this.refresh(true);
        Wf.OperationTypeSelect.superclass.setValue.call(this, val, sendUpdatedEvt);
        if (this.options.container instanceof Wf.CardPropertyContainer){
            if (this.isVisibleActiveField()) {
                this.options.container.setVisibleActiveField(true);
            } else {
                this.options.container.setVisibleActiveField(false);
            }
        }

    },

    isVisibleActiveField: function() {
        if (this.divEl.style.display == 'none')
            return true;

        var val = this.getValue();
        return "is null" != val && "is not null" != val;
    },

    setCustomChoises: function(customChoises){
        var i;
        this.customChoises = true;

        this.clear();
        for (i = 0; i < this.choicesList.length; i++) {
            var choice = this.choicesList[i];
            this.removeChoiceNode(choice.node);
        }
        this.choicesList = [];
        for (var j = 0; j < customChoises.length; j++) {
            var s = customChoises[j];
            if (this.getChoicePosition(s) == -1)
                this.addChoice(s);
        }
    },

    refresh: function(useCache) {
        if (this.customChoises){
            return;
        }
        var i;

        this.clear();
        for (i = 0; i < this.choicesList.length; i++) {
            var choice = this.choicesList[i];
            this.removeChoiceNode(choice.node);
        }
        this.choicesList = [];

        if (useCache && Wf.operationTypesCache ) {
            for (var j = 0; j < Wf.operationTypesCache.length; j++) {
                var s = Wf.operationTypesCache[j];
                if (this.getChoicePosition(s) == -1)
                    this.addChoice(s);
            }
        } else {
            Wf.loadOperationTypes(this.addStatuses, this);
        }
    },

    addStatuses: function(templates) {
        Wf.operationTypesCache = templates;
        for (var i = 0; i < templates.length; i++) {
            var v = templates[i];
            if (this.getChoicePosition(v) == -1)
                this.addChoice(v);
        }
    }
});

inputEx.registerType("operationTypeSelect", Wf.OperationTypeSelect);