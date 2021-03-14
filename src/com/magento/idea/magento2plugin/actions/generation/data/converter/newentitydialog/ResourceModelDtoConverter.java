/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.ResourceModelData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityManagerContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class ResourceModelDtoConverter extends ResourceModelData implements DataObjectConverter {

    /**
     * Resource model DTO converter.
     *
     * @param generationContextData EntityManagerContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public ResourceModelDtoConverter(
            final @NotNull EntityManagerContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                generationContextData.getModuleName(),
                newEntityDialogData.getTableName(),
                newEntityDialogData.getEntityName().concat("Resource"),
                newEntityDialogData.getIdFieldName(),
                generationContextData.getResourceModelNamespaceBuilder().getNamespace(),
                generationContextData.getResourceModelNamespaceBuilder().getClassFqn()
        );
    }
}
