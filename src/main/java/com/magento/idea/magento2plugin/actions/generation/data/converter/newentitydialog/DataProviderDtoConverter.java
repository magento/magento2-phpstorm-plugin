/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.UiComponentDataProviderData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import com.magento.idea.magento2plugin.magento.files.UiComponentDataProviderFile;
import org.jetbrains.annotations.NotNull;

public class DataProviderDtoConverter extends UiComponentDataProviderData
        implements DataObjectConverter {

    /**
     * Data provider converter.
     *
     * @param generationContextData EntityCreatorContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public DataProviderDtoConverter(
            final @NotNull EntityCreatorContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                newEntityDialogData.getEntityName().concat("DataProvider"),
                UiComponentDataProviderFile.DIRECTORY,
                newEntityDialogData.getEntityName(),
                newEntityDialogData.getIdFieldName(),
                newEntityDialogData.hasWebApi()
        );
    }
}
