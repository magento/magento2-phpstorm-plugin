define(function () {
    'use strict';

    var mixin = {
        isDisabled: function () {
            return this._super();
        }
    };

    return function (target) {
        return target.extend(mixin);
    };
});
