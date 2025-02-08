/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.bundles;

import com.intellij.CommonBundle;
import java.util.ResourceBundle;

public abstract class AbstractBundle {

    public abstract String getBundleName();

    /**
     * Get bundle message.
     *
     * @param key String
     * @param params Object[]
     *
     * @return String
     */
    public String message(final String key, final Object... params) {
        final ResourceBundle bundle = ResourceBundle.getBundle(getBundleName());

        return CommonBundle.message(bundle, key, params);
    }

    /**
     * Get message or default value.
     *
     * @param key String
     * @param defaultValue String
     * @param params Object[]
     *
     * @return String
     */
    public String messageOrDefault(
            final String key,
            final String defaultValue,
            final Object... params
    ) {
        final ResourceBundle bundle = ResourceBundle.getBundle(getBundleName());

        return CommonBundle.messageOrDefault(bundle, key, defaultValue, params);
    }
}
