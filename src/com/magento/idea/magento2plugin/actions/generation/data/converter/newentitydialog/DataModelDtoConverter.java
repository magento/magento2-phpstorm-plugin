/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.DataModelData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class DataModelDtoConverter extends DataModelData implements DataObjectConverter {

    /**
     * DTO converter.
     *
     * @param generationContextData EntityCreatorContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public DataModelDtoConverter(
            final @NotNull EntityCreatorContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                generationContextData.getDtoModelNamespaceBuilder().getNamespace(),
                newEntityDialogData.getEntityName().concat("Data"),
                generationContextData.getModuleName(),
                generationContextData.getDtoModelNamespaceBuilder().getClassFqn(),
                generationContextData.getDtoInterfaceNamespaceBuilder().getClassFqn(),
                newEntityDialogData.getProperties(),
                newEntityDialogData.hasDtoInterface()
        );
    }
}
