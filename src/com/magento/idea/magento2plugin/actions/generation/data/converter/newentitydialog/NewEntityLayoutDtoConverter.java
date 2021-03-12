/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.NewEntityLayoutData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityManagerContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class NewEntityLayoutDtoConverter extends NewEntityLayoutData
        implements DataObjectConverter {

    /**
     * New entity layout DTO converter.
     *
     * @param generationContextData EntityManagerContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public NewEntityLayoutDtoConverter(
            final @NotNull EntityManagerContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                generationContextData.getModuleName(),
                generationContextData.getNewViewAction(),
                generationContextData.getEditViewAction()
        );
    }
}
