/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.SaveEntityCommandData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class SaveEntityCommandDtoConverter extends SaveEntityCommandData
        implements DataObjectConverter {

    /**
     * Save entity command DTO converter.
     *
     * @param generationContextData EntityCreatorContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public SaveEntityCommandDtoConverter(
            final @NotNull EntityCreatorContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                generationContextData.getModuleName(),
                newEntityDialogData.getEntityName(),
                generationContextData.getSaveEntityCommandNamespaceBuilder().getNamespace(),
                generationContextData.getSaveEntityCommandNamespaceBuilder().getClassFqn(),
                generationContextData.getModelNamespaceBuilder().getClassFqn(),
                generationContextData.getResourceModelNamespaceBuilder().getClassFqn(),
                generationContextData.getFinalDtoTypeNamespaceBuilder().getClassFqn()
        );
    }
}
