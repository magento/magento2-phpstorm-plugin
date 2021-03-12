/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.RoutesXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityManagerContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import org.jetbrains.annotations.NotNull;

public class RoutesXmlDtoConverter extends RoutesXmlData implements DataObjectConverter {

    /**
     * Routes XML DTO converter.
     *
     * @param generationContextData EntityManagerContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public RoutesXmlDtoConverter(
            final @NotNull EntityManagerContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                Areas.adminhtml.toString(),
                newEntityDialogData.getRoute(),
                generationContextData.getModuleName()
        );
    }
}
