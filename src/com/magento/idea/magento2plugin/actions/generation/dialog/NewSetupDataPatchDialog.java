/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.context.php.NewSetupDataPatchAction;
import com.magento.idea.magento2plugin.actions.generation.ModuleSetupDataPatchData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleSetupDataPatchGenerator;
import com.magento.idea.magento2plugin.magento.files.ModuleSetupDataPatchFile;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

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
    private JLabel classNameLabel;//NOPMD
    private JLabel classNameErrorMessage;//NOPMD


    /**
     * Constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewSetupDataPatchDialog(
            final Project project,
            final PsiDirectory directory,
            final String modulePackage,
            final String moduleName
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
            final String modulePackage,
            final String moduleName
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
        final PsiDirectory targetDirectory = getDirectory(baseDir);

        if (NewSetupDataPatchAction.DATA_DIRECTORY.equals(targetDirectory.getName())) {
            final PsiFile[] files = targetDirectory.getFiles();
            for (final PsiFile file : files) {
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

    private PsiDirectory getDirectory(final PsiDirectory targetDirectory) {
        if (NewSetupDataPatchAction.ROOT_DIRECTORY.equals(baseDir.getName())) {
            final PsiDirectory subDirectoryPatch = baseDir.findSubdirectory(
                    NewSetupDataPatchAction.PATCH_DIRECTORY
            );

            if (subDirectoryPatch != null) {
                return subDirectoryPatch.findSubdirectory(NewSetupDataPatchAction.DATA_DIRECTORY);
            }
        }
        if (NewSetupDataPatchAction.PATCH_DIRECTORY.equals(baseDir.getName())) {
            return baseDir.findSubdirectory(NewSetupDataPatchAction.DATA_DIRECTORY);
        }

        return targetDirectory;
    }

    private PsiDirectory createDirectory(final PsiDirectory targetDirectory) {
        if (NewSetupDataPatchAction.ROOT_DIRECTORY.equals(targetDirectory.getName())) {
            final PsiDirectory subDirectoryPatch = targetDirectory.findSubdirectory(
                    NewSetupDataPatchAction.PATCH_DIRECTORY
            );

            if (subDirectoryPatch == null) {
                return targetDirectory.createSubdirectory(
                        NewSetupDataPatchAction.PATCH_DIRECTORY
                ).createSubdirectory(NewSetupDataPatchAction.DATA_DIRECTORY);
            }
            final PsiDirectory subDirectoryData = subDirectoryPatch.findSubdirectory(
                    NewSetupDataPatchAction.DATA_DIRECTORY
            );

            return Objects.requireNonNullElseGet(
                    subDirectoryData, () -> subDirectoryPatch.createSubdirectory(
                            NewSetupDataPatchAction.DATA_DIRECTORY
                    )
            );
        }
        if (NewSetupDataPatchAction.PATCH_DIRECTORY.equals(targetDirectory.getName())) {
            final PsiDirectory subDirectoryData = targetDirectory.findSubdirectory(
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
