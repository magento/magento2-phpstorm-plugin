/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.UiComponentGridData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import com.magento.idea.magento2plugin.magento.files.UiComponentDataProviderFile;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import org.jetbrains.annotations.NotNull;

public class UiComponentGridDtoConverter extends UiComponentGridData
        implements DataObjectConverter {

    /**
     * Ui component grid DTO converter.
     *
     * @param generationContextData EntityCreatorContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public UiComponentGridDtoConverter(
            final @NotNull EntityCreatorContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                generationContextData.getModuleName(),
                Areas.adminhtml.toString(),
                newEntityDialogData.getGridName(),
                newEntityDialogData.getIdFieldName(),
                newEntityDialogData.getAclId(),
                newEntityDialogData.getEntityName().concat("DataProvider"),
                UiComponentDataProviderFile.DIRECTORY,
                newEntityDialogData.getEntityName(),
                new UiComponentGridToolbarDtoConverter(generationContextData, newEntityDialogData),
                generationContextData.getEntityProps()
        );
    }
}
