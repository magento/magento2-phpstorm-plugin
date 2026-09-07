define(['uiComponent'], function (Component) {
    'use strict';

    return Component.extend({
        defaults: {
            elementTmpl: 'Foo_Bar/templa<caret>te2'
        }
    });
});
