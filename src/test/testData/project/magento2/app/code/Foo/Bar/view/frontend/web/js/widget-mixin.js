define(['jquery'], function ($) {
    'use strict';

    var widgetMixin = {
        closeModal: function () {
            return this._super();
        }
    };

    return function (targetWidget) {
        $.widget('mage.testWidget', targetWidget, widgetMixin);

        return $.mage.testWidget;
    };
});
