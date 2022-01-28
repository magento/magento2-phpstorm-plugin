/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewViewModelAction;
import com.magento.idea.magento2plugin.actions.generation.data.ViewModelFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.DirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.StartWithNumberOrCapitalLetterRule;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleViewModelClassGenerator;
import com.magento.idea.magento2plugin.magento.files.ViewModelPhp;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
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

public class NewViewModelDialog extends AbstractDialog {

    private static final String VIEW_MODEL_NAME = "View Model Name";
    private static final String VIEW_MODEL_DIR = "View Model Directory";

    private final Project project;
    private final PsiDirectory baseDir;
    private final String moduleName;

    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, VIEW_MODEL_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, VIEW_MODEL_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHANUMERIC,
            message = {AlphanumericRule.MESSAGE, VIEW_MODEL_NAME})
    @FieldValidation(rule = RuleRegistry.START_WITH_NUMBER_OR_CAPITAL_LETTER,
            message = {StartWithNumberOrCapitalLetterRule.MESSAGE, VIEW_MODEL_NAME})
    private JTextField viewModelName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, VIEW_MODEL_DIR})
    @FieldValidation(rule = RuleRegistry.DIRECTORY,
            message = {DirectoryRule.MESSAGE, VIEW_MODEL_DIR})
    private JTextField viewModelParentDir;

    private JLabel viewModelNameErrorMessage;//NOPMD
    private JLabel viewModelParentDirErrorMessage;//NOPMD

    /**
     * Constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewViewModelDialog(final Project project, final PsiDirectory directory) {
        super();

        this.project = project;
        this.baseDir = directory;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        setContentPane(contentPanel);
        setModal(true);
        setTitle(NewViewModelAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);
        suggestViewModelDirectory();

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
                new FocusOnAFieldListener(() -> viewModelName.requestFocusInWindow())
        );
    }

    /**
     * Open dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewViewModelDialog dialog = new NewViewModelDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    protected void onOK() {
        if (validateFormFields()) {
            generateFile();
            exit();
        }
    }

    private void generateFile() {
        new ModuleViewModelClassGenerator(new ViewModelFileData(
                getViewModelDirectory(),
                getViewModelName(),
                getModuleName(),
                getNamespace()
        ), project).generate(NewViewModelAction.ACTION_NAME, true);
    }

    private String getModuleName() {
        return moduleName;
    }

    public String getViewModelName() {
        return viewModelName.getText().trim();
    }

    public String getViewModelDirectory() {
        return viewModelParentDir.getText().trim();
    }

    private void suggestViewModelDirectory() {
        final String moduleIdentifierPath = getModuleIdentifierPath();
        if (moduleIdentifierPath == null) {
            viewModelParentDir.setText(ViewModelPhp.DEFAULT_DIR);
            return;
        }

        final String path = baseDir.getVirtualFile().getPath();
        final String[] pathParts = path.split(moduleIdentifierPath);
        final int partsMaxLength = 2;
        if (pathParts.length != partsMaxLength) {
            viewModelParentDir.setText(ViewModelPhp.DEFAULT_DIR);
            return;
        }

        if (pathParts[1] != null) {
            viewModelParentDir.setText(pathParts[1].substring(1));
            return;
        }
        viewModelParentDir.setText(ViewModelPhp.DEFAULT_DIR);
    }

    private String getModuleIdentifierPath() {
        final String[]parts = moduleName.split(Package.vendorModuleNameSeparator);
        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }
        return parts[0] + File.separator + parts[1];
    }

    private String getNamespace() {
        final String[]parts = moduleName.split(Package.vendorModuleNameSeparator);
        if (parts[0] == null || parts[1] == null || parts.length > 2) {
            return null;
        }
        final String directoryPart = getViewModelDirectory().replace(
                File.separator,
                Package.fqnSeparator
        );
        return parts[0] + Package.fqnSeparator + parts[1] + Package.fqnSeparator + directoryPart;
    }
}
