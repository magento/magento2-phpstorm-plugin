/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.eavattribute;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewProductEavAttributeDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.eavattribute.EavAttributeDialog;

public class NewProductEavAttributeAction extends NewEavAttributeAction {

    public static final String ACTION_NAME = "Product Attribute";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 EAV Product Attribute";

    public NewProductEavAttributeAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    @Override
    protected EavAttributeDialog getDialogWindow(
            final Project project,
            final PsiDirectory directory
    ) {
        return new NewProductEavAttributeDialog(project, directory, ACTION_NAME);
    }
}
