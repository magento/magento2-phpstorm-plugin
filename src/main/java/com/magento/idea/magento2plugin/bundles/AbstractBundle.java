/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.bundles;

public abstract class AbstractBundle extends com.intellij.AbstractBundle {

    protected AbstractBundle(final String bundleName) {
        super(bundleName);
    }

    public String message(final String key, final Object... params) {
        return getMessage(key, params);
    }

    public String messageOrDefault(
            final String key,
            final String defaultValue,
            final Object... params
    ) {
        return super.messageOrDefault(key, defaultValue, params);
    }
}
