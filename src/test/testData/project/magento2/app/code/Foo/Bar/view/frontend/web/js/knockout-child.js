define(['uiComponent'], function (Component) {
    'use strict';

    return Component.extend({
        defaults: {
            template: 'Foo_Bar/template/child',
            displayArea: 'childRegion'
        }
    });
});
