/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFileData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import com.magento.idea.magento2plugin.magento.files.UiComponentDataProviderFile;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import org.jetbrains.annotations.NotNull;

public class UiComponentFormLayoutDtoConverter extends UiComponentFormFileData
        implements DataObjectConverter {

    private static final String SUBMIT_ACTION_NAME = "Save";

    /**
     * Ui Component form layout DTO converter.
     *
     * @param generationContextData EntityCreatorContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public UiComponentFormLayoutDtoConverter(
            final @NotNull EntityCreatorContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                newEntityDialogData.getFormName(),
                Areas.adminhtml.toString(),
                generationContextData.getModuleName(),
                newEntityDialogData.getFormLabel(),
                generationContextData.getButtons(),
                generationContextData.getFieldsetData(),
                generationContextData.getFieldsData(),
                newEntityDialogData.getRoute(),
                newEntityDialogData.getEntityName(),
                SUBMIT_ACTION_NAME,
                newEntityDialogData.getEntityName().concat("DataProvider"),
                UiComponentDataProviderFile.DIRECTORY,
                newEntityDialogData.getEntityName(),
                newEntityDialogData.getIdFieldName()
        );
    }
}
