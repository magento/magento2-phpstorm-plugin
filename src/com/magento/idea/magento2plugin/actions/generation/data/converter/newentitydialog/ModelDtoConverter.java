/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.ModelData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class ModelDtoConverter extends ModelData implements DataObjectConverter {

    /**
     * Model DTO converter.
     *
     * @param generationContextData EntityCreatorContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public ModelDtoConverter(
            final @NotNull EntityCreatorContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                generationContextData.getModuleName(),
                newEntityDialogData.getTableName(),
                newEntityDialogData.getEntityName().concat("Model"),
                newEntityDialogData.getEntityName().concat("Resource")
        );
    }
}
