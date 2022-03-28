/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog;

import com.magento.idea.magento2plugin.magento.files.SourceModelFile;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JTextField;

public class AttributeSourcePanelComponentListener implements ComponentListener {
    private final JTextField sourceModelDirectoryTexField;

    public AttributeSourcePanelComponentListener(
            final JTextField sourceModelDirectoryTexField
    ) {
        this.sourceModelDirectoryTexField = sourceModelDirectoryTexField;
    }

    @Override
    public void componentResized(final ComponentEvent componentEvent) {
        //
    }

    @Override
    public void componentMoved(final ComponentEvent componentEvent) {
        //
    }

    @Override
    public void componentShown(final ComponentEvent componentEvent) {
        initSetDirectoryFieldValue();
    }

    @Override
    public void componentHidden(final ComponentEvent componentEvent) {
        initSetDirectoryFieldValue();
    }

    private void initSetDirectoryFieldValue() {
        final String sourceDirectoryValue = sourceModelDirectoryTexField.getText().trim();

        if (sourceDirectoryValue.isEmpty()) {
            sourceModelDirectoryTexField.setText(SourceModelFile.DEFAULT_DIR);
        }
    }
}
