/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.DeleteEntityByIdCommandData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityManagerContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class DeleteEntityByIdCommandDtoConverter extends DeleteEntityByIdCommandData
        implements DataObjectConverter {

    /**
     * Delete entity by id command DTO converter.
     *
     * @param generationContextData EntityManagerContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public DeleteEntityByIdCommandDtoConverter(
            final @NotNull EntityManagerContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                generationContextData.getModuleName(),
                newEntityDialogData.getEntityName(),
                generationContextData.getDeleteEntityByIdCommandNamespaceBuilder().getNamespace(),
                generationContextData.getDeleteEntityByIdCommandNamespaceBuilder().getClassFqn(),
                generationContextData.getModelNamespaceBuilder().getClassFqn(),
                generationContextData.getResourceModelNamespaceBuilder().getClassFqn(),
                newEntityDialogData.getIdFieldName()
        );
    }
}
