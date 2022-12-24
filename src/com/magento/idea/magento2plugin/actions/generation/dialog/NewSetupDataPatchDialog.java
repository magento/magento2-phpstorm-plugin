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
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.magento.files.ModuleSetupDataPatchFile;
import com.magento.idea.magento2plugin.magento.packages.File;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

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
            final @NotNull Project project,
            final @NotNull PsiDirectory directory,
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
            final @NotNull Project project,
            final @NotNull PsiDirectory directory,
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
        final PsiDirectory directory = DirectoryGenerator.getInstance().findOrCreateSubdirectories(
                baseDir,
                NewSetupDataPatchAction.PATCH_DIRECTORY + File.separator
                        + NewSetupDataPatchAction.DATA_DIRECTORY
        );
        final ModuleSetupDataPatchGenerator generator = new ModuleSetupDataPatchGenerator(
                new ModuleSetupDataPatchData(
                        modulePackage,
                        moduleName,
                        directory,
                        getClassName()
                ),
                project
        );

        generator.generate(NewSetupDataPatchAction.ACTION_NAME, true);
    }

    public String getClassName() {
        return className.getText().trim();
    }

    private boolean validateFields() {
        final PsiDirectory patchDirectory = baseDir.findSubdirectory(
                NewSetupDataPatchAction.PATCH_DIRECTORY
        );
        PsiDirectory directory = null;

        if (patchDirectory != null) {
            directory = patchDirectory.findSubdirectory(NewSetupDataPatchAction.DATA_DIRECTORY);
        }

        if (directory != null) {
            for (final PsiFile file : directory.getFiles()) {
                final String className = ModuleSetupDataPatchFile.resolveClassNameFromInput(
                        getClassName()
                );

                if (file.getName().equals(className + ModuleSetupDataPatchFile.EXTENSION)) {
                    showErrorMessage(
                            fieldsValidationsList.get(0).getField(),
                            "Class name `" + className + "` already exist."
                    );

                    return false;
                }
            }
        }

        return validateFormFields();
    }
}
