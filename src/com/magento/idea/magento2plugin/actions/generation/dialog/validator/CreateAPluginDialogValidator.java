/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.refactoring.PhpNameUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.CreateAPluginDialog;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.util.RegExUtil;
import java.util.List;
import javax.swing.JOptionPane;

@SuppressWarnings({"PMD.OnlyOneReturn", "PMD.FieldNamingConventions"})
public class CreateAPluginDialogValidator {
    private static final String NOT_EMPTY = "validator.notEmpty";
    private static final String PLUGIN_CLASS_NAME = "Plugin Class Name";
    private static CreateAPluginDialogValidator INSTANCE = null;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
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

    /**
     * Create a plugin dialog validator.
     */
    private CreateAPluginDialogValidator() {
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
    public boolean validate(final Project project) {
        final String errorTitle = commonBundle.message("common.error");
        final String pluginClassName = dialog.getPluginClassName();

        if (!PhpNameUtil.isValidClassName(pluginClassName)) {
            final String errorMessage = this.validatorBundle.message(
                    "validator.class.isNotValid",
                    PLUGIN_CLASS_NAME
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
            final String errorMessage = validatorBundle.message(
                    NOT_EMPTY,
                    PLUGIN_CLASS_NAME
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
            final String errorMessage = validatorBundle.message(
                    "validator.alphaNumericCharacters",
                    PLUGIN_CLASS_NAME
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
            final String errorMessage = validatorBundle.message(
                    "validator.startWithNumberOrCapitalLetter",
                    PLUGIN_CLASS_NAME
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        final String pluginDirectory = dialog.getPluginDirectory();
        if (pluginDirectory.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    NOT_EMPTY,
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
            final String errorMessage = validatorBundle.message(
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

        final String pluginName = dialog.getPluginName();
        if (pluginName.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    NOT_EMPTY,
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
            final String errorMessage = validatorBundle.message(
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

        final String sortOrder = dialog.getPluginSortOrder();
        if (sortOrder.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    NOT_EMPTY,
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
            final String errorMessage = validatorBundle.message(
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

        final String pluginModule = dialog.getPluginModule();
        if (pluginModule.length() == 0) {
            final String errorMessage = validatorBundle.message(
                    NOT_EMPTY,
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

        final List<String> allModulesList = ModuleIndex
                .getInstance(project).getEditableModuleNames();
        if (!allModulesList.contains(pluginModule)) {
            final String errorMessage = validatorBundle.message(
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
