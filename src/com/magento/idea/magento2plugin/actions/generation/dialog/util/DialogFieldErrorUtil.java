/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.util;

import com.intellij.util.ui.UIUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.AbstractDialog;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Field;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.jetbrains.annotations.NotNull;

public final class DialogFieldErrorUtil {

    private static final Color ERROR_COLOR = new Color(252, 119, 83);
    private static final Color ERROR_BACKGROUND_COLOR = new Color(252, 119, 83, 15);
    private static final String ERROR_HOLDER_NAME_SUFFIX = "ErrorMessage";

    private DialogFieldErrorUtil() {}

    /**
     * Add error highlighting for JComponent.
     *
     * @param dialog AbstractDialog
     * @param field Field
     */
    public static void highlightField(
            final @NotNull AbstractDialog dialog,
            final @NotNull Field field
    ) {
        final JComponent fieldComponent = getComponentForField(dialog, field);

        if (fieldComponent != null) {
            highlightField(fieldComponent);
        }
    }

    /**
     * Add error highlighting for JComponent.
     *
     * @param fieldComponent JComponent
     */
    public static void highlightField(final @NotNull JComponent fieldComponent) {
        final Color defaultBackgroundColor = fieldComponent.getBackground();

        fieldComponent.setBorder(BorderFactory.createLineBorder(ERROR_COLOR));
        fieldComponent.setBackground(ERROR_BACKGROUND_COLOR);

        fieldComponent.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent event) {
                fieldComponent.setBorder(null);
                fieldComponent.setBackground(defaultBackgroundColor);
            }

            @Override
            public void focusLost(final FocusEvent event) {
                fieldComponent.setBorder(null);
                fieldComponent.setBackground(defaultBackgroundColor);
            }
        });
    }

    /**
     * Show error message for field.
     *
     * @param dialog AbstractDialog
     * @param field Field
     * @param message String
     *
     * @return boolean value that specify if error message was shown for field.
     */
    public static boolean showErrorMessageForField(
            final @NotNull AbstractDialog dialog,
            final @NotNull Field field,
            final @NotNull String message
    ) {
        final JLabel messageHolder = getMessageHolderForField(dialog, field);
        final JComponent fieldComponent = getComponentForField(dialog, field);

        if (messageHolder == null || fieldComponent == null) {
            return false;
        }
        highlightField(fieldComponent);

        messageHolder.setVisible(true);
        messageHolder.setFont(UIUtil.getLabelFont(UIUtil.FontSize.MINI));
        messageHolder.setForeground(ERROR_COLOR);
        messageHolder.setText(message);

        fieldComponent.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent event) {
                messageHolder.setVisible(false);
                messageHolder.setText("");
            }

            @Override
            public void focusLost(final FocusEvent event) {
                messageHolder.setVisible(false);
                messageHolder.setText("");
            }
        });

        return true;
    }

    /**
     * Get message holder component for field.
     *
     * @param dialog AbstractDialog
     * @param field Field
     *
     * @return JLabel
     */
    private static JLabel getMessageHolderForField(
            final @NotNull AbstractDialog dialog,
            final @NotNull Field field
    ) {
        try {
            final String errorHolderName = field.getName().concat(ERROR_HOLDER_NAME_SUFFIX);
            final Field holderField = dialog.getClass().getDeclaredField(errorHolderName);
            holderField.setAccessible(true);
            final Object holderComponent = holderField.get(dialog);
            holderField.setAccessible(false);

            if (holderComponent instanceof JLabel) {
                return (JLabel) holderComponent;
            }
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            return null;
        }

        return null;
    }

    /**
     * Get JComponent for field.
     *
     * @param field Field
     *
     * @return JComponent
     */
    private static JComponent getComponentForField(
            final @NotNull AbstractDialog dialog,
            final @NotNull Field field
    ) {
        try {
            field.setAccessible(true);
            final Object component = field.get(dialog);

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
