/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.util.eavdialog;

import com.magento.idea.magento2plugin.actions.generation.data.SourceModelData;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.magento.files.SourceModelFile;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeScope;
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeSourceModel;

public final class AttributeUtil {

    private AttributeUtil() {}

    /**
     * Return actual source class by selected item.
     *
     * @param selectedSourceItem ComboBoxItemData
     * @param sourceModelData    SourceModelData
     * @return String
     */
    public static String getSourceClassBySelectedItem(
            final ComboBoxItemData selectedSourceItem,
            final SourceModelData sourceModelData
    ) {
        if (selectedSourceItem == null
                || selectedSourceItem.getText().equals(
                AttributeSourceModel.NULLABLE_SOURCE.getSource()
        )) {
            return null;
        }

        if (selectedSourceItem.getText().equals(AttributeSourceModel.GENERATE_SOURCE.getSource())
                && sourceModelData != null) {

            return "\\" + new SourceModelFile(
                    sourceModelData.getModuleName(),
                    sourceModelData.getClassName(),
                    sourceModelData.getDirectory()
            ).getClassFqn();
        }

        return selectedSourceItem.toString();
    }

    /**
     * Return actual scope class by selected item.
     *
     * @param selectedScopeItem ComboBoxItemData
     * @return String
     */
    public static String getScopeClassBySelectedItem(final ComboBoxItemData selectedScopeItem) {
        if (selectedScopeItem != null) {
            return selectedScopeItem.getKey().trim();
        }

        return AttributeScope.GLOBAL.getScope();
    }

    /**
     * Return actual input type by selected item.
     *
     * @param selectedInputItem ComboBoxItemData
     * @return String
     */
    public static String getInputTypeBySelectedItem(final ComboBoxItemData selectedInputItem) {
        if (selectedInputItem != null) {
            return selectedInputItem.getText().trim();
        }

        return "";
    }

    /**
     * Return actual backend type by selected item.
     *
     * @param selectedTypeItem ComboBoxItemData
     * @return String
     */
    public static String getBackendTypeBySelectedItem(final ComboBoxItemData selectedTypeItem) {
        if (selectedTypeItem != null) {
            return selectedTypeItem.getText();
        }

        return "";
    }
}
