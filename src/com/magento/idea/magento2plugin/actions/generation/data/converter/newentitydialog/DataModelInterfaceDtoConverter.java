/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.DataModelInterfaceData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityManagerContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class DataModelInterfaceDtoConverter extends DataModelInterfaceData
        implements DataObjectConverter {

    /**
     * DTO interface converter.
     * @param generationContextData EntityManagerContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public DataModelInterfaceDtoConverter(
            final @NotNull EntityManagerContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                generationContextData.getDtoInterfaceNamespaceBuilder().getNamespace(),
                newEntityDialogData.getEntityName().concat("Interface"),
                generationContextData.getModuleName(),
                generationContextData.getDtoInterfaceNamespaceBuilder().getClassFqn(),
                newEntityDialogData.getProperties()
        );
    }
}
