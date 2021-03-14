/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog;

import com.magento.idea.magento2plugin.actions.generation.data.ControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.data.converter.DataObjectConverter;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityManagerContextData;
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData;
import com.magento.idea.magento2plugin.magento.files.ControllerBackendPhp;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import org.jetbrains.annotations.NotNull;

public class FormViewControllerDtoConverter extends ControllerFileData
        implements DataObjectConverter {

    private static final String VIEW_ACTION_NAME = "Edit";

    /**
     * Form view controller converter.
     *
     * @param generationContextData EntityManagerContextData
     * @param newEntityDialogData NewEntityDialogData
     */
    public FormViewControllerDtoConverter(
            final @NotNull EntityManagerContextData generationContextData,
            final @NotNull NewEntityDialogData newEntityDialogData
    ) {
        super(
                ControllerBackendPhp.DEFAULT_DIR  + File.separator +
                        newEntityDialogData.getEntityName(),
                VIEW_ACTION_NAME,
                generationContextData.getModuleName(),
                Areas.adminhtml.toString(),
                HttpMethod.GET.toString(),
                newEntityDialogData.getAclId(),
                true,
                generationContextData.getFormViewNamespaceBuilder().getNamespace()
        );
    }
}
