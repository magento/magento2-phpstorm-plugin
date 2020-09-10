/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project.validator;

import com.intellij.openapi.options.ConfigurationException;
import com.magento.idea.magento2plugin.util.magento.MagentoVersionUtil;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.project.SettingsForm;
import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;

public class SettingsFormValidator {
    private static SettingsFormValidator INSTANCE = null;
    private SettingsForm form;
    private ValidatorBundle validatorBundle;

    public static SettingsFormValidator getInstance(SettingsForm form) {
        if (null == INSTANCE) {
            INSTANCE = new SettingsFormValidator();
        }
        INSTANCE.form = form;
        return INSTANCE;
    }

    public SettingsFormValidator() {
        this.validatorBundle = new ValidatorBundle();
    }

    public void validate() throws ConfigurationException {
        if (!form.getSettings().pluginEnabled) {
            return;
        }

        if (!MagentoBasePathUtil.isMagentoFolderValid(form.getMagentoPath())) {
            throw new ConfigurationException(validatorBundle.message("validator.package.validPath"));
        }

        String magentoVersion = form.getMagentoVersion();
        if (magentoVersion.length() == 0) {
            throw new ConfigurationException(validatorBundle.message("validator.notEmpty", "Magento Version"));
        }

        if (!magentoVersion.matches(RegExUtil.MAGENTO_VERSION) && !magentoVersion.equals(MagentoVersionUtil.DEFAULT_VERSION)) {
            throw new ConfigurationException(validatorBundle.message("validator.magentoVersionInvalid"));
        }
    }
}