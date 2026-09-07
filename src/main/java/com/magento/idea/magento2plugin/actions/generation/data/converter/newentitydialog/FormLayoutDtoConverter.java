/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.LayoutXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import org.jetbrains.annotations.NotNull;

public class FormLayoutDtoConverter extends LayoutXmlData implements DataObjectConverter {

    private static final String VIEW_ACTION_NAME = "Edit";

    /**
     * Form layout converter.
     *
     * @param generationContextData EntityCreatorContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public FormLayoutDtoConverter(
            final @NotNull EntityCreatorContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                Areas.adminhtml.toString(),
                newEntityDialogData.getRoute(),
                generationContextData.getModuleName(),
                newEntityDialogData.getEntityName(),
                VIEW_ACTION_NAME,
                newEntityDialogData.getFormName()
        );
    }
}
