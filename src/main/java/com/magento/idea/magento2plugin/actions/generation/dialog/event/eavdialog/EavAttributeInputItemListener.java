/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog;

import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeSourceModel;
import com.magento.idea.magento2plugin.magento.packages.uicomponent.AvailableSourcesByInput;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JComboBox;

public class EavAttributeInputItemListener implements ItemListener {
    private final JComboBox<ComboBoxItemData> sourceComboBox;

    public EavAttributeInputItemListener(final JComboBox<ComboBoxItemData> sourceComboBox) {
        this.sourceComboBox = sourceComboBox;
    }

    @Override
    public void itemStateChanged(final ItemEvent itemEvent) {
        final String selectedInput = itemEvent.getItem().toString();

        final List<ComboBoxItemData> availableSources =
                new AvailableSourcesByInput(selectedInput).getItems();
        sourceComboBox.removeAllItems();

        final ComboBoxItemData defaultSourceItem = new ComboBoxItemData(
                AttributeSourceModel.NULLABLE_SOURCE.name(),
                AttributeSourceModel.NULLABLE_SOURCE.getSource()
        );

        sourceComboBox.addItem(defaultSourceItem);
        sourceComboBox.setSelectedItem(defaultSourceItem);

        if (availableSources.isEmpty()) {
            return;
        }

        for (final ComboBoxItemData comboBoxItemData : availableSources) {
            sourceComboBox.addItem(comboBoxItemData);
        }
    }
}
