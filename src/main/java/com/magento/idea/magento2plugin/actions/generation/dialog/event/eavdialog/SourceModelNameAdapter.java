/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog;

import com.intellij.ui.DocumentAdapter;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.SplitEavAttributeCodeUtil;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.NotNull;

public class SourceModelNameAdapter extends DocumentAdapter {

    private final JTextField sourceModelNameTexField;

    public SourceModelNameAdapter(final JTextField sourceModelNameTexField) {
        super();
        this.sourceModelNameTexField = sourceModelNameTexField;
    }

    @Override
    protected void textChanged(@NotNull final DocumentEvent event) {
        final Document document = event.getDocument();
        try {
            final String attributeCode = document.getText(
                    0, document.getEndPosition().getOffset()
            ).trim();
            updateSourceModelName(attributeCode);
        } catch (BadLocationException badLocationException) {
            return;
        }
    }

    private void updateSourceModelName(final String attributeCode) {
        if (attributeCode.isEmpty()) {
            sourceModelNameTexField.setText("");

            return;
        }

        final StringBuilder sourceModelClassName = new StringBuilder();

        for (final String codePart : SplitEavAttributeCodeUtil.execute(attributeCode)) {
            sourceModelClassName.append(StringUtils.capitalise(codePart));
        }

        sourceModelNameTexField.setText(sourceModelClassName.toString());
    }
}
