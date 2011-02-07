/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 28.01.11 12:08
 *
 * $Id$
 */

Wf.Container = function(options, layer) {
    Wf.Container.superclass.constructor.call(this, options, layer);

    this.eventFocus.subscribe(this.onContainerFocus, this, true);
    this.eventBlur.subscribe(this.onContainerBlur, this, true);
};

YAHOO.lang.extend(Wf.Container, WireIt.FormContainer, {

    xtype: "Wf.Container",

    initTerminals: function(terminalConfigs) {
        Wf.Container.superclass.initTerminals.call(this, terminalConfigs);
        Wf.initTerminalLabels(this, terminalConfigs);
    },

    onContainerFocus: function(eventName, containers) {
        var container = containers[0];
        Wf.OptionFieldsHelper.showOptions(container);
    },

    onContainerBlur: function(eventName, containers) {
        var container = containers[0];
        Wf.OptionFieldsHelper.hideOptions(container);
    },

    onCloseButton: function(e, args) {
        Wf.Container.superclass.onCloseButton.call(this, e, args);
        Wf.OptionFieldsHelper.hideOptions(this);
    },

    getValue: function() {
        var value = Wf.Container.superclass.getValue.call(this);
        value.options = Wf.OptionFieldsHelper.getValue(this);
        return value;
    },

    setValue: function(val) {
        Wf.Container.superclass.setValue.call(this, val);
        Wf.OptionFieldsHelper.setValue(this, val.options);
    }

});
