/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.resources;

import com.intellij.CommonBundle;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;
import java.util.ResourceBundle;

public class ValidatorBundle {
    /** The {@link ResourceBundle} path. */
    @NonNls
    public static final String BUNDLE_NAME = "messages.ValidatorBundle";

    /** The {@link ResourceBundle} instance. */
    @NotNull
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Available syntax list.
     */
    public enum Syntax {
        GLOB, REGEXP;

        @NonNls
        private static final String KEY = "syntax:";

        @Nullable
        public static Syntax find(@Nullable String name) {
            if (name == null) {
                return null;
            }

            try {
                return Syntax.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException iae) {
                return null;
            }
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        /**
         * @return element presentation
         */
        public String getPresentation() {
            return StringUtil.join(KEY, " ", toString());
        }
    }

    /**
     * Loads a {@link String} from the {@link #BUNDLE} {@link ResourceBundle}.
     *
     * @param key    the key of the resource
     * @param params the optional parameters for the specific resource
     *
     * @return the {@link String} value or {@code null} if no resource found for the key
     */
    public static String message(@PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
        return CommonBundle.message(BUNDLE, key, params);
    }

    /**
     * Loads a {@link String} from the {@link #BUNDLE} {@link ResourceBundle}.
     *
     * @param key the key of the resource
     * @param defaultValue the default value that will be returned if there is nothing set
     * @param params the optional parameters for the specific resource
     *
     * @return the {@link String} value or {@code null} if no resource found for the key
     */
    public static String messageOrDefault(
            @PropertyKey(resourceBundle = BUNDLE_NAME) String key,
            String defaultValue,
            Object... params
    ) {
        return CommonBundle.messageOrDefault(BUNDLE, key, defaultValue, params);
    }
}
