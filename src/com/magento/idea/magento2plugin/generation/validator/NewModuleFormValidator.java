/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.generation.validator;

import com.magento.idea.magento2plugin.generation.NewModuleForm;
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
            return "Package Name must not be empty.";
        }

        if (!packageName.matches(Regex.ALPHANUMERIC)) {
            return "Package Name must contain letters and numbers only.";
        }

        if (!Character.isUpperCase(packageName.charAt(0)) && !Character.isDigit(packageName.charAt(0))) {
            return "Package Name must start from a number or a capital letter";
        }

        String moduleName = form.getModuleName();
        if (moduleName.length() == 0) {
            return "Module Name must not be empty.";
        }

        if (!moduleName.matches(Regex.ALPHANUMERIC)) {
            return "Module Name must contain letters and numbers only.";
        }

        if (!Character.isUpperCase(moduleName.charAt(0)) && !Character.isDigit(moduleName.charAt(0))) {
            return "Module Name must start from a number or a capital letter";
        }

        if (form.getModuleVersion().length() == 0) {
            return "Module Version must not be empty.";
        }

        if (form.getModuleDescription().length() == 0) {
            return "Module Version must not be empty.";
        }

        if (!MagentoBasePathUtil.isMagentoFolderValid(form.getMagentoPath())) {
            return "Please specify valid magento installation path!";
        }

        return null;
    }
}
