/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.NewSetupDataPatch;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.context.php.NewSetupDataPatchAction;
import com.magento.idea.magento2plugin.actions.generation.ModuleSetupDataPatchData;
import com.magento.idea.magento2plugin.actions.generation.dialog.AbstractDialog;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleSetupDataPatchGenerator;
import com.magento.idea.magento2plugin.magento.files.ModuleSetupDataPatchFile;


import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

public class NewSetupDataPatchDialog extends AbstractDialog {

    private static final String CLASS_NAME = "Class Name";
    private final Project project;
    private final PsiDirectory baseDir;
    private final String moduleName;
    private final String modulePackage;

    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CLASS_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, CLASS_NAME})
    private JTextField className;
    private JLabel classNameLabel;
    private JLabel classNameErrorMessage;


    /**
     * Constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewSetupDataPatchDialog(
            final Project project,
            final PsiDirectory directory,
            String modulePackage,
            String moduleName
    ) {
        super();

        this.project = project;
        this.baseDir = directory;
        this.modulePackage = modulePackage;
        this.moduleName = moduleName;

        setContentPane(contentPanel);
        setModal(true);
        setTitle(NewSetupDataPatchAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener((final ActionEvent event) -> onOK());
        buttonCancel.addActionListener((final ActionEvent event) -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        addComponentListener(
                new FocusOnAFieldListener(() -> className.requestFocusInWindow())
        );
    }

    /**
     * Open dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(
            final Project project,
            final PsiDirectory directory,
            String modulePackage,
            String moduleName
    ) {
        final NewSetupDataPatchDialog dialog = new NewSetupDataPatchDialog(
                project,
                directory,
                modulePackage,
                moduleName
        );
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    protected void onOK() {
        if (validateFields()) {
            generateFile();
            exit();
        }
    }

    private void generateFile() {
        final ModuleSetupDataPatchGenerator generator = new ModuleSetupDataPatchGenerator(
                new ModuleSetupDataPatchData(
                        modulePackage,
                        moduleName,
                        createDirectory(baseDir),
                        getClassName()
                ),
                project
        );

        generator.generate(NewSetupDataPatchAction.ACTION_NAME, true);
    }

    public String getClassName() {
        return className.getText();
    }

    private boolean validateFields() {
        PsiDirectory targetDirectory = getDirectory(baseDir);

        if (targetDirectory.getName().equals(NewSetupDataPatchAction.DATA_DIRECTORY)) {
            final PsiFile[] files = targetDirectory.getFiles();
            for (PsiFile file : files) {
                if (file.getName().equals(getClassName() + ModuleSetupDataPatchFile.FILE_NAME)) {
                    showErrorMessage(
                            fieldsValidationsList.get(0).getField(),
                            "Class name " + getClassName() + " already exist."
                    );

                    return false;
                }
            }
        }
        return validateFormFields();
    }

    private PsiDirectory getDirectory(PsiDirectory targetDirectory){
        if(baseDir.getName().equals(NewSetupDataPatchAction.ROOT_DIRECTORY)) {
            PsiDirectory subDirectoryPatch =
                    baseDir.findSubdirectory(NewSetupDataPatchAction.PATCH_DIRECTORY);

            if (subDirectoryPatch != null) {
                return subDirectoryPatch.findSubdirectory(NewSetupDataPatchAction.DATA_DIRECTORY);
            }
        }
        if(baseDir.getName().equals(NewSetupDataPatchAction.PATCH_DIRECTORY)) {
            return baseDir.findSubdirectory(NewSetupDataPatchAction.DATA_DIRECTORY);
        }

        return targetDirectory;
    }

    private PsiDirectory createDirectory(PsiDirectory targetDirectory) {
        if(targetDirectory.getName().equals(NewSetupDataPatchAction.ROOT_DIRECTORY)){
            PsiDirectory subDirectoryPatch = targetDirectory.findSubdirectory(
                    NewSetupDataPatchAction.PATCH_DIRECTORY
            );

            if(subDirectoryPatch == null) {
                return targetDirectory.createSubdirectory(
                        NewSetupDataPatchAction.PATCH_DIRECTORY
                ).createSubdirectory(NewSetupDataPatchAction.DATA_DIRECTORY);
            }
            PsiDirectory subDirectoryData = subDirectoryPatch.findSubdirectory(
                    NewSetupDataPatchAction.DATA_DIRECTORY
            );

            return Objects.requireNonNullElseGet(
                    subDirectoryData, () -> subDirectoryPatch.createSubdirectory(
                            NewSetupDataPatchAction.DATA_DIRECTORY
                    )
            );
        }
        if(targetDirectory.getName().equals(NewSetupDataPatchAction.PATCH_DIRECTORY)){
            PsiDirectory subDirectoryData = targetDirectory.findSubdirectory(
                    NewSetupDataPatchAction.DATA_DIRECTORY
            );

            return Objects.requireNonNullElseGet(
                    subDirectoryData, () -> targetDirectory.createSubdirectory(
                            NewSetupDataPatchAction.DATA_DIRECTORY
                    )
            );
        }

        return targetDirectory;
    }
}
