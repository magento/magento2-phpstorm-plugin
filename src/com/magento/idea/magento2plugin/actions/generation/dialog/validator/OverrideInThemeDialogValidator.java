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

import javax.swing.*;
import java.util.List;

public class OverrideInThemeDialogValidator {
    private static OverrideInThemeDialogValidator INSTANCE = null;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private OverrideInThemeDialog dialog;

    public static OverrideInThemeDialogValidator getInstance(OverrideInThemeDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new OverrideInThemeDialogValidator();
        }

        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public OverrideInThemeDialogValidator() {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    public boolean validate(Project project)
    {
        String errorTitle = commonBundle.message("common.error");

        String theme = dialog.getTheme();
        if (theme.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "Target Theme");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        List<String> allThemesList = ModuleIndex.getInstance(project).getEditableThemeNames();
        if (!allThemesList.contains(theme)) {
            String errorMessage = validatorBundle.message("validator.module.noSuchModule", theme);
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        return true;
    }
}
