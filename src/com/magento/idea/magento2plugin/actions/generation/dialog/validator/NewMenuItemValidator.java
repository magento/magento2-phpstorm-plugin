/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator;


import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewCLICommandDialog;

@SuppressWarnings({
        "PMD.FieldNamingConventions",
        "PMD.RedundantFieldInitializer",
        "PMD.OnlyOneReturn",
        "PMD.AvoidDuplicateLiterals",
})
public class NewMenuItemValidator {
    private static NewMenuItemValidator INSTANCE = null;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;

    /**
     * Returns a new instance of a class.
     *
     * @return NewMenuItemValidator
     */
    public static NewMenuItemValidator getInstance() {
        if (null != INSTANCE) {
            return INSTANCE;
        }
        INSTANCE = new NewMenuItemValidator();

        return INSTANCE;
    }

    public NewMenuItemValidator() {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Validate new menu item command form data.
     *
     * @param project Project
     * @param dialog Dialog
     * @return boolen
     */
    public boolean validate(final Project project, final NewCLICommandDialog dialog) {
        return true;
    }
}
