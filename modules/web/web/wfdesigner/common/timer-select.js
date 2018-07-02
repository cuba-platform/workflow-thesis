/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
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
        this.timerTypeField.updatedEvt.subscribe(this.markUnsaved, this, true);

        this.divEl.appendChild(this.fieldset);

        if(this.options.disabled) {
            this.disable();
        }
    },

    markUnsaved: function(e) {
        if (!this.initialized) {
            this.initialized = true;
        }
        else {
            this.options.container.layer.eventChanged.fire();
        }
    },

    onTimerTypeChanged: function(e, params) {
        this.timerType = params[0];
        //console.log(this.formName);
        this.renderTimerParams(false);
    },

    renderDueDateField: function() {
        var dueDateField = {
            type: 'combine', label: i18n.get('Timer.dueDate'), name: 'dueDate', fields: [
                {type: 'integer', name: 'qty', className: 'Wf-TimerSelect-dueDate-qty'},
                {type: 'select', name: 'calendar', choices: [
                    {value: '', label: i18n.get('Timer.calendar')},
                    {value: 'business', label: i18n.get('Timer.business')}
                ]},
                {type: 'select', name: 'interval', choices: [
                    {value: 'minute', label: i18n.get('Timer.minute')},
                    {value: 'hour', label: i18n.get('Timer.hour')},
                    {value: 'day', label: i18n.get('Timer.day')}
                ]}
            ]
        };
        this.timerParamsGroup.addField(dueDateField);
        this.timerParamsGroup.getFieldByName('dueDate').updatedEvt.subscribe(this.markUnsaved,this,true);
    } ,

    onDueDateTypeChanged: function(e, params) {
        this.markUnsaved()
        var dueDateType = params[0];
        if (dueDateType == 'manual') {
            this.renderDueDateField();
        }
        else {
            var dueDateField = this.timerParamsGroup.getFieldByName('dueDate');
            if (dueDateField) {
                dueDateField.destroy();
            }
        }
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

            var dueDateChoices = [
                {value: null, label: ""},
                {value: 'form', label: i18n.get('Timer.fromProcess')},
                {value: 'manual', label: i18n.get('Timer.manual')}
            ];

            var timerDueDateChooser = {
                type: 'select', label: i18n.get('Timer.dueDateType'), name: 'dueDateType', choices:dueDateChoices};
            fields.push(timerDueDateChooser);

            this.timerParamsGroup = inputEx({type: "group", fields: fields}, this);
            this.timerParamsGroup.getFieldByName('dueDateType').updatedEvt.subscribe(this.onDueDateTypeChanged,this,true);
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
            this.timerParamsGroup.updatedEvt.subscribe(this.markUnsaved,this,true);
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
            if ('manual' == val.properties.dueDateType) {
                this.renderDueDateField();
            }
            this.timerParamsGroup.setValue(val.properties);
        } catch(e) {
            console.log(e)
        }
    }
});

inputEx.registerType("wfTimerSelect", Wf.TimerSelect);
