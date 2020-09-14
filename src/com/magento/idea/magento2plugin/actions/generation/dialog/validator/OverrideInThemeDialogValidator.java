/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.dialog.OverrideInThemeDialog;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import java.util.List;
import javax.swing.JOptionPane;

public class OverrideInThemeDialogValidator {
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final OverrideInThemeDialog dialog;

    /**
     * Constructor.
     *
     * @param dialog OverrideInThemeDialog
     */
    public OverrideInThemeDialogValidator(final OverrideInThemeDialog dialog) {
        this.dialog = dialog;
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Validate dialog.
     *
     * @param project Project
     * @return boolean
     */
    public boolean validate(final Project project) {
        final String errorTitle = commonBundle.message("common.error");

        final String theme = dialog.getTheme();
        if (theme.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "Target Theme"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final List<String> allThemesList = ModuleIndex.getInstance(project).getEditableThemeNames();
        if (!allThemesList.contains(theme)) {
            final String errorMessage = validatorBundle
                    .message("validator.module.noSuchModule", theme);
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        return true;
    }
}
