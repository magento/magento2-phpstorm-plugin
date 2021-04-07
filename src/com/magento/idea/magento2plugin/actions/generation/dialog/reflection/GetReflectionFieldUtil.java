/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.reflection;

import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;

public final class GetReflectionFieldUtil {

    private GetReflectionFieldUtil() {}

    /**
     * Get field by its name for specified type.
     *
     * @param name String
     * @param type Class
     *
     * @return Field
     */
    public static Field getByName(final @NotNull String name, final @NotNull Class<?> type) {
        try {
            return type.getDeclaredField(name);
        } catch (NoSuchFieldException | SecurityException exception) {
            return null;
        }
    }
}
