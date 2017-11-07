/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

Wf.Layer = function(options) {
    Wf.Layer.superclass.constructor.call(this, options);
    this.eventContainerDragged.subscribe(this.onContainersMoved, this);
    this.eventAddContainer.subscribe(this.onContainersMoved, this);
};

YAHOO.lang.extend(Wf.Layer, WireIt.Layer, {

    onContainersMoved: function(e, params) {
        var container = params[0];
        if (container.getXY()[1] + 200 >= container.layer.height) {
            container.layer.height = container.getXY()[1] + 400;
            WireIt.sn(container.layer.el, null, { height:container.layer.height + 'px' });
        }
        if (container.getXY()[0] + 200 >= container.layer.width) {
            container.layer.width = container.getXY()[0] + 400;
            WireIt.sn(container.layer.el, null, { width:container.layer.width + 'px' });
        }
    },

    height:1000,
    width:1400,

    heightArrows: WireIt.cn('div', {'class': 'heightArrows'}),
    widthArrows: WireIt.cn('div', {'class': 'widthArrows'}),
    arrowDown: WireIt.cn('div', {'class': 'arrowDown'}),
    arrowUp: WireIt.cn('div', {'class': 'arrowUp'}),
    arrowRight: WireIt.cn('div', {'class': 'arrowRight'}),
    arrowLeft: WireIt.cn('div', {'class': 'arrowLeft'}),

    render: function() {
        this.el = WireIt.cn('div', {className: this.className}, {height: this.height + 'px', width: this.width + 'px'});
        YAHOO.util.Event.addListener(this.arrowDown, 'click', this.scrollDown, this, true);
        YAHOO.util.Event.addListener(this.arrowUp, 'click', this.scrollUp, this, true);
        YAHOO.util.Event.addListener(this.arrowRight, 'click', this.scrollRight, this, true);
        YAHOO.util.Event.addListener(this.arrowLeft, 'click', this.scrollLeft, this, true);

        this.heightArrows.appendChild(this.arrowDown);
        this.heightArrows.appendChild(this.arrowUp);

        this.widthArrows.appendChild(this.arrowRight);
        this.widthArrows.appendChild(this.arrowLeft);

        this.parentEl.appendChild(this.el);
        this.parentEl.appendChild(this.heightArrows);
        this.parentEl.appendChild(this.widthArrows);

    },

    addContainer: function(containerConfig) {
        var currNumber = 0;
        for (var i = 0; i < this.containers.length; i++) {
            var cont = this.containers[i];
            if (cont.moduleName == containerConfig.moduleName) {
                if (cont.number > currNumber) {
                    currNumber = cont.number;
                }
            }
        }
        var klass = WireIt.containerClassFromXtype(containerConfig.xtype);

        var container = new klass(containerConfig, this);
        var optField;
            for (var j = 0; j < containerConfig.optFields.length; j++) {
                if (containerConfig.optFields[j].name == 'name') {
                    optField = containerConfig.optFields[j];
                    break;
                }
            }
        if (containerConfig.moduleName == 'Start') {
            container.optionsValue = {name: optField.value};
        }
        else {
            container.number = currNumber + 1;
            if (container.form) {
               container.form.setValue({name:optField.value + container.number}, false);
            }
            container.optionsValue = {name: optField.value + container.number};
        }

        return this.addContainerDirect(container);
    },

    scrollDown : function(type, args) {
        args['height'] += 300;
        WireIt.sn(args['el'], null, {height: args['height'] + 'px'});
        this.parentEl.scrollTop = this.parentEl.scrollHeight;
    },

    scrollUp : function(type, args) {
        if (args['height'] <= 1200)
            return;
        args['height'] -= 300;
        WireIt.sn(args['el'], null, { height:args['height'] + 'px' });

    },

    scrollRight : function(type, args) {
        args['width'] += 200;
        WireIt.sn(args['el'], null, { width:args['width'] + 'px' });
        this.parentEl.scrollLeft = this.parentEl.scrollWidth;

    },
    scrollLeft : function(type, args) {
        if (args['width'] <= 1200)
            return;
        args['width'] -= 200;
        WireIt.sn(args['el'], null, { width:args['width'] + 'px' });
    },

    addWire: function(wireConfig) {

          var src = wireConfig.src;
          var tgt = wireConfig.tgt;

          var terminal1 = this.containers[src.moduleId].getTerminal(src.terminal);
          var terminal2 = this.containers[tgt.moduleId].getTerminal(tgt.terminal);
          if (terminal1==null || terminal2==null){
                return;
          }

          return Wf.Layer.superclass.addWire.call(this,wireConfig);
       },

});




