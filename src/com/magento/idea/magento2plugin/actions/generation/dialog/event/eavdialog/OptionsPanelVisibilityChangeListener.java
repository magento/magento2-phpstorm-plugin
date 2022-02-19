/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog;

import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeInput;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeSourceModel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class OptionsPanelVisibilityChangeListener implements ItemListener {
    private final JPanel optionsPanel;
    private final JComboBox<ComboBoxItemData> inputComboBox;

    public OptionsPanelVisibilityChangeListener(
            @NotNull final JPanel optionsPanel,
            @NotNull final JComboBox<ComboBoxItemData> inputComboBox
    ) {
        this.optionsPanel = optionsPanel;
        this.inputComboBox = inputComboBox;
    }

    @Override
    public void itemStateChanged(final ItemEvent itemEvent) {
        final String selectedSource = itemEvent.getItem().toString();

        final ComboBoxItemData selectedInputItem =
                (ComboBoxItemData) inputComboBox.getSelectedItem();
        final String selectedInput = selectedInputItem == null ? "" : selectedInputItem.toString();

        final boolean isAttributeWithoutSource =
                AttributeSourceModel.NULLABLE_SOURCE.getSource().equals(selectedSource);
        final boolean isAllowedInput =
                AttributeInput.SELECT.getInput().equals(selectedInput)
                        || AttributeInput.MULTISELECT.getInput().equals(selectedInput);

        optionsPanel.setVisible(isAllowedInput && isAttributeWithoutSource);
    }
}
