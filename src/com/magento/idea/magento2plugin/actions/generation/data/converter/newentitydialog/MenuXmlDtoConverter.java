/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.MenuXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class MenuXmlDtoConverter extends MenuXmlData implements DataObjectConverter {

    /**
     * Menu XML DTO converter.
     *
     * @param generationContextData EntityCreatorContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public MenuXmlDtoConverter(
            final @NotNull EntityCreatorContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                newEntityDialogData.getParentMenuId(),
                String.valueOf(newEntityDialogData.getMenuSortOrder()),
                generationContextData.getModuleName(),
                newEntityDialogData.getMenuId(),
                newEntityDialogData.getMenuTitle(),
                newEntityDialogData.getAclId(),
                generationContextData.getIndexViewAction()
        );
    }
}
