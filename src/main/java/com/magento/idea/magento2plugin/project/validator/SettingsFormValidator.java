/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.validator;

import com.intellij.openapi.options.ConfigurationException;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.project.Settings;
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
            if (!Settings.isValidPublishedReleasesUrl(form.getPublishedReleasesUrl())) {
                throw new ConfigurationException(
                        validatorBundle.message("validator.publishedReleasesUrlInvalid")
                );
            }

            final String magentoRootPath = form.getMagentoPath();
            final com.magento.idea.magento2plugin.project.MagentoProjectConfigurationService configService =
                    com.magento.idea.magento2plugin.project.MagentoProjectConfigurationService.getInstance(form.getProject());
            final java.util.List<String> pathErrors = configService.validatePathForConfiguration(magentoRootPath, true);
            if (!pathErrors.isEmpty()) {
                throw new ConfigurationException(String.join("<br>", pathErrors));
            }

            final String resolved = configService.resolvePath(magentoRootPath);
            final com.magento.idea.magento2plugin.project.MagentoProjectConfigurationService.InstallationDetails details =
                    configService.inspectPath(resolved != null ? resolved : magentoRootPath);

            final String magentoVersion = form.getMagentoVersion();
            if (details.type() == com.magento.idea.magento2plugin.project.MagentoProjectConfigurationService.InstallationType.RECOGNIZED_APPLICATION) {
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
}
