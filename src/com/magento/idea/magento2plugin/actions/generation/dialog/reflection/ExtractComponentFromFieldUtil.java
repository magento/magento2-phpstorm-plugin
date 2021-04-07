/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.reflection;

import java.lang.reflect.Field;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public final class ExtractComponentFromFieldUtil {

    private ExtractComponentFromFieldUtil() {}

    /**
     * Get JComponent value for field.
     *
     * @param field Field
     * @param object Object
     *
     * @return JComponent
     */
    public static JComponent extract(final @NotNull Field field, final @NotNull Object object) {
        try {
            field.setAccessible(true);
            final Object component = field.get(object);

            if (component instanceof JComponent) {
                return (JComponent) component;
            }
        } catch (IllegalAccessException exception) {
            return null;
        } finally {
            field.setAccessible(false);
        }

        return null;
    }
}
