/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.AdminListViewEntityActionData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityManagerContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class EntityListActionDtoConverter extends AdminListViewEntityActionData
        implements DataObjectConverter {

    /**
     * Entity list action converter.
     *
     * @param generationContextData EntityManagerContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public EntityListActionDtoConverter(
            final @NotNull EntityManagerContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                generationContextData.getModuleName(),
                newEntityDialogData.getEntityName(),
                generationContextData.getEntityListActionNamespaceBuilder().getNamespace(),
                generationContextData.getEntityListActionNamespaceBuilder().getClassFqn(),
                newEntityDialogData.getAclId(),
                newEntityDialogData.getMenuId()
        );
    }
}
