/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.CollectionData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class CollectionModelDtoConverter extends CollectionData implements DataObjectConverter {

    /**
     * Collection model DTO converter.
     *
     * @param generationContextData EntityCreatorContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public CollectionModelDtoConverter(
            final @NotNull EntityCreatorContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                generationContextData.getModuleName(),
                newEntityDialogData.getTableName(),
                newEntityDialogData.getEntityName().concat("Model"),
                newEntityDialogData.getEntityName().concat("Resource"),
                newEntityDialogData.getEntityName().concat("Collection"),
                newEntityDialogData.getEntityName().concat("Model")
        );
    }
}
