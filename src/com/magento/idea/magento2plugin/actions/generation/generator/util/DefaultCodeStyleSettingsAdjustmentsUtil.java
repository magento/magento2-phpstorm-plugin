/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public final class DefaultCodeStyleSettingsAdjustmentsUtil {

    private DefaultCodeStyleSettingsAdjustmentsUtil() {
    }

    /**
     * Adjust default code style settings for project.
     *
     * @param project Project
     */
    public static void execute(final @NotNull Project project) {
        final CodeStyleSettings codeStyleSettings = CodeStyleSettingsManager
                .getInstance(project)
                .getTemporarySettings();

        if (codeStyleSettings == null) {
            return;
        }
        final CommonCodeStyleSettings commonPhpSettings = codeStyleSettings
                .getCommonSettings("PHP");
        // This value has changed to TRUE by default in the latest releases.
        // This is necessary to return the value by default to the previous one.
        commonPhpSettings.ALIGN_MULTILINE_PARAMETERS = false;
    }
}
