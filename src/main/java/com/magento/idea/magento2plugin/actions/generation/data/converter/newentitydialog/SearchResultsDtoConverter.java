/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import com.magento.idea.magento2plugin.actions.generation.data.php.SearchResultsData;
import org.jetbrains.annotations.NotNull;

public class SearchResultsDtoConverter extends SearchResultsData implements DataObjectConverter {

    /**
     * Save entity command DTO converter.
     *
     * @param generationContextData EntityCreatorContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public SearchResultsDtoConverter(
            final @NotNull EntityCreatorContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                generationContextData.getModuleName(),
                newEntityDialogData.getEntityName(),
                newEntityDialogData.hasDtoInterface()
                        ? generationContextData.getDtoInterfaceNamespaceBuilder().getClassFqn()
                        : generationContextData.getDtoModelNamespaceBuilder().getClassFqn()
        );
    }
}
