/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.uicomponent;

import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeInput;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeSourceModel;
import com.sun.istack.NotNull;
import java.util.ArrayList;
import java.util.List;

public class AvailableSourcesByInput {
    private final String input;

    public AvailableSourcesByInput(@NotNull final String input) {
        this.input = input;
    }

    /**
     * Source items getter.
     *
     * @return List
     */
    public List<ComboBoxItemData> getItems() {
        final List<ComboBoxItemData> items = new ArrayList<>();
        final ComboBoxItemData generateSourceItem = new ComboBoxItemData(
                AttributeSourceModel.GENERATE_SOURCE.getSource(),
                AttributeSourceModel.GENERATE_SOURCE.getSource()
        );
        items.add(generateSourceItem);

        if (input.equals(AttributeInput.BOOLEAN.getInput())) {
            items.add(new ComboBoxItemData(
                    AttributeSourceModel.BOOLEAN.getSource(),
                    AttributeSourceModel.BOOLEAN.getSource())
            );
        } else if (input.equals(AttributeInput.SELECT.getInput())
                || input.equals(AttributeInput.MULTISELECT.getInput())) {
            items.add(new ComboBoxItemData(
                    AttributeSourceModel.BOOLEAN.getSource(),
                    AttributeSourceModel.BOOLEAN.getSource())
            );
            items.add(new ComboBoxItemData(
                    AttributeSourceModel.TABLE.getSource(),
                    AttributeSourceModel.TABLE.getSource()
            ));
            items.add(new ComboBoxItemData(
                    AttributeSourceModel.STORE.getSource(),
                    AttributeSourceModel.STORE.getSource()
            ));
            items.add(new ComboBoxItemData(
                    AttributeSourceModel.CONFIG.getSource(),
                    AttributeSourceModel.CONFIG.getSource()
            ));
        }

        return items;
    }
}
