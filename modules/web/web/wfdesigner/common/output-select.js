/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

Wf.OutputSelect = function(options) {
    Wf.OutputSelect.superclass.constructor.call(this, options);
};

YAHOO.lang.extend(Wf.OutputSelect, inputEx.SelectField, {

    setOptions: function(options) {
        Wf.OutputSelect.superclass.setOptions.call(this, options);

        this.options.className = options.className || 'Wf-OutputSelect';
        this.options.container = options.container;
    },

    setContainer: function(container) {
        Wf.OutputSelect.superclass.setContainer.call(this, container);

        if (this.options.container.eventAddOutput) {
            this.options.container.eventAddOutput.subscribe(this.onAddOutput, this, true);
        }
        if (this.options.container.eventDelOutput) {
            this.options.container.eventDelOutput.subscribe(this.onDelOutput, this, true);
        }
        if (this.options.container.eventOutputChanged) {
            this.options.container.eventOutputChanged.subscribe(this.onOutputChanged, this, true);
        }
    },

    onAddOutput: function(e, params) {
        var choice = {value: params[0]};
        if (this.getChoicePosition(choice) == -1)
            this.addChoice(choice);
    },

    onDelOutput: function(e, params) {
        var choice = {value: params[0]};
        if (this.getChoicePosition(choice) >= -1)
            this.removeChoice(choice);
    },

    onOutputChanged: function(e, params) {
    	var currentValue = this.getValue();
    	var itemToChange = params[0];

    	if(itemToChange.prevValue == currentValue) {
    		this.setValue(itemToChange.value, false);
    	} else {
    		this.setValue(this.getValue(), false);
    	}
     },

    setValue: function(val, sendUpdatedEvt) {
        if (!val)
            return;
        this.refresh();
        Wf.OutputSelect.superclass.setValue.call(this, val, sendUpdatedEvt);
    },

    refresh: function() {
        var i;

        this.clear();
        for (i = 0; i < this.choicesList.length; i++) {
            var choice = this.choicesList[i];
            this.removeChoiceNode(choice.node);
        }
        this.choicesList = [];

        if (this.options.container) {
            for (i = 0; i < this.options.container.terminals.length; i++) {
                var terminal = this.options.container.terminals[i];
                if (terminal.alwaysSrc) {
                    this.addChoice({value: terminal.name});
                }
            }
        }
    }

});

inputEx.registerType("wfOutputSelect", Wf.OutputSelect);
