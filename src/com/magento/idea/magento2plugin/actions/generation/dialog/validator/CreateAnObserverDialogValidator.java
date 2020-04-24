/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.dialog.CreateAnObserverDialog;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import javax.swing.*;
import java.util.List;

public class CreateAnObserverDialogValidator {
    private static CreateAnObserverDialogValidator INSTANCE = null;
    private ValidatorBundle validatorBundle;
    private CreateAnObserverDialog dialog;

    public static CreateAnObserverDialogValidator getInstance(CreateAnObserverDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new CreateAnObserverDialogValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public CreateAnObserverDialogValidator() {
        this.validatorBundle = new ValidatorBundle();
    }

    public boolean validate(Project project)
    {
        String errorTitle = "Error";
        String observerClassName = dialog.getObserverClassName();
        if (observerClassName.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "Observer Class Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!observerClassName.matches(RegExUtil.ALPHANUMERIC)) {
            String errorMessage = validatorBundle.message("validator.alphaNumericCharacters", "Observer Class Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!Character.isUpperCase(observerClassName.charAt(0)) && !Character.isDigit(observerClassName.charAt(0))) {
            String errorMessage = validatorBundle.message("validator.startWithNumberOrCapitalLetter", "Observer Class Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        String observerDirectory = dialog.getObserverDirectory();

        if (observerDirectory.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "Observer Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!observerDirectory.matches(RegExUtil.DIRECTORY)) {
            String errorMessage = validatorBundle.message("validator.directory.isNotValid", "Observer Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }


        String observerModule = dialog.getObserverModule();
        if (observerModule.length() == 0) {
            String errorMessage = validatorBundle.message("validator.notEmpty", "Observer Module");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        List<String> allModulesList = ModuleIndex.getInstance(project).getEditableModuleNames();
        if (!allModulesList.contains(observerModule)) {
            String errorMessage = validatorBundle.message("validator.module.noSuchModule", observerModule);
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        return true;
    }
}
