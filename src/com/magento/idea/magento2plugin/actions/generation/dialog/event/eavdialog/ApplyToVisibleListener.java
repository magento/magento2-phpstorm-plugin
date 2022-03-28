/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jetbrains.annotations.NotNull;

public class ApplyToVisibleListener implements ChangeListener {

    private final JPanel applyToPanel;

    public ApplyToVisibleListener(@NotNull final JPanel applyToPanel) {
        this.applyToPanel = applyToPanel;
    }

    @Override
    public void stateChanged(final ChangeEvent changeEvent) {
        final JCheckBox checkBox = (JCheckBox) changeEvent.getSource();

        applyToPanel.setVisible(!checkBox.isSelected());
    }
}
