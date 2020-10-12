package com.magento.idea.magento2plugin.actions.generation.dialog.util;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;

public final class HighlightDialogFieldOnErrorUtil {
    private static final Color ERROR_BORDER_COLOR = new Color(252, 119, 83);
    private static final Color ERROR_BACKGROUND_COLOR = new Color(252, 119, 83, 15);

    private HighlightDialogFieldOnErrorUtil() {}

    /**
     * Add error highlighting for JComponent.
     *
     * @param field JComponent
     */
    public static void execute(final JComponent field) {
        final Color defaultBackgroundColor = field.getBackground();

        field.setBorder(BorderFactory.createLineBorder(ERROR_BORDER_COLOR));
        field.setBackground(ERROR_BACKGROUND_COLOR);

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent event) {
                field.setBorder(null);
                field.setBackground(defaultBackgroundColor);
            }

            @Override
            public void focusLost(FocusEvent e) {}//NOPMD
        });
    }
}
