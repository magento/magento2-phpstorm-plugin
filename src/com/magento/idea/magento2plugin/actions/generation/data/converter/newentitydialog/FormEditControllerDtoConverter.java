/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.EditEntityActionData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class FormEditControllerDtoConverter extends EditEntityActionData
        implements DataObjectConverter {

    /**
     * Form edit controller converter.
     *
     * @param generationContextData EntityCreatorContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public FormEditControllerDtoConverter(
            final @NotNull EntityCreatorContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                newEntityDialogData.getEntityName(),
                generationContextData.getModuleName(),
                newEntityDialogData.getAclId(),
                newEntityDialogData.getMenuId()
        );
    }
}
