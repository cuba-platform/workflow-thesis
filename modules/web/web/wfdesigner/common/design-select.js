/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

Wf.DesignSelect = function(options) {
    Wf.DesignSelect.superclass.constructor.call(this, options);
    this.refresh(false);
};

YAHOO.lang.extend(Wf.DesignSelect, inputEx.SelectField, {

    allowVariable: false,
    designLink: null,
    isLoaded: true,
    isLoadedDesigns: false,

    render: function () {
        Wf.DesignSelect.superclass.render.call(this)
        var designLinkContainer = WireIt.cn('div', { className: 'designLink'}, null, null);
        this.designLink = WireIt.cn('a', { href: '',onClick:'return false;'}, null, i18nDict.OpenDesign);
        YAHOO.util.Event.addListener(this.designLink, "click", this.openDesignUrl, this, true);
        YAHOO.util.Event.addListener(this.designLink, "click", this.openDesignUrl, this, true);
        designLinkContainer.appendChild(this.designLink);
        this.el.parentElement.parentElement.appendChild(designLinkContainer);

    },

    setOptions: function(options) {
        Wf.DesignSelect.superclass.setOptions.call(this, options);

        this.options.className = options.className || 'Wf-DesignSelect';
        this.options.container = options.container;
    },

    createOutputs: function(container,val) {
        for (var i = 0; i < Wf.subdesignCache.length; i++) {
                var choice = Wf.subdesignCache[i];
                if (choice.value == val){
                    container.removeAllOutputs();
                    for (var j = 0; j < choice.outs.length; j++){
                        container.createOutput(null,[choice.outs[j].name]);
                    }
                }
            }
    },

    setValue: function(val, sendUpdatedEvt) {
        if (!val)
           return;

        var container = this.options.container;
        if (val){
            this.refresh(true);
            this.newVal = val;
            this.sendUpdateEvt = sendUpdatedEvt;
            Wf.DesignSelect.superclass.setValue.call(this, val, sendUpdatedEvt);
        }
        if (container!=null && Wf.subdesignCache!=null){
            var valueChanged = val!=container.previousValue || container.previousValue==null;
            if (valueChanged){
                container.previousValue=val;
                this.createOutputs(container,val);
                for (var i = 0; i < Wf.subdesignCache.length; i++) {
                    var choice = Wf.subdesignCache[i];
                    if (choice.value == val){
                        for (var j = 0; j < choice.outs.length; j++){
                            this.designLink.setAttribute('href', this.prepareURL(choice.value));
                        }
                    }
                }
            }
        }

    },

    prepareURL: function (value){
        var url = document.URL;
        var idIndex = url.lastIndexOf('id=') + 3;
        var s1 = url.substr(0, idIndex);
        var s2 = url.substr(idIndex + 36, url.length);
        return s1 + value + s2;
    },

    openDesignUrl: function(){
        var val = this.getValue();
        if (!val)
                return;
       window.open(this.prepareURL(val),val,"width=800,height=600");
    },

    refresh: function(useDesignsCache) {
        var i;

        this.clear();
        for (i = 0; i < this.choicesList.length; i++) {
            var choice = this.choicesList[i];
            this.removeChoiceNode(choice.node);
        }
        this.choicesList = [];
        if (useDesignsCache && Wf.subdesignCache) {
            for (var j = 0; j < Wf.subdesignCache.length; j++) {
                var v = {value:Wf.subdesignCache[j].value, label:Wf.subdesignCache[j].label};
                if (this.getChoicePosition(v) == -1)
                this.addChoice(v);
            }
        } else {
            Wf.loadSubDesigns(this.addDesigns, this);
        }
        this.isLoaded = !(Wf.editor && Wf.editor.preventLayerChangedEvent);
    },

    addDesigns: function(designs) {
        var isLoadedDesigns = this.isLoadedDesigns;
        var isChoicesContainsValue = false;
        this.isLoadedDesigns = true;
        var isLoaded = this.isLoaded;
        this.isLoaded = true;
        Wf.subdesignCache = designs;
        for (var i = 0; i < designs.length; i++) {
            if(this.newVal == designs[i].value) isChoicesContainsValue = true;
            var v = {value:designs[i].value, label:designs[i].label};
            if (this.getChoicePosition(v) == -1)
                this.addChoice(v);
        }
        if(!isChoicesContainsValue && this.options.container.previousValue !== null && this.newVal !== null)
            if(designs.length>0){
            	var firstDesign = {value:designs[0].value, label:designs[0].label};
            	this.createOutputs(this.options.container,firstDesign.value);
            } else{
                this.options.container.removeAllOutputs();
            }
        //set value executed early then choice list arrived from server
        if (this.newVal) {
            Wf.DesignSelect.superclass.setValue.call(this, this.newVal,
                isLoaded && this.sendUpdatedEvt !== false && isLoadedDesigns);
            this.options.container.previousValue = this.newVal;
            this.newVal = null;
        }
        else {
            if (designs.length>0 && this.options.container.previousValue == null){
               var firstDesign = {value:designs[0].value, label:designs[0].label};
               Wf.DesignSelect.superclass.setValue.call(this, firstDesign, isLoaded && this.sendUpdatedEvt && isLoadedDesigns);
               this.options.container.previousValue = firstDesign;
               this.newVal = null;
               this.createOutputs(this.options.container,firstDesign.value);
            }
        }
    }
});

inputEx.registerType("wfDesignSelect", Wf.DesignSelect);

