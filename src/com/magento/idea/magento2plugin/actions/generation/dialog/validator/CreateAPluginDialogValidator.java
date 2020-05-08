/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.refactoring.PhpNameUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.CreateAPluginDialog;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import javax.swing.JOptionPane;
import java.util.List;

@SuppressWarnings({"PMD.OnlyOneReturn"})
public class CreateAPluginDialogValidator {
    private static CreateAPluginDialogValidator INSTANCE = null;
    private ValidatorBundle validatorBundle;
    private CommonBundle commonBundle;
    private CreateAPluginDialog dialog;

    /**
     * Get instance of a class.
     *
     * @param dialog Create plugin dialog
     *
     * @return CreateAPluginDialogValidator
     */
    public static CreateAPluginDialogValidator getInstance(CreateAPluginDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new CreateAPluginDialogValidator();
        }

        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public CreateAPluginDialogValidator() {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Validate whenever new create plugin dialog data is ready for generation.
     *
     * @param project Project
     *
     * @return Boolean
     */
    public boolean validate(Project project)
    {
        String errorTitle = commonBundle.message("common.error");
        String pluginClassName = dialog.getPluginClassName();

        if (!PhpNameUtil.isValidClassName(pluginClassName)) {
            String errorMessage = this.validatorBundle.message(
                    "validator.class.isNotValid",
                    "Plugin Class Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (pluginClassName.length() == 0) {
            String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "Plugin Class Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!pluginClassName.matches(RegExUtil.ALPHANUMERIC)) {
            String errorMessage = validatorBundle.message(
                    "validator.alphaNumericCharacters",
                    "Plugin Class Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!Character.isUpperCase(pluginClassName.charAt(0))
                && !Character.isDigit(pluginClassName.charAt(0))
        ) {
            String errorMessage = validatorBundle.message(
                    "validator.startWithNumberOrCapitalLetter",
                    "Plugin Class Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        String pluginDirectory = dialog.getPluginDirectory();
        if (pluginDirectory.length() == 0) {
            String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "Plugin Directory"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!pluginDirectory.matches(RegExUtil.DIRECTORY)) {
            String errorMessage = validatorBundle.message(
                    "validator.directory.isNotValid",
                    "Plugin Directory"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        String pluginName = dialog.getPluginName();
        if (pluginName.length() == 0) {
            String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "Plugin Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!pluginName.matches(RegExUtil.IDENTIFIER)) {
            String errorMessage = validatorBundle.message(
                    "validator.identifier",
                    "Plugin Name"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        String sortOrder = dialog.getPluginSortOrder();
        if (sortOrder.length() == 0) {
            String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "Sort Order"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        if (!sortOrder.matches(RegExUtil.NUMERIC)) {
            String errorMessage = validatorBundle.message(
                    "validator.onlyNumbers",
                    "Sort Order"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        String pluginModule = dialog.getPluginModule();
        if (pluginModule.length() == 0) {
            String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "Plugin Module"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        List<String> allModulesList = ModuleIndex.getInstance(project).getEditableModuleNames();
        if (!allModulesList.contains(pluginModule)) {
            String errorMessage = validatorBundle.message(
                    "validator.module.noSuchModule",
                    pluginModule
            );
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
