/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.util;

import com.intellij.util.ui.UIUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.AbstractDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.reflection.ExtractComponentFromFieldUtil;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Field;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
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
        final JComponent fieldComponent = ExtractComponentFromFieldUtil.extract(field, dialog);

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
        fieldComponent.setBorder(BorderFactory.createLineBorder(ERROR_COLOR));
        fieldComponent.setBackground(ERROR_BACKGROUND_COLOR);

        fieldComponent.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent event) {
                resetComponentHighlighting(fieldComponent);
            }

            @Override
            public void focusLost(final FocusEvent event) {
                resetComponentHighlighting(fieldComponent);
            }
        });
    }

    /**
     * Reset field highlighting.
     *
     * @param field Field
     * @param dialog AbstractDialog
     */
    public static void resetFieldHighlighting(
            final @NotNull Field field,
            final @NotNull AbstractDialog dialog
    ) {
        final JComponent fieldComponent = ExtractComponentFromFieldUtil.extract(field, dialog);

        if (fieldComponent == null) {
            return;
        }

        resetComponentHighlighting(fieldComponent);
        final JLabel messageHolder = getMessageHolderForField(dialog, field);

        if (messageHolder == null) {
            return;
        }

        messageHolder.setVisible(false);
        messageHolder.setText("");
    }

    /**
     * Reset component highlighting.
     *
     * @param fieldComponent JComponent
     */
    public static void resetComponentHighlighting(
            final @NotNull JComponent fieldComponent
    ) {
        Color defaultBackgroundColor;
        Border defaultBorder;

        if (fieldComponent instanceof JTextField) {
            defaultBackgroundColor = UIManager.getColor("TextField.background");
            defaultBorder = UIManager.getBorder("TextField.border");
        } else if (fieldComponent instanceof JComboBox) {
            defaultBackgroundColor = UIManager.getColor("ComboBox.background");
            defaultBorder = UIManager.getBorder("ComboBox.border");
        } else {
            defaultBackgroundColor = UIManager.getColor("TextField.background");
            defaultBorder = UIManager.getBorder("TextField.border");
        }

        fieldComponent.setBackground(defaultBackgroundColor);
        fieldComponent.setBorder(defaultBorder);
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
        final JComponent fieldComponent = ExtractComponentFromFieldUtil.extract(field, dialog);

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
     * Show error message for field for component.
     *
     * @param fieldComponent JTextComponent
     * @param messageHolder JLabel
     * @param message String
     */
    public static void showErrorMessageForField(
            final @NotNull JTextComponent fieldComponent,
            final @NotNull JLabel messageHolder,
            final @NotNull String message
    ) {
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
}
