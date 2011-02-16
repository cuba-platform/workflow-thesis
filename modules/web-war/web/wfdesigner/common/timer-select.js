/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 26.01.11 11:13
 *
 * $Id$
 */
Wf.TimerSelect = function(options) {
    Wf.TimerSelect.superclass.constructor.call(this, options);
};

YAHOO.lang.extend(Wf.TimerSelect, inputEx.Field, {

    setOptions: function(options) {
        Wf.TimerSelect.superclass.setOptions.call(this, options);

        this.options.className = options.className || 'Wf-TimerSelect';
        this.options.container = options.container;
    },

    render: function() {
        this.divEl = inputEx.cn('div', {className: this.options.className});
        if(this.options.id) {
            this.divEl.id = this.options.id;
        }

        this.fieldset = inputEx.cn('fieldset');

        var timerTypeChoices = [
            {value: null, label: ""},
            {value: 'transision', label: i18n.get('Timer.takeTransision')},
            {value: 'script', label: i18n.get('Timer.runScript')}
        ];
        this.timerTypeField = inputEx({
            type: 'select', label: i18n.get('Timer.timerType'), name: 'timerType', choices: timerTypeChoices
        }, this);
        this.fieldset.appendChild(this.timerTypeField.getEl());

        this.timerTypeField.updatedEvt.subscribe(this.onTimerTypeChanged, this, true);

        this.divEl.appendChild(this.fieldset);

        if(this.options.disabled) {
            this.disable();
        }
    },

    onTimerTypeChanged: function(e, params) {
        this.timerType = params[0];
        //console.log(this.formName);
        this.renderTimerParams(false);
    },

    renderTimerParams: function(useScriptsCache) {
        try {
            if (this.timerParamsGroup) {
                this.fieldset.removeChild(this.timerParamsGroup.getEl());
                this.timerParamsGroup = undefined;
            }
            var i;
            var fields = [];
            var choices = [];
            var needScripts = false;
            if (this.timerType == 'transision') {
                if (this.options.container) {
                    for (i = 0; i < this.options.container.terminals.length; i++) {
                        var terminal = this.options.container.terminals[i];
                        if (terminal.alwaysSrc) {
                            choices.push(terminal.name);
                        }
                    }
                }
                fields.push({type: 'select', name: 'name', label: i18n.get('Timer.transition'), choices: choices});
            } else if (this.timerType == 'script') {
                fields.push({type: 'select', name: 'name', label: i18n.get('Timer.script'), choices: ['']});
                needScripts = true;
            }

            var dueDateField = {
                type: 'combine', label: i18n.get('Timer.dueDate'), name: 'dueDate', fields: [
                    {type: 'integer', name: 'qty', className: 'Wf-TimerSelect-dueDate-qty'},
                    {type: 'select', name: 'calendar', choices: [
                        '',
                        {value: 'business', label: i18n.get('Timer.business')}
                    ]},
                    {type: 'select', name: 'interval', choices: [
                        {value: 'minute', label: i18n.get('Timer.minute')},
                        {value: 'hour', label: i18n.get('Timer.hour')},
                        {value: 'day', label: i18n.get('Timer.day')}
                    ]}
                ]
            };
            fields.push(dueDateField);

            this.timerParamsGroup = inputEx({type: "group", fields: fields}, this);
            if (needScripts) {
                var scriptsSelect = this.getScriptsSelect();
                if (scriptsSelect) {
                    if (useScriptsCache && Wf.scriptsCache) {
                        for (var j = 0; j < Wf.scriptsCache.length; j++) {
                            var s = Wf.scriptsCache[j];
                            scriptsSelect.addChoice({value: s});
                        }
                    } else {
                        Wf.loadScripts(this.addScripts, this);
                    }
                }
            }
            this.fieldset.appendChild(this.timerParamsGroup.getEl());
        } catch(e) {
            console.log(e)
        }
    },

    getScriptsSelect: function() {
        for (var i = 0; i < this.timerParamsGroup.inputs.length; i++) {
            var input = this.timerParamsGroup.inputs[i];
            if (input.options.name == 'name') {
                return input;
            }
        }
        return null;
    },

    addScripts: function(scripts) {
        Wf.scriptsCache = scripts;
        var select = this.getScriptsSelect();
        if (select) {
            for (var i = 0; i < scripts.length; i++) {
                select.addChoice({value: scripts[i]});
            }
        }
    },

    getValue: function() {
        var val;
        if (this.timerType) {
            val = {};
            val.type = this.timerType;
            val.properties = this.timerParamsGroup.getValue();
        }
        return val;
    },

    setValue: function(val, sendUpdatedEvt) {
        if (!val || !val.type)
            return;

        try {
            this.timerTypeField.setValue(val.type, false);

            this.timerType = val.type;
            this.renderTimerParams(true);

            this.timerParamsGroup.setValue(val.properties);
        } catch(e) {
            console.log(e)
        }
    }
});

inputEx.registerType("wfTimerSelect", Wf.TimerSelect);