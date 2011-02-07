/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 14.01.11 12:42
 *
 * $Id$
 */
Wf.FormSelect = function(options) {
    Wf.FormSelect.superclass.constructor.call(this, options);
};

YAHOO.lang.extend(Wf.FormSelect, inputEx.Field, {

    setOptions: function(options) {
        Wf.FormSelect.superclass.setOptions.call(this, options);

        this.options.className = options.className || 'Wf-FormSelect';
        this.options.useTransition = options.useTransition;
    },

    render: function() {
        this.divEl = inputEx.cn('div', {className: this.options.className});
        if(this.options.id) {
            this.divEl.id = this.options.id;
        }

        this.fieldset = inputEx.cn('fieldset');

        if (this.options.useTransition) {
            var transitions = [];
            if (this.options.container) {
                for (var i = 0; i < this.options.container.terminals.length; i++) {
                    var terminal = this.options.container.terminals[i];
                    if (terminal.alwaysSrc) {
                        transitions.push(terminal.name);
                    }
                }
            }
            this.transitionField = inputEx({
                type: 'select', label: i18n.get("transition"), name: 'transition', choices: transitions
            }, this);
            this.fieldset.appendChild(this.transitionField.getEl());
        }

        var formChoices = [{value: null, label: ""}];
        for (var name in Wf.FormSelect.forms) {
            var form = Wf.FormSelect.forms[name];
            formChoices.push({value: name, label: form.label});
        }

        var label = this.options.label || i18n.get("form");

        this.formTypeField = inputEx({
            type: 'select', label: label, name: 'formType', choices: formChoices
        }, this);
        this.fieldset.appendChild(this.formTypeField.getEl());

        this.formTypeField.updatedEvt.subscribe(this.onFormTypeChanged, this, true);

        this.divEl.appendChild(this.fieldset);

        if(this.options.disabled) {
            this.disable();
        }
    },

    onFormTypeChanged: function(e, params) {
        this.formName = params[0];
        //console.log(this.formName);
        this.renderFormParams();
    },

    renderFormParams: function() {
        try {
            if (this.formParamsGroup) {
                this.fieldset.removeChild(this.formParamsGroup.getEl());
                this.formParamsGroup = undefined;
            }
            var form = Wf.FormSelect.forms[this.formName];
            if (form) {
                this.formParamsGroup = inputEx({type: "group", fields: form.fields}, this);
                this.fieldset.appendChild(this.formParamsGroup.getEl());
            }
        } catch(e) {
            console.log(e)
        }
    },

    getValue: function() {
        var val;
        if (this.formName) {
            val = {};
            val.name = this.formName;
            if (this.transitionField) {
                val.transition = this.transitionField.getValue();
            }
            val.properties = this.formParamsGroup.getValue();
        }
        return val;
    },

    setValue: function(val, sendUpdatedEvt) {
        if (!val || !val.name)
            return;

        var form = Wf.FormSelect.forms[val.name];
        if (!form)
            return;

        if (val.transition && this.transitionField) {
            this.transitionField.setValue(val.transition);
        }

        this.formTypeField.setValue(val.name, false);

        this.formName = val.name;
        this.renderFormParams();

        this.formParamsGroup.setValue(val.properties);
    }

});

Wf.FormSelect.forms = {};

Wf.FormSelect.registerForm = function(name, form) {
    Wf.FormSelect.forms[name] = form;
};

inputEx.registerType("wfFormSelect", Wf.FormSelect);

//////////////////////////////////////////////////////////////

Wf.registerForms = function() {
    Wf.FormSelect.registerForm("notification", {
        label: i18n.get("notification"),
        fields: [
            {type: "string", name: "message", label: i18n.get("message")}
        ]
    });

    Wf.FormSelect.registerForm("resolution", {
        label: i18n.get("resolution"),
        fields: [
            {type: "boolean", name: "attachmentsVisible", label: i18n.get("attachmentsVisible")},
            {type: "boolean", name: "commentRequired", label: i18n.get("commentRequired")}
        ]
    });

    Wf.FormSelect.registerForm("transition", {
        label: i18n.get("transition"),
        fields: [
            {type: "boolean", name: "commentVisible", label: i18n.get("commentVisible")},
            {type: "boolean", name: "cardRolesVisible", label: i18n.get("cardRolesVisible")},
            {type: "boolean", name: "dueDateVisible", label: i18n.get("dueDateVisible")},
            {type: "boolean", name: "refusedOnlyVisible", label: i18n.get("refusedOnlyVisible")},
            {type: "string", name: "requiredRoles", label: i18n.get("requiredRoles")}
        ]
    });
};

