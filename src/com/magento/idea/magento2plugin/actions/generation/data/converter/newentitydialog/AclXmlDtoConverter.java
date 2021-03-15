/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.AclXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import org.jetbrains.annotations.NotNull;

public class AclXmlDtoConverter extends AclXmlData implements DataObjectConverter {

    /**
     * Acl XML DTO converter.
     *
     * @param generationContextData EntityCreatorContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public AclXmlDtoConverter(
            final @NotNull EntityCreatorContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                newEntityDialogData.getParentAclId(),
                newEntityDialogData.getAclId(),
                newEntityDialogData.getAclTitle()
        );
    }
}
