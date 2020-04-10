/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog.validator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.dialog.CreateAnObserverDialog;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.util.Regex;
import com.magento.idea.magento2plugin.validators.ValidatorBundle;
import javax.swing.*;
import java.util.List;

public class CreateAnObserverDialogValidator {
    private static CreateAnObserverDialogValidator INSTANCE = null;
    private CreateAnObserverDialog dialog;

    public static CreateAnObserverDialogValidator getInstance(CreateAnObserverDialog dialog) {
        if (null == INSTANCE) {
            INSTANCE = new CreateAnObserverDialogValidator();
        }
        INSTANCE.dialog = dialog;
        return INSTANCE;
    }

    public boolean validate(Project project)
    {
        String errorTitle = "Error";
        String observerClassName = dialog.getObserverClassName();
        if (observerClassName.length() == 0) {
            String errorMessage = ValidatorBundle.message("validator.notEmpty", "Observer Class Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!observerClassName.matches(Regex.ALPHANUMERIC)) {
            String errorMessage = ValidatorBundle.message("validator.alphaNumericCharacters", "Observer Class Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!Character.isUpperCase(observerClassName.charAt(0)) && !Character.isDigit(observerClassName.charAt(0))) {
            String errorMessage = ValidatorBundle.message("validator.startWithNumberOrCapitalLetter", "Observer Class Name");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        String observerDirectory = dialog.getObserverDirectory();

        if (observerDirectory.length() == 0) {
            String errorMessage = ValidatorBundle.message("validator.notEmpty", "Observer Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        if (!observerDirectory.matches(Regex.DIRECTORY)) {
            String errorMessage = ValidatorBundle.message("validator.directory.isNotValid", "Observer Directory");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }


        String observerModule = dialog.getObserverModule();
        if (observerModule.length() == 0) {
            String errorMessage = ValidatorBundle.message("validator.notEmpty", "Observer Module");
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        List<String> allModulesList = ModuleIndex.getInstance(project).getEditableModuleNames();
        if (!allModulesList.contains(observerModule)) {
            String errorMessage = ValidatorBundle.message("validator.module.noSuchModule", observerModule);
            JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        return true;
    }
}
