/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.generation.validator;

import com.magento.idea.magento2plugin.generation.NewModuleForm;
import com.magento.idea.magento2plugin.resources.ValidatorBundle;
import com.magento.idea.magento2plugin.util.Regex;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;

public class NewModuleFormValidator {
    private static NewModuleFormValidator INSTANCE = null;
    private NewModuleForm form;

    public static NewModuleFormValidator getInstance(NewModuleForm form) {
        if (null == INSTANCE) {
            INSTANCE = new NewModuleFormValidator();
        }
        INSTANCE.form = form;
        return INSTANCE;
    }

    public String validate()
    {
        String packageName = form.getPackageName();
        if (packageName.length() == 0) {
            return ValidatorBundle.message("validator.notEmpty", "Package Name");
        }

        if (!packageName.matches(Regex.ALPHANUMERIC)) {
            return ValidatorBundle.message("validator.alphaNumericCharacters", "Package Name");
        }

        if (!Character.isUpperCase(packageName.charAt(0)) && !Character.isDigit(packageName.charAt(0))) {
            return ValidatorBundle.message("validator.startWithNumberOrCapitalLetter", "Package Name");
        }

        String moduleName = form.getModuleName();
        if (moduleName.length() == 0) {
            return ValidatorBundle.message("validator.notEmpty", "Module Name");
        }

        if (!moduleName.matches(Regex.ALPHANUMERIC)) {
            return ValidatorBundle.message("validator.alphaNumericCharacters", "Module Name");
        }

        if (!Character.isUpperCase(moduleName.charAt(0)) && !Character.isDigit(moduleName.charAt(0))) {
            return ValidatorBundle.message("validator.startWithNumberOrCapitalLetter", "Module Name");
        }

        if (form.getModuleVersion().length() == 0) {
            return ValidatorBundle.message("validator.notEmpty", "Module Version");
        }

        if (form.getModuleDescription().length() == 0) {
            return ValidatorBundle.message("validator.notEmpty", "Module Description");
        }

        if (!MagentoBasePathUtil.isMagentoFolderValid(form.getMagentoPath())) {
            return ValidatorBundle.message("validator.package.validPath");
        }

        return null;
    }
}
