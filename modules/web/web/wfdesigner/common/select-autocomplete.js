/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 */

Wf.SelectAutoComplete = function(options) {
    Wf.SelectAutoComplete.superclass.constructor.call(this, options);
};

YAHOO.lang.extend(Wf.SelectAutoComplete, inputEx.AutoComplete, {

    renderComponent: function() {
        Wf.SelectAutoComplete.superclass.renderComponent.call(this);

        this.createPopupButton();
    },

    createPopupButton: function() {
        var options = {
            id: YAHOO.util.Dom.generateId(),
            type: 'submit',
            className: 'yui-sac-popup',
            parentEl: this.wrapEl,
            value: String.fromCharCode(9660),
            onClick: {
                fn: this.onClickPopup,
                scope: this
            }
        };
        this.popupButton = new inputEx.widget.Button(options);
        YAHOO.util.Event.addListener(this.popupButton.el,"blur", function(e) {
            YAHOO.util.Event.stopEvent(e);
            this.onBlurPopup();
        }, this, true);
    },

    buildAutocomplete: function() {
        Wf.SelectAutoComplete.superclass.buildAutocomplete.call(this);
        if (this.oAutoComp) {
            this.oAutoComp.minQueryLength = 0;
	        YAHOO.util.Dom.addClass(this.el, "yui-sac-input");
	    }
    },

    onClickPopup: function() {
        if (this.oAutoComp._elContent && this.oAutoComp._elContent.style["display"] != "none") {
            this.oAutoComp._toggleContainer(false);
        } else {
            this.oAutoComp.sendQuery("");
        }
    },

    onBlurPopup: function() {
        if (this.oAutoComp && !this.oAutoComp._bOverContainer) {
            this.oAutoComp._toggleContainer(false);
        }
    },

    setVariableStyle: function() {
        Wf.SelectAutoComplete.superclass.setVariableStyle.call(this);

        var width = this.el.style['width'] = "124px";
    },

    setOptions: function(options) {
        Wf.SelectAutoComplete.superclass.setOptions.call(this, options);
        this.choicesList = options.choicesList ? options.choicesList : [];
        this.options.datasource = options.datasource ? options.datasource : this.choicesList;
    },

    clear: function() {
        this.choicesList.splice(0,this.choicesList.length);
    },

    addChoice: function(s) {
        this.choicesList.pop(s);
    },

    getChoicePosition: function(s) {
        for (var i = 0; i < this.choicesList.length; i++) {
            if (s == this.choicesList[i]) {
                return i;
            }
        }
        return -1;
    },

    containsChoice: function(s) {
        return this.getChoicePosition(s) >= 0;
    }

});

inputEx.registerType("selectAutocomplete", Wf.SelectAutoComplete);