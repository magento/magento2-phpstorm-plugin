/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.eavattribute;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewCategoryEavAttributeDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.eavattribute.EavAttributeDialog;

public class NewCategoryEavAttributeAction extends NewEavAttributeAction {

    public static final String ACTION_NAME = "Category Attribute";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 EAV Catalog Attribute";

    public NewCategoryEavAttributeAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    @Override
    protected EavAttributeDialog getDialogWindow(
            final Project project,
            final PsiDirectory directory
    ) {
        return new NewCategoryEavAttributeDialog(project, directory, ACTION_NAME);
    }
}
