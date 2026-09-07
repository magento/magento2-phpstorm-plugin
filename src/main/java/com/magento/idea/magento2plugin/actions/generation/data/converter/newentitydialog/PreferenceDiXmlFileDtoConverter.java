/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.PreferenceDiXmFileData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import org.jetbrains.annotations.NotNull;

public class PreferenceDiXmlFileDtoConverter extends PreferenceDiXmFileData
        implements DataObjectConverter {

    /**
     * Preference DI XML file DTO converter.
     *
     * @param generationContextData EntityCreatorContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public PreferenceDiXmlFileDtoConverter(
            final @NotNull EntityCreatorContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                generationContextData.getModuleName(),
                generationContextData.getDtoInterfaceNamespaceBuilder().getClassFqn(),
                generationContextData.getDtoModelNamespaceBuilder().getClassFqn(),
                Areas.base.toString()
        );
    }
}
