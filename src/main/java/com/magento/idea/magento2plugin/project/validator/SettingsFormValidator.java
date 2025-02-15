/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.validator;

import com.intellij.openapi.options.ConfigurationException;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.project.SettingsForm;
import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;
import com.magento.idea.magento2plugin.util.magento.MagentoVersionUtil;

public class SettingsFormValidator {
    private final SettingsForm form;
    private final ValidatorBundle validatorBundle;

    public SettingsFormValidator(
            final SettingsForm form
    ) {
        this.form = form;
        this.validatorBundle = new ValidatorBundle();
    }

    /**
     * Validates form if plugin is enabled.
     *
     * @throws ConfigurationException Exception
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.AvoidDeeplyNestedIfStmts"})
    public void validate() throws ConfigurationException {
        if (form.isBeingUsed()) {
            final String magentoRootPath = form.getMagentoPath();
            final boolean isMagentoFrameworkDirExist =
                    MagentoBasePathUtil.isMagentoFolderValid(magentoRootPath);

            if (!MagentoBasePathUtil.isComposerJsonExists(magentoRootPath)) {
                if (isMagentoFrameworkDirExist) {
                    throw new ConfigurationException(
                            validatorBundle.message("validator.package.validPathComposerFiles")
                    );
                }
                throw new ConfigurationException(
                        validatorBundle.message("validator.package.validPath")
                );
            }

            if (!isMagentoFrameworkDirExist) {
                throw new ConfigurationException(
                        validatorBundle.message("validator.package.validPathVendor")
                );
            }

            final String magentoVersion = form.getMagentoVersion();
            if (magentoVersion.length() == 0) {
                throw new ConfigurationException(
                        validatorBundle.message("validator.notEmpty", "Magento Version")
                );
            }

            if (!magentoVersion.matches(RegExUtil.MAGENTO_VERSION)
                    && !magentoVersion.equals(MagentoVersionUtil.DEFAULT_VERSION)) {
                throw new ConfigurationException(
                        validatorBundle.message("validator.magentoVersionInvalid")
                );
            }
        }
    }
}
