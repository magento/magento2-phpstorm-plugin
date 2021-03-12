/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.LayoutXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityManagerContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import com.magento.idea.magento2plugin.magento.files.actions.IndexActionFile;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import org.jetbrains.annotations.NotNull;

public class GridLayoutXmlDtoConverter extends LayoutXmlData implements DataObjectConverter {

    /**
     * Grid layout XML converter.
     *
     * @param generationContextData EntityManagerContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public GridLayoutXmlDtoConverter(
            final @NotNull EntityManagerContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                Areas.adminhtml.toString(),
                newEntityDialogData.getRoute(),
                generationContextData.getModuleName(),
                newEntityDialogData.getEntityName(),
                IndexActionFile.CLASS_NAME,
                newEntityDialogData.getGridName()
        );
    }
}
