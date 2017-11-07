/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

Wf.ScriptSelect = function(options) {
    Wf.ScriptSelect.superclass.constructor.call(this, options);

    this.refresh(false);
};

YAHOO.lang.extend(Wf.ScriptSelect, inputEx.SelectField, {

    allowVariable: false,
    scriptsLoaded: false,

    setOptions: function(options) {
        Wf.OutputSelect.superclass.setOptions.call(this, options);

        this.options.className = options.className || 'Wf-ScriptSelect';
        this.options.container = options.container;
    },

    setValue: function(val, sendUpdatedEvt) {
        if (!val)
            return;
        this.refresh(true);
        this.newVal = val;
        this.sendUpdateEvt = sendUpdatedEvt;
        Wf.ScriptSelect.superclass.setValue.call(this, val, sendUpdatedEvt);
    },

    refresh: function(useScriptsCache) {
        var i;

        this.clear();
        for (i = 0; i < this.choicesList.length; i++) {
            var choice = this.choicesList[i];
            this.removeChoiceNode(choice.node);
        }
        this.choicesList = [];

        if (useScriptsCache && Wf.scriptsCache) {
            for (var j = 0; j < Wf.scriptsCache.length; j++) {
                var s = Wf.scriptsCache[j];
                var v = {value: s};
                if (this.getChoicePosition(v) == -1)
                    this.addChoice(v);
            }
        } else {
            Wf.loadScripts(this.addScripts, this);
        }
    },

    addScripts: function(scripts) {
        var scriptsLoaded = this.scriptsLoaded;
        this.scriptsLoaded = true;
        Wf.scriptsCache = scripts;
        for (var i = 0; i < scripts.length; i++) {
            var v = {value: scripts[i]};
            if (this.getChoicePosition(v) == -1)
                this.addChoice(v);
        }
        //set value executed early then choice list arrived from server
        if (this.newVal) {
            Wf.ScriptSelect.superclass.setValue.call(this, this.newVal, scriptsLoaded && this.sendUpdatedEvt);
            this.options.container.previousValue = this.newVal;
            this.newVal = null;
        }
    }
});

inputEx.registerType("wfScriptSelect", Wf.ScriptSelect);
