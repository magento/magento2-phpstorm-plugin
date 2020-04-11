/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.validators;

import com.intellij.CommonBundle;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class ValidatorBundle {
    /**
     * The {@link ResourceBundle} path.
     */
    @NonNls
    public static final String BUNDLE_NAME = "magento2.validation";

    /**
     * The {@link ResourceBundle} instance.
     */
    @NotNull
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    public static String message(@PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
        return CommonBundle.message(BUNDLE, key, params);
    }

    public static String messageOrDefault(
            @PropertyKey(resourceBundle = BUNDLE_NAME) String key,
            String defaultValue,
            Object... params
    ) {
        return CommonBundle.messageOrDefault(BUNDLE, key, defaultValue, params);
    }
}
