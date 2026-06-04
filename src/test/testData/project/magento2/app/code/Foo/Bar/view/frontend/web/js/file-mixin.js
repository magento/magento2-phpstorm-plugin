/**
 * Header comment should not receive Magento JS mixin line markers.
 */
define(function () {
    'use strict';

    return function (target) {
        target.saveShippingInformation = wrapper.wrap(target.saveShippingInformation, function (originalAction) {
            return originalAction();
        });

        return target.extend({});
    };
});
