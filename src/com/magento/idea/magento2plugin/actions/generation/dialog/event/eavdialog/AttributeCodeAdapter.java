/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog;

import com.intellij.ui.DocumentAdapter;
import java.util.Locale;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jetbrains.annotations.NotNull;

public class AttributeCodeAdapter extends DocumentAdapter {

    private final JTextField attributeCodeTextField;

    public AttributeCodeAdapter(@NotNull final JTextField attributeCodeTextField) {
        super();
        this.attributeCodeTextField = attributeCodeTextField;
    }

    @Override
    protected void textChanged(final @NotNull DocumentEvent event) {
        final Document document = event.getDocument();

        try {
            final String attributeLabel = document.getText(
                    0, document.getEndPosition().getOffset()
            ).trim();
            attributeCodeTextField.setText(
                    convertLabelToAttributeCode(attributeLabel)
            );
        } catch (BadLocationException badLocationException) {
            return;
        }
    }

    private String convertLabelToAttributeCode(final String attributeLabel) {
        final String formattedAttributeLabel = attributeLabel.trim().toLowerCase(Locale.ROOT);

        return formattedAttributeLabel.replaceAll("^ +| +$|( )+", "_");
    }
}
