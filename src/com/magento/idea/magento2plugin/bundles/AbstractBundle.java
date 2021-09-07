/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.bundles;

import com.intellij.CommonBundle;
import java.util.ResourceBundle;

public abstract class AbstractBundle {

    abstract public String getBundleName();

    public String message(String key, Object... params) {
        ResourceBundle BUNDLE = ResourceBundle.getBundle(getBundleName());

        return CommonBundle.message(BUNDLE, key, params);
    }

    public String messageOrDefault(String key, String defaultValue, Object... params) {
        ResourceBundle BUNDLE = ResourceBundle.getBundle(getBundleName());

        return CommonBundle.messageOrDefault(BUNDLE, key, defaultValue, params);
    }
}
