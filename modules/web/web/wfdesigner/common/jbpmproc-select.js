/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

Wf.JbpmProcSelect = function (options) {
    Wf.JbpmProcSelect.superclass.constructor.call(this, options);
    this.refresh();
};

YAHOO.lang.extend(Wf.JbpmProcSelect, inputEx.SelectField, {

    setOptions:function (options) {
        Wf.OutputSelect.superclass.setOptions.call(this, options);
        this.options.className = options.className || 'Wf-JbpmProcSelect';
        this.options.container = options.container;
    },

    setValue:function (val, sendUpdatedEvt) {
        if (!val)
            return;
        this.refresh();
        this.newVal = val;
        this.sendUpdateEvt = sendUpdatedEvt;
        Wf.JbpmProcSelect.superclass.setValue.call(this, val, sendUpdatedEvt);
    },

    refresh:function () {
        var i;

        this.clear();
        for (i = 0; i < this.choicesList.length; i++) {
            var choice = this.choicesList[i];
            this.removeChoiceNode(choice.node);
        }
        this.choicesList = [];
        Wf.loadJbpmProcs(this.addProc, this);
    },

    addProc:function (procs) {
        for (var i = 0; i < procs.length; i++) {
            var v = {value:procs[i].procCode, label:procs[i].name};
            if (this.getChoicePosition(v) == -1)
                this.addChoice(v);
        }
        //set value executed early then choice list arrived from server
        if (this.newVal) {
            Wf.JbpmProcSelect.superclass.setValue.call(this, this.newVal, this.sendUpdateEvt);
            this.newVal = null;
        }

    }
});

inputEx.registerType("WfJbpmProcSelect", Wf.JbpmProcSelect);
