/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.NewActionEntityControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class NewControllerDtoConverter extends NewActionEntityControllerFileData
        implements DataObjectConverter {

    /**
     * New controller DTO converter.
     *
     * @param generationContextData EntityCreatorContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public NewControllerDtoConverter(
            final @NotNull EntityCreatorContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                newEntityDialogData.getEntityName(),
                generationContextData.getModuleName(),
                generationContextData.getNewControllerNamespaceBuilder().getNamespace(),
                newEntityDialogData.getAclId(),
                newEntityDialogData.getMenuId()
        );
    }
}
