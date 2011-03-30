/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 28.01.11 12:09
 *
 * $Id$
 */
Wf.ImageContainer = function(options, layer) {
    Wf.ImageContainer.superclass.constructor.call(this, options, layer);

    this.eventFocus.subscribe(this.onContainerFocus, this, true);
    this.eventBlur.subscribe(this.onContainerBlur, this, true);
};

YAHOO.lang.extend(Wf.ImageContainer, WireIt.ImageContainer, {

    xtype: "Wf.ImageContainer",

    selectedImage: null,

    initTerminals: function(terminalConfigs) {
        Wf.ImageContainer.superclass.initTerminals.call(this, terminalConfigs);
        Wf.initTerminalLabels(this, terminalConfigs);
    },

    onContainerFocus: function(eventName, containers) {
        var container = containers[0];
        Wf.OptionFieldsHelper.showOptions(container);
        YAHOO.util.Dom.setStyle(this.bodyEl, "background-image", "url("+this.selectedImage+")");
    },

    onContainerBlur: function(eventName, containers) {
        var container = containers[0];
        Wf.OptionFieldsHelper.hideOptions(container);
        YAHOO.util.Dom.setStyle(this.bodyEl, "background-image", "url("+this.image+")");
    },

    onCloseButton: function(e, args) {
        Wf.ImageContainer.superclass.onCloseButton.call(this, e, args);
        Wf.OptionFieldsHelper.hideOptions(this);
    },

    getValue: function() {
        var value = Wf.ImageContainer.superclass.getValue.call(this);
        value.options = Wf.OptionFieldsHelper.getValue(this);
        return value;
    },

    setValue: function(val) {
        Wf.ImageContainer.superclass.setValue.call(this, val);
        Wf.OptionFieldsHelper.setValue(this, val.options);
    }
});
