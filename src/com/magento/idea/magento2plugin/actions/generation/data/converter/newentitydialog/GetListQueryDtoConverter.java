/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.GetListQueryModelData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class GetListQueryDtoConverter extends GetListQueryModelData
        implements DataObjectConverter {

    /**
     * Get list query converter.
     *
     * @param generationContextData EntityCreatorContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public GetListQueryDtoConverter(
            final @NotNull EntityCreatorContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                generationContextData.getModuleName(),
                newEntityDialogData.getEntityName(),
                newEntityDialogData.getEntityName().concat("Model"),
                newEntityDialogData.getEntityName().concat("Collection"),
                newEntityDialogData.getAclId(),
                newEntityDialogData.hasWebApi()
        );
    }
}
