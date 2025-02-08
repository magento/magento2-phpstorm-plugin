/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.prompt;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.swing.text.JTextComponent;
import org.jdesktop.swingx.prompt.PromptSupport;
import org.jetbrains.annotations.NotNull;

public final class PlaceholderInitializerUtil {

    private static final String PLACEHOLDER_CLIENT_PROPERTY = "promptText";

    private final Class<?> type;
    private final Object object;

    /**
     * PlaceholderInitializerUtil constructor.
     *
     * @param object Object
     */
    public PlaceholderInitializerUtil(final @NotNull Object object) {
        this.object = object;
        type = object.getClass();
    }

    /**
     * Initialize placeholders for supported field types if specified promptText client property.
     */
    public void initialize() {
        for (final Field field : type.getDeclaredFields()) {
            final JTextComponent target = getTarget(field);

            if (target == null) {
                continue;
            }
            final Object promptProperty = target.getClientProperty(
                    PLACEHOLDER_CLIENT_PROPERTY
            );

            if (promptProperty == null) {
                continue;
            }
            PromptSupport.setPrompt((String) promptProperty, target);
        }
    }

    private JTextComponent getTarget(final Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            return null;
        }
        final boolean isAccessible = field.canAccess(object);
        field.setAccessible(true);

        try {
            final Object value = field.get(object);

            return value instanceof JTextComponent ? (JTextComponent) value : null;
        } catch (IllegalAccessException exception) {
            return null;
        } finally {
            field.setAccessible(isAccessible);
        }
    }
}
