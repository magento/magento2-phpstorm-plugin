/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewCustomerEavAttributeDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.eavattribute.EavAttributeDialog;
import com.magento.idea.magento2plugin.actions.generation.eavattribute.NewEavAttributeAction;

public class NewCustomerEavAttributeAction extends NewEavAttributeAction {

    public static final String ACTION_NAME = "Customer Attribute";
    public static final String ACTION_DESCRIPTION = "Create a new Magento 2 EAV Customer Attribute";

    public NewCustomerEavAttributeAction() {
        super(ACTION_NAME, ACTION_DESCRIPTION, MagentoIcons.MODULE);
    }

    @Override
    protected EavAttributeDialog getDialogWindow(
            final Project project,
            final PsiDirectory directory
    ) {
        return new NewCustomerEavAttributeDialog(project, directory, ACTION_NAME);
    }
}
