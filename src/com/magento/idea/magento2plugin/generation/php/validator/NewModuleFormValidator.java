/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.generation.php.validator;

import com.magento.idea.magento2plugin.generation.php.NewModuleForm;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;

public class NewModuleFormValidator {
    private static NewModuleFormValidator INSTANCE = null;
    private NewModuleForm form;
    private ValidatorBundle validatorBundle;

    public static NewModuleFormValidator getInstance(NewModuleForm form) {
        if (null == INSTANCE) {
            INSTANCE = new NewModuleFormValidator();
        }
        INSTANCE.form = form;
        return INSTANCE;
    }

    public NewModuleFormValidator() {
        this.validatorBundle = new ValidatorBundle();
    }

    public String validate()
    {
        String packageName = form.getPackageName();
        if (packageName.length() == 0) {
            return validatorBundle.message("validator.notEmpty", "Package Name");
        }

        if (!packageName.matches(RegExUtil.ALPHANUMERIC)) {
            return validatorBundle.message("validator.alphaNumericCharacters", "Package Name");
        }

        if (!Character.isUpperCase(packageName.charAt(0)) && !Character.isDigit(packageName.charAt(0))) {
            return validatorBundle.message("validator.startWithNumberOrCapitalLetter", "Package Name");
        }

        String moduleName = form.getModuleName();
        if (moduleName.length() == 0) {
            return validatorBundle.message("validator.notEmpty", "Module Name");
        }

        if (!moduleName.matches(RegExUtil.ALPHANUMERIC)) {
            return validatorBundle.message("validator.alphaNumericCharacters", "Module Name");
        }

        if (!Character.isUpperCase(moduleName.charAt(0)) && !Character.isDigit(moduleName.charAt(0))) {
            return validatorBundle.message("validator.startWithNumberOrCapitalLetter", "Module Name");
        }

        if (form.getModuleVersion().length() == 0) {
            return validatorBundle.message("validator.notEmpty", "Module Version");
        }

        if (form.getModuleDescription().length() == 0) {
            return validatorBundle.message("validator.notEmpty", "Module Description");
        }

        if (!MagentoBasePathUtil.isMagentoFolderValid(form.getMagentoPath())) {
            return validatorBundle.message("validator.package.validPath");
        }

        return null;
    }
}
