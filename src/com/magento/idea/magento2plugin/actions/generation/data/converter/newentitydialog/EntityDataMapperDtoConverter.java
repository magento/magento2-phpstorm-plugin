/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.EntityDataMapperData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityManagerContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class EntityDataMapperDtoConverter extends EntityDataMapperData
        implements DataObjectConverter {

    /**
     * Entity data mapper converter.
     *
     * @param generationContextData EntityManagerContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public EntityDataMapperDtoConverter(
            final @NotNull EntityManagerContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                generationContextData.getModuleName(),
                newEntityDialogData.getEntityName(),
                generationContextData.getEntityDataMapperNamespaceBuilder().getNamespace(),
                generationContextData.getEntityDataMapperNamespaceBuilder().getClassFqn(),
                generationContextData.getModelNamespaceBuilder().getClassFqn(),
                generationContextData.getFinalDtoTypeNamespaceBuilder().getClassFqn()
        );
    }
}
