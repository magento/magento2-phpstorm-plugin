/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.dialog.NewCronjobDialog;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.util.RegExUtil;

import javax.swing.*;

public class NewCronjobValidator {
    private static NewCronjobValidator INSTANCE = null;
    private ValidatorBundle validatorBundle;
    private CommonBundle commonBundle;
    private GetPhpClassByFQN getPhpClassByFQN;

    public static NewCronjobValidator getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new NewCronjobValidator();
        }

        return INSTANCE;
    }

    public NewCronjobValidator() {
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Validate whenever new cronjob dialog data is ready for generation
     *
     * @return boolean
     */
    public boolean validate(Project project, NewCronjobDialog dialog) {
        String errorTitle = commonBundle.message("common.error");
        String cronjobClassName = dialog.getCronjobClassName();
        String cronjobDirectory = dialog.getCronjobDirectory();
        String cronjobName = dialog.getCronjobName();
        String moduleName = dialog.getCronjobModule();

        boolean isFixedScheduleType = dialog.isFixedScheduleType();
        String cronjobSchedule = dialog.getCronjobSchedule();

        boolean isConfigurableScheduleType = dialog.isConfigurableScheduleType();
        String scheduleConfigPath = dialog.getCronjobScheduleConfigPath();

        if (cronjobClassName.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "Cronjob ClassName");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }
        if (!cronjobClassName.matches(RegExUtil.ALPHANUMERIC)) {
            String errorMessage = validatorBundle.message(
                "validator.alphaNumericCharacters",
                "Cronjob ClassName"
            );
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }
        if (!Character.isUpperCase(cronjobClassName.charAt(0)) && !Character.isDigit(cronjobClassName.charAt(0))) {
            String errorMessage = validatorBundle.message(
                "validator.startWithNumberOrCapitalLetter",
                "Cronjob ClassName"
            );
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (cronjobName.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "Cronjob Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }
        if (!cronjobName.matches(RegExUtil.IDENTIFIER)) {
            String errorMessage = validatorBundle.message("validator.identifier", "Cronjob Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (cronjobDirectory.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "Cronjob Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }
        if (!cronjobDirectory.matches(RegExUtil.DIRECTORY)) {
            String errorMessage = validatorBundle.message(
                "validator.directory.isNotValid",
                "Cronjob Directory"
            );
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (isFixedScheduleType) {
            if (cronjobSchedule.length() == 0) {
                String errorMessage = validatorBundle.message("validator.notEmpty", "Cronjob Schedule");
                JOptionPane.showMessageDialog(null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
                );

                return false;
            }
            if (!cronjobSchedule.matches(RegExUtil.Magento.CRON_SCHEDULE)) {
                String errorMessage = validatorBundle.message(
                "validator.cronSchedule.invalidExpression",
                "Cronjob Schedule"
                );
                JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
                );

                return false;
            }
        }

        if (isConfigurableScheduleType) {
            if (scheduleConfigPath.length() == 0) {
                String errorMessage = validatorBundle.message(
                    "validator.notEmpty",
                    "Schedule Config Path"
                );
                JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
                );

                return false;
            }

            if (scheduleConfigPath.matches(RegExUtil.Magento.CONFIG_PATH)) {
                String errorMessage = validatorBundle.message(
                    "validator.configPath.invalidFormat",
                    "Schedule Config Path"
                );
                JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
                );

                return false;
            }
        }

        NamespaceBuilder namespaceBuilder = new NamespaceBuilder(
            moduleName,
            cronjobClassName,
            cronjobDirectory
        );
        PhpClass cronjobClassFile = GetPhpClassByFQN.getInstance(project).execute(namespaceBuilder.getClassFqn());

        if (cronjobClassFile != null) {
            String errorMessage = validatorBundle.message(
                "validator.file.alreadyExists",
                "Cronjob Class"
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
