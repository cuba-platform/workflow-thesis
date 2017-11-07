Wf.CardInheritorSelect = function(options) {
    Wf.CardInheritorSelect.superclass.constructor.call(this, options);

    this.refresh(false);
};

YAHOO.lang.extend(Wf.CardInheritorSelect, inputEx.SelectField, {

    allowVariable: false,
    initiated: false,
    prevVal: null,
    isLoadedCardInheritors: false,

    setOptions: function(options) {
        Wf.OutputSelect.superclass.setOptions.call(this, options);

        this.options.className = options.className || 'Wf-CardInheritorSelect';
        this.options.container = options.container;
        this.isLoaded = !Wf.editor.preventLayerChangedEvent;
    },

    setValue: function(val, sendUpdatedEvt) {
        if (!val)
            return;
        this.refresh(true);
        this.options.container.propertyPathField.clazz = val;
        if (this.initiated && val!=this.prevVal){
            this.options.container.propertyPathField.setValue(null,sendUpdatedEvt);

        }
        this.newVal = val;
        this.initiated = true;
        this.prevVal = val;
        this.sendUpdateEvt = sendUpdatedEvt;
        Wf.CardInheritorSelect.superclass.setValue.call(this, val, sendUpdatedEvt);
    },

    refresh: function(useCardInheritorsCache) {
        var i;

        this.clear();
        for (i = 0; i < this.choicesList.length; i++) {
            var choice = this.choicesList[i];
            this.removeChoiceNode(choice.node);
        }
        this.choicesList = [];

        if (useCardInheritorsCache && Wf.cardInheritorsCache) {
            for (var j = 0; j < Wf.cardInheritorsCache.length; j++) {
                var s = Wf.cardInheritorsCache[j];
                var v = {value:s.key, label:s.name};
                if (this.getChoicePosition(v) == -1)
                    this.addChoice(v);
            }
            if(Wf.cardInheritorsCache.length>0){
                this.setDefaultClassForPropertyPathField(Wf.cardInheritorsCache[0].key);
            }
        } else {
            Wf.loadCardInheritors(this.addCardInheritors, this);
        }
    },

    addCardInheritors: function(cardInheritors) {
        var isLoadedCardInheritors = this.isLoadedCardInheritors;
        this.isLoadedCardInheritors = true;
        Wf.cardInheritorsCache = cardInheritors;
        for (var i = 0; i < cardInheritors.length; i++) {
            var s = cardInheritors[i];
            var v = {value:s.key, label:s.name};
            if (this.getChoicePosition(v) == -1)
                this.addChoice(v);
        }
        //set value executed early then choice list arrived from server
        if (this.newVal) {
            Wf.CardInheritorSelect.superclass.setValue.call(this, this.newVal,
                this.isLoaded && this.sendUpdatedEvt !== false && isLoadedCardInheritors);
            this.options.container.previousValue = this.newVal;
            this.newVal = null;
        }
        if(cardInheritors.length>0){
            this.setDefaultClassForPropertyPathField(cardInheritors[0].key);
        }
        if (this.isLoaded) {
            this.isLoaded = true;
        }
    },

    setDefaultClassForPropertyPathField: function(value){
        if (this.options.container.propertyPathField==null) return;
        if (this.options.container.propertyPathField.clazz == null){
                this.options.container.propertyPathField.clazz = value;
        }
    }
});

inputEx.registerType("cardInheritorSelect", Wf.CardInheritorSelect);