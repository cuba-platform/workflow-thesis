/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

/**
 * <p>$Id$</p>
 *
 * @author devyatkin
 */

Wf.Layer = function(options) {
    Wf.Layer.superclass.constructor.call(this, options);
    this.eventContainerDragged.subscribe(this.onContainersMoved, this);
    this.eventAddContainer.subscribe(this.onContainersMoved, this);
}

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
    width:1200,

    heightArrows: WireIt.cn('div', {class: 'heightArrows'}),
    widthArrows: WireIt.cn('div', {class: 'widthArrows'}),
    arrowDown: WireIt.cn('div', {class: 'arrowDown'}),
    arrowUp: WireIt.cn('div', {class: 'arrowUp'}),
    arrowRight: WireIt.cn('div', {class: 'arrowRight'}),
    arrowLeft: WireIt.cn('div', {class: 'arrowLeft'}),

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
    }

})




