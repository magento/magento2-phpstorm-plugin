/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.FormGenericButtonBlockData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityManagerContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class FormGenericButtonBlockDtoConverter extends FormGenericButtonBlockData
        implements DataObjectConverter {

    /**
     * Form generic button block converter.
     *
     * @param generationContextData EntityManagerContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public FormGenericButtonBlockDtoConverter(
            final @NotNull EntityManagerContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                generationContextData.getModuleName(),
                newEntityDialogData.getEntityName(),
                newEntityDialogData.getIdFieldName(),
                generationContextData.getGenericButtonBlockNamespaceBuilder().getClassFqn(),
                generationContextData.getGenericButtonBlockNamespaceBuilder().getNamespace()
        );
    }
}
