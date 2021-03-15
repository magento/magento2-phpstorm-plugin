/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.CollectionData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import com.magento.idea.magento2plugin.magento.files.ResourceModelFile;
import com.magento.idea.magento2plugin.magento.packages.File;
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
                newEntityDialogData.getEntityName().concat("Collection"),
                generationContextData.getCollectionModelNamespaceBuilder().getClassFqn(),
                ResourceModelFile.RESOURCE_MODEL_DIRECTORY + File.separator
                        + newEntityDialogData.getEntityName(),
                generationContextData.getCollectionModelNamespaceBuilder().getNamespace(),
                newEntityDialogData.getEntityName().concat("Resource"),
                generationContextData.getResourceModelNamespaceBuilder().getClassFqn(),
                generationContextData.getModelNamespaceBuilder().getClassFqn()
        );
    }
}
