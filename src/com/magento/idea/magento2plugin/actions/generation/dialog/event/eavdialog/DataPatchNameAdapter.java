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

public class DataPatchNameAdapter extends DocumentAdapter {

    private static final String NAME_PREFIX = "Add";
    private static final String NAME_SUFFIX = "Attribute";
    private final String entityType;
    private final JTextField dataPatchNameTextField;

    /**
     * Constructor.
     *
     * @param dataPatchNameTextField JTextField
     */
    public DataPatchNameAdapter(final JTextField dataPatchNameTextField) {
        super();

        this.dataPatchNameTextField = dataPatchNameTextField;
        entityType = "";
    }

    /**
     * Constructor.
     *
     * @param dataPatchNameTextField JTextField
     * @param entityType String
     */
    public DataPatchNameAdapter(
            final JTextField dataPatchNameTextField,
            final String entityType
    ) {
        super();

        this.dataPatchNameTextField = dataPatchNameTextField;
        this.entityType = entityType;
    }

    @Override
    protected void textChanged(@NotNull final DocumentEvent event) {
        final Document document = event.getDocument();

        try {
            final String attributeCode = document.getText(
                    0, document.getEndPosition().getOffset()
            ).trim();
            updateDataPatchFileName(attributeCode);
        } catch (BadLocationException badLocationException) {
            return;
        }
    }

    private void updateDataPatchFileName(final String attributeCode) {
        if (attributeCode.isEmpty()) {
            dataPatchNameTextField.setText("");

            return;
        }

        String fileName = "";

        for (final String fileNamePart : SplitEavAttributeCodeUtil.execute(attributeCode)) {
            fileName = String.join("", fileName, StringUtils.capitalise(fileNamePart));
        }

        dataPatchNameTextField.setText(
                String.join(
                        "",
                        NAME_PREFIX,
                        fileName,
                        StringUtils.capitalise(entityType), NAME_SUFFIX
                )
        );
    }
}
