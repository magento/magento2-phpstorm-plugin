define([
    'mage/utils/wrapper'
], function (wrapper) {
    'use strict';

    return function (target) {
        return wrapper.wrap(target, function (original) {
            return original();
        });
    };
});
